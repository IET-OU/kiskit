package uk.ac.open.data.kiskit.v002.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v002.vocab.Unistats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Institutions {
	private static Logger log = LoggerFactory.getLogger(Institutions.class);
	private final static URL dataFile = Institutions.class.getResource("./UNISTATS_UKPRN_lookup_20130827.csv");
	private Map<String, String> institutions = new HashMap<String, String>();

	public Institutions() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataFile.getFile()));
			String line;
			boolean firstline = true;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Pattern pattern = Pattern.compile("([0-9]+),(.*)$");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String code = matcher.group(1);
					String description = matcher.group(2);
					institutions.put(code, description);
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

	public String getDescription(String ukprn) {
		if (institutions.containsKey(ukprn)) {
			return institutions.get(ukprn);
		}
		return "";
	}

	public boolean codeExists(String ukprn) {
		return institutions.containsKey(ukprn);
	}

	private static Institutions instance = null;

	public static Institutions getInstance() {
		if (instance == null) {
			instance = new Institutions();
		}
		return instance;
	}

	public Model toModel() {
		Model m = ModelFactory.createDefaultModel();
		for (Entry<String, String> e : getMap().entrySet()) {
			Resource i = ResourceFactory.createResource(Unistats.getInstitutionURI(e.getKey()));
			m.add(i, RDF.type, Unistats.Institution);
			m.add(i, RDFS.label, ResourceFactory.createPlainLiteral(e.getValue()));
		}
		return m;
	}

	public Map<String, String> getMap() {
		return Collections.unmodifiableMap(institutions);
	}

	public static void main(String[] args) {
		System.out.println("Institutions:");
		System.out.println("UKPRN :: Description");
		for (Entry<String, String> e : Institutions.getInstance().getMap().entrySet()) {
			System.out.println(e.getKey() + " :: " + e.getValue());
		}
	}

}
