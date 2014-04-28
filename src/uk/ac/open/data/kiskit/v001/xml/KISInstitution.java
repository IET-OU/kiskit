package uk.ac.open.data.kiskit.v001.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v001.vocab.Unistats;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("ukprn")) {
					XMLEvent content = eventReader.nextEvent();
					if (content.isCharacters()) {
						String ukprn = content.asCharacters().getData();
						log.debug("UKPRN: {}", ukprn);
						// if we want this institution
						if (all || ukprnCodes.contains(ukprn)) {
							collect = true;
						} else {
							collect = false;
							continue; // don't load this istitution
						}
						log.debug("Collecting about: {}", ukprn);
						Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(ukprn));
						model.add(r, RDF.type, Unistats.Institution);
						lastukprn = ukprn;
						// in any case we add the code
						// this is because it is possible that
						// the class have been initialized without
						// specifying a list of ukprn (all=true)
						// and the method asking the list of institutions
						// have never been called. So we preload them to avoid
						// another useless file iteration at that time
						// If the set have been already initialized, it doesn't
						// make the difference.
						ukprnCodes.add(lastukprn);
					}
				} else if (collect) {
					if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("accomurl")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String accurl = content.asCharacters().getData();
							try {
								Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
								model.add(r, Unistats.accomodationUrl, ResourceFactory.createTypedLiteral(accurl, XSDDatatype.XSDanyURI));
							} catch (Exception e) {
								log.warn("Skipped: ukprn: {} property: {} Error: {}", new Object[] { lastukprn, Unistats.accomodationUrl, e.getMessage() });
							}
						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("accomurlw")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String accurl = content.asCharacters().getData();
							try {
								Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
								model.add(r, Unistats.accomodationUrlW, ResourceFactory.createTypedLiteral(accurl, XSDDatatype.XSDanyURI));
							} catch (Exception e) {
								log.warn("Skipped: ukprn: {} property: {} Error: {}", new Object[] { lastukprn, Unistats.accomodationUrl, e.getMessage() });
							}
						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("instbeds")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String beds = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r, Unistats.numberOfBeds, ResourceFactory.createTypedLiteral(new Integer(beds)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("instlower")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String beds = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r, Unistats.bedsLowerPrice, ResourceFactory.createTypedLiteral(new Integer(beds)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("instupper")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String beds = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r, Unistats.bedsUpperPrice, ResourceFactory.createTypedLiteral(new Integer(beds)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("privatelower")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String beds = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r,

							Unistats.privateBedsLowerPrice, ResourceFactory.createTypedLiteral(new Integer(beds)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("privateupper")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String beds = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r,

							Unistats.privateBedsUpperPrice, ResourceFactory.createTypedLiteral(new Integer(beds)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("country")) {
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
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("nsp")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String cc = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r, Unistats.nspParticipant, ResourceFactory.createTypedLiteral(cc));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("q24")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String cc = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r, Unistats.studentsUnionSatisfactory, ResourceFactory.createTypedLiteral(new Integer(cc)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("q24pop")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String cc = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							model.add(r,

							Unistats.nssPopulation, ResourceFactory.createTypedLiteral(new Integer(cc)));

						}
					} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("kiscourseid")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							String cc = content.asCharacters().getData();
							Resource r = ResourceFactory.createResource(Unistats.getInstitutionURI(lastukprn));
							Resource ccc = ResourceFactory.createResource((ns_COURSE == null) ? Unistats.getCourseURI(lastukprn, cc.toLowerCase()) : ns_COURSE + cc.toLowerCase());
							model.add(r, Unistats.teaches, ccc);
						}
					}
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
	}

	public static void main(String[] args) {
		KISInstitution i = new KISInstitution(new File(args[0]));
		Model m = i.getModel();
		StmtIterator s = m.listStatements(null, RDF.type, Unistats.Institution);
		System.out.println(s.toList().size());
	}
}
