package uk.ac.open.data.kiskit.v002.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v002.vocab.FOAF;
import uk.ac.open.data.kiskit.v002.vocab.Unistats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This class reads a Unistats XML and triplifies informations about
 * institutions, including links that connect the institution to its unistats
 * courses.
 * 
 * The approach here is SAX based, so we can to traverse the entire XML without
 * loading all in memory.
 * 
 * @author ed4565
 * 
 */
public class KISInstitution {

	private File input = null;
	private Model model = null;

	private static Logger log = LoggerFactory.getLogger(KISInstitution.class);

	private String ns_COURSE = null;
	private Set<String> ukprnCodes = null;
	private boolean all = true;

	public KISInstitution(File inputFile, String base_ns_course) {
		input = inputFile;
		this.ns_COURSE = base_ns_course;
	}

	public KISInstitution(File inputFile, String base_ns_course, String... ukprnCodes) {
		this(inputFile, base_ns_course);
		this.ukprnCodes = new HashSet<String>();
		this.ukprnCodes.addAll(Arrays.asList(ukprnCodes));
		this.all = false;
	}

	public KISInstitution(File inputFile) {
		this(inputFile, null);
	}

	public Set<String> ukprnCodes() {
		if (ukprnCodes == null) {
			buildUkprnCodes();
		}
		return ukprnCodes;
	}

