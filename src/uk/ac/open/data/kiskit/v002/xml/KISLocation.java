package uk.ac.open.data.kiskit.v002.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v002.vocab.Unistats;
import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.intValue;
import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.stringValue;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

public class KISLocation {

	private static final Logger log = LoggerFactory.getLogger(KISLocation.class);

	private File input;
	private Model model = null;
	private String[] ukprns = null;
	public KISLocation(File inputFile) {
		this.input = inputFile;
		this.ukprns = new String[0];
	}

	public KISLocation(File inputFile, String... ukprns) {
		this(inputFile);
		this.ukprns = ukprns;
	}

	public Model getModel() {
		if (model == null) {
			buildRDFModel();
		}
		return model;
	}

	private void buildRDFModel() {
		model = ModelFactory.createDefaultModel();

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(input));

			String path = "";
			String ACCOMURL = null;
			String ACCOMURLW = null;
			String LOCID = null;
			String LOCNAME = null;
			String LOCNAMEW = null;
			String LATITUDE = null;
			String LONGITUDE = null;
			String UKPRN = null;
			String LOCUKPRN = null;
			Integer PRIVATELOWER = null;
			Integer PRIVATEUPPER = null;
			String SUURL = null;
			String SUURLW = null;
			Integer INSTBED = null;
			Integer INSTLOWER = null;
			Integer INSTUPPER = null;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					path += "/" + event.asStartElement().getName();
					if (path.equals("/KIS/LOCATION/LOCUKPRN")) {
						LOCUKPRN = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/UKPRN")) {
						UKPRN = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/ACCOMURL")) {
						ACCOMURL = stringValue(eventReader);
					}  else if (path.equals("/KIS/LOCATION/ACCOMURLW")) {
						ACCOMURLW = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/LOCNAME")) {
						LOCNAME = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/LOCNAMEW")) {
						LOCNAMEW = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/LOCID")) {
						LOCID = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/LATITUDE")) {
						LATITUDE = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/LONGITUDE")) {
						LONGITUDE = stringValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/PRIVATELOWER")) {
						PRIVATELOWER = intValue(eventReader);
					} else if (path.equals("/KIS/LOCATION/PRIVATEUPPER")) {
						PRIVATEUPPER = intValue(eventReader);
					}else if (path.equals("/KIS/LOCATION/INSTBED")) {
						INSTBED = intValue(eventReader);
					}else if (path.equals("/KIS/LOCATION/INSTLOWER")) {
						INSTLOWER = intValue(eventReader);
					}else if (path.equals("/KIS/LOCATION/INSTUPPER")) {
						INSTUPPER = intValue(eventReader);
					}
				} else if (event.isEndElement()) {
					log.trace("{} ends", path);
					if (path.equals("/KIS/LOCATION")) {
						boolean skip = false;
						if(ukprns.length > 0){
							if(!Arrays.asList(ukprns).contains(UKPRN)){
								skip = true;
							}
						}
						if (LOCID != null && (LOCUKPRN != null || UKPRN != null) && !skip) {
							Resource r = model.createResource(Unistats.getLocationURI(LOCID));
							if (LOCNAME == null) {
								LOCNAME = "Location " + LOCID;
							}
							r.addProperty(RDFS.label, LOCNAME, "en");
							if (LOCNAMEW != null) {
								r.addProperty(RDFS.label, LOCNAMEW, "cy");
							}
							if (ACCOMURL != null) {
								r.addProperty(RDFS.seeAlso, ACCOMURL, XSDDatatype.XSDanyURI);
								r.addProperty(Unistats.accomodationUrl, ACCOMURL, XSDDatatype.XSDanyURI);
							}
							if (ACCOMURLW != null) {
								r.addProperty(Unistats.accomodationUrlW, ACCOMURLW, XSDDatatype.XSDanyURI);
							}
							if (LATITUDE != null && LONGITUDE != null) {
								r.addProperty(model.createProperty(Unistats.ns_GEO + "lat"), LATITUDE);
								r.addProperty(model.createProperty(Unistats.ns_GEO + "long"), LONGITUDE);
							}
							if (LOCUKPRN == null) {
								LOCUKPRN = UKPRN;
							}
							r.addProperty(DCTerms.isPartOf, Unistats.getInstitutionURI(LOCUKPRN));
							if (PRIVATELOWER != null && PRIVATEUPPER != null) {
								model.addLiteral(r, Unistats.privateBedsLowerPrice, (int) PRIVATELOWER);
								model.addLiteral(r, Unistats.privateBedsUpperPrice, (int) PRIVATEUPPER);
							}
							if (SUURL != null) {
								r.addProperty(Unistats.studentsUnionURL, SUURL, XSDDatatype.XSDanyURI);
							}
							if (SUURLW != null) {
								r.addProperty(Unistats.studentsUnionWelshURL, SUURLW, XSDDatatype.XSDanyURI);
							}
							if (INSTBED != null) {
								model.addLiteral(r, Unistats.numberOfBeds, (int) INSTBED);
							}
							if (INSTLOWER != null) {
								model.addLiteral(r, Unistats.bedsLowerPrice, (int) INSTLOWER);
							}
							if (INSTUPPER != null) {
								model.addLiteral(r, Unistats.bedsUpperPrice, (int) INSTUPPER);
							}
						} else {
							log.error("Skipping location, LOCID: {} UKPRN: {} LOCUKPRN: {} LOCNAME: {}", new Object[] { LOCID, UKPRN, LOCUKPRN, LOCNAME });
						}

						ACCOMURL = null;
						ACCOMURLW = null;
						LOCID = null;
						LOCNAME = null;
						LOCNAMEW = null;
						LATITUDE = null;
						LONGITUDE = null;
						UKPRN = null;
						LOCUKPRN = null;
						PRIVATELOWER = null;
						PRIVATEUPPER = null;
						SUURL = null;
						SUURLW = null;
						INSTBED = null;
						INSTLOWER = null;
						INSTUPPER = null;
					}
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
	}

	public static void main(String[] args) {
		KISLocation i = new KISLocation(new File(args[0]));
		Model m = i.getModel();
		StmtIterator s = m.listStatements();
		while (s.hasNext()) {
			System.out.println(s.next());
		}
	}
}
