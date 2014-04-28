package uk.ac.open.data.kiskit.v001.jacs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v001.utils.RangeCallback;
import uk.ac.open.data.kiskit.v001.vocab.Unistats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class Jacs {

	private static Logger log = LoggerFactory.getLogger(Jacs.class);
	private final static URL dataFile = Jacs.class.getResource("./jacs-skos-southampton.rdf");
	private final static URL jacsLookup = Jacs.class.getResource("./UNISTATS_subject_aggregation_lookup.csv");

	private Set<String> jacs_2_0;
	private Set<String> jacs_level_1;
	private Set<String> jacs_level_2;
	private Set<String> jacs_level_3;
	private Map<String, String> jacs_2_0__1;
	private Map<String, String> jacs_2_0__2;
	private Map<String, String> jacs_2_0__3;
	private Model model = null;

	private Jacs() throws IOException {
		model = ModelFactory.createDefaultModel();
		model.read(dataFile.openConnection().getInputStream(), "RDF/XML");

		jacs_2_0 = new HashSet<String>();
		jacs_level_1 = new HashSet<String>();
		jacs_level_2 = new HashSet<String>();
		jacs_level_3 = new HashSet<String>();
		jacs_2_0__1 = new HashMap<String, String>();
		jacs_2_0__2 = new HashMap<String, String>();
		jacs_2_0__3 = new HashMap<String, String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(jacsLookup.getFile()));
			String line;
			boolean firstline = true;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Pattern pattern = Pattern.compile("[01],([0-9a-zA-Z]{4})*,([0-9a-zA-Z]{1}),([0-9]{2}),([0-9]{3})$");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String j20 = matcher.group(1);
					String j1 = matcher.group(2);
					String j2 = matcher.group(3);
					String j3 = matcher.group(4);
					jacs_2_0.add(j20);
					jacs_level_1.add(j1);
					jacs_level_2.add(j2);
					jacs_level_3.add(j3);

					jacs_2_0__1.put(j20, j1);
					jacs_2_0__2.put(j20, j2);
					jacs_2_0__3.put(j20, j3);
				} else {
					if (!firstline) {
						log.error("SKIPPED The line cannot be parsed: \n{}\n", line);
					}
				}
				if (firstline) {
					firstline = false;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		}
	}

	//private static final String NS = Unistats.ns_SouthHampton + "JACS/";
	private static Jacs jacs = null;

	private static final Jacs getInstance() {
		try {
			if (jacs == null) {
				jacs = new Jacs();
			}
			return jacs;
		} catch (IOException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		}
	}

	private class JacsRangeCallback_2_0 implements RangeCallback {

		@Override
		public RDFNode get(String value) throws Exception {
			// check if it is a JACS 2.0
			if (!jacs_2_0.contains(value)) {
				throw new RuntimeException("Not a JACS 2.0 code: " + value);
			} else {
				// find mapping with level 3
				String level3 = jacs_2_0__3.get(value);
				// build sha uri
				String uri = Unistats.getJACSURI("L3." + level3);
				Resource r = ResourceFactory.createResource(uri);
				if (model.containsResource(r)) {
					return r;
				}

				// try with level 2
				String level2 = jacs_2_0__2.get(value);
				// build sha uri
				uri = Unistats.getJACSURI("L2." + level2);
				r = ResourceFactory.createResource(uri);
				if (model.containsResource(r)) {
					return r;
				}

				// try with level 1
				String level1 = jacs_2_0__1.get(value);
				// build sha uri
				uri = Unistats.getJACSURI("L1." + level1);
				r = ResourceFactory.createResource(uri);
				if (model.containsResource(r)) {
					return r;
				}

				// give up!
				throw new RuntimeException("No JACS URI: 2.0 = " + value + "; 3 = " + level3 + "; 2 = " + level2 + "; 1 = " + level1);
			}
		}
	}

	/**
	 * in cases we know it is level 2 or 3
	 */
	private class JacsRangeCallback_l2_l3 implements RangeCallback {

		@Override
		public RDFNode get(String value) throws Exception {
			String uri = null;
			// check if it is a JACS level 2 or level 3
			if (jacs_level_3.contains(value)) {
				String v3 = value;
				// if starts with 0, remove it
				if (v3.startsWith("0")) {
					v3 = value.substring(1);
				}
				// build shampt uri
				uri = Unistats.getJACSURI("L3." + v3);
			} else if (jacs_level_2.contains(value)) {
				// build shampt uri
				uri = Unistats.getJACSURI("L2." + value);
			}

			// XXX the below is based on a heuristic
			if (uri == null) {
				// if it is a single number could be of level 2
				if (value.matches("^[0-9]{1}$") && jacs_level_2.contains("0" + value)) {
					uri = Unistats.getJACSURI("L2.0" + value);
				}

				// XXX
				// if it is a single letter it could be of level 1, but
				// shampton's level1 is different form
				// the level 1 in reference table UNISTATS_subject_aggragation
				// this is wrong, according with the documentation.
				// but what can we do?
				if (value.matches("^[A-Z]{1}$")) {
					if (jacs_level_1.contains(value)) {
						// build shampt uri level 1
						uri = Unistats.getJACSURI(value + "0");
					}
				}
			}
			Resource r = ResourceFactory.createResource(uri);
			if (model.containsResource(r)) {
				return r;
			} else {
				throw new RuntimeException("jacs uri does not exist in sthampton:" + r.getURI());
			}
		}
	}

	// return the range callback for jacs code 2.0
	public static RangeCallback getRangeCallback_2_0() {
		return getInstance().new JacsRangeCallback_2_0();
	}

	public static RangeCallback getRangeCallback_l2_l3() {
		return getInstance().new JacsRangeCallback_l2_l3();
	}

	/** returns null if not found/valid */
	public static Resource jacs2_0(String code) {
		try {
			return (Resource) getRangeCallback_2_0().get(code);
		} catch (Exception e) {
			log.error("{}", e);
		}
		return null;

	}

	/** returns null if not found/valid */
	public static Resource jacs_l2_l3(String code) {
		try {
			return (Resource) getRangeCallback_l2_l3().get(code);
		} catch (Exception e) {
			log.error("{}", e);
		}
		return null;
	}
}