	private void buildUkprnCodes() {
		// don't build it if it is not wanted or necessary
		// (this is just to be sure, this method should not be called in
		// such situations)
		if (all != true || ukprnCodes != null) {
			return;
		}
		ukprnCodes = new HashSet<String>();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(input));
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("ukprn")) {
					XMLEvent content = eventReader.nextEvent();
					if (content.isCharacters()) {
						String ukprn = content.asCharacters().getData();
						ukprnCodes.add(ukprn);
					}
				}
			}
		} catch (FileNotFoundException e) {
			log.error("FATAL: {}", e.getMessage());
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			log.error("FATAL: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Model getModel() {
		if (model == null) {
			buildRDFModel();
		}
		return model;
	}

	private void buildRDFModel() {
		model = ModelFactory.createDefaultModel();

		if (ukprnCodes == null) {
			// if this is not initialized, we populate it during this iteration
			ukprnCodes = new HashSet<String>();
		}
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(input));
			String lastukprn = null;
			boolean collect = false;
			String path = "";
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					path += "/" + event.asStartElement().getName();
					log.trace("{} starts", path);
					if (path.equals("/KIS/INSTITUTION/UKPRN")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String ukprn = content.asCharacters().getData();
							// if we want this institution
							if (all || ukprnCodes.contains(ukprn)) {
								log.debug("{} triples so far. Now about: {}", model.size(), ukprn);
								collect = true;
							} else {
								collect = false;
								continue; // don't load this istitution
							}
							Resource r = model.createResource(Unistats.getInstitutionURI(ukprn));
							model.add(r, RDF.type, Unistats.AIISOInstitution);
							model.add(r, RDF.type, Unistats.Institution);
							
							r.addProperty(Unistats.ukprn, ukprn);
							// Implements GH Issue #3
							r.addProperty(FOAF.page, model.createResource("http://learning-provider.data.ac.uk/ukprn/" + ukprn + ".html"));
							r.addProperty(OWL.sameAs, model.createResource("http://id.learning-provider.data.ac.uk/ukprn/" + ukprn ));
							
							lastukprn = ukprn;
							// in any case we add the code
							// this is because it is possible that
							// the class have been initialized without
							// specifying a list of ukprn (all=true)
							// and the method asking the list of institutions
							// have never been called. So we preload them to
							// avoid
							// another useless file iteration at that time
							// If the set have been already initialized, it
							// doesn't
							// make the difference.
							ukprnCodes.add(lastukprn);
						}
					} else if (collect) {
						log.trace("{}", path);
						if (path.equals("/KIS/INSTITUTION/COUNTRY")) {
							XMLEvent content = eventReader.nextEvent();
							if (content.isCharacters()) {
								String cc = content.asCharacters().getData();
								Resource country = null;
								if (cc.equals("XF")) {
									country = ResourceFactory.createResource("http://dbpedia/resource/England");
								} else if (cc.equals("XG")) {
									country = ResourceFactory.createResource("http://dbpedia/resource/Northern_Ireland");
								} else if (cc.equals("XH")) {
									country = ResourceFactory.createResource("http://dbpedia/resource/Scotland");
								} else if (cc.equals("XI")) {
									country = ResourceFactory.createResource("http://dbpedia/resource/Wales");
								} else {
									log.error("Unrecognized country: {}", cc);
								}
								
								if (country != null) {
									Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
									model.add(r, Unistats.country, country);
								}
							}
						} else if (path.equals("/KIS/INSTITUTION/NSP")) {
							XMLEvent content = eventReader.nextEvent();
							if (content.isCharacters()) {
								String cc = content.asCharacters().getData();
								Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
								model.add(r, Unistats.nspParticipant, ResourceFactory.createTypedLiteral(cc));
							}
						} else if (path.equals("/KIS/INSTITUTION/Q24")) {
							XMLEvent content = eventReader.nextEvent();
							if (content.isCharacters()) {
								String cc = content.asCharacters().getData();
								Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
								model.add(r, Unistats.studentsUnionSatisfactory, ResourceFactory.createTypedLiteral(new Integer(cc)));
							}
						} else if (path.equals("/KIS/INSTITUTION/Q24POP")) {
							XMLEvent content = eventReader.nextEvent();
							if (content.isCharacters()) {
								String cc = content.asCharacters().getData();
								Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
								model.add(r,
								Unistats.nssPopulation, ResourceFactory.createTypedLiteral(new Integer(cc)));
							}
						} else if (path.equals("/KIS/INSTITUTION/KISCOURSE/KISCOURSEID")) {
							XMLEvent content = eventReader.nextEvent();
							if (content.isCharacters()) {
								String cc = content.asCharacters().getData();
								Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
								Resource ccc = ResourceFactory.createResource((ns_COURSE == null) ? Unistats.getCourseURI(lastukprn, cc.toLowerCase()) : ns_COURSE + cc.toLowerCase());
								model.add(r, Unistats.teaches, ccc);
							}
						}
					}

				} else if (event.isEndElement()) {
					log.trace("{} ends", path);
					path = path.substring(0, path.lastIndexOf('/'));
				}
			}
			eventReader.close();
		} catch (FileNotFoundException e) {
			log.error("", e);
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			log.error("", e);
			throw new RuntimeException(e);
		}
		log.debug("{} triples", model.size());
	}

	public static void main(String[] args) {
		KISInstitution i = new KISInstitution(new File(args[0]), "urn:x-kis-course-test:");
		Model m = i.getModel();
		StmtIterator s = m.listStatements(null, Unistats.ukprn, (RDFNode) null);
		while (s.hasNext()) {
			Statement st = s.next();
			String ukprn = st.getString();
			System.out.println("UKPRN: " + ukprn);
			Resource in = st.getSubject();
			for(Statement is : in.listProperties().toSet()){
				// Verify link to external resources
				if(is.getPredicate().equals(FOAF.page)||is.getPredicate().equals(OWL.sameAs)){
					try {
						HttpURLConnection huc = (HttpURLConnection) new URL(is.getResource().getURI()).openConnection();
						HttpURLConnection.setFollowRedirects(true);
						if(huc.getResponseCode()!=200){
							System.err.println(is.getResource() + " [" + huc.getResponseCode()+ "]");
						}else{
							System.out.println(is.getResource() + " [" + huc.getResponseCode()+ "]");
						}
					} catch (MalformedURLException e) {
						System.err.println("Bad URL: " + is.getResource());
					} catch (IOException e) {
						System.err.println("Broken link: " + is.getResource());
					}
				}
			}
		}
	}
}
