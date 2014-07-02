package uk.ac.open.data.kiskit.v002.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v002.vocab.SKOS;
import uk.ac.open.data.kiskit.v002.vocab.Unistats;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class KISAccreditationType {

	private File input = null;
	private Model model = null;

	private static Logger log = LoggerFactory.getLogger(KISAccreditationType.class);

	public KISAccreditationType(File inputFile) {
		input = inputFile;
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
			String accurl = null;
			String acctext = null;
			String acctextw = null;
			String acctype = null;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					path += "/" + event.asStartElement().getName();
					if (path.equals("/KIS/ACCREDITATIONTABLE")){
						// Starts parent element >> init variables
						 accurl = null;
						 acctext = null;
						 acctextw = null;
						 acctype = null;
					}else if (path.equals("/KIS/ACCREDITATIONTABLE/ACCURL")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							accurl = content.asCharacters().getData();
						}
					} else if (path.equals("/KIS/ACCREDITATIONTABLE/ACCTEXT")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							acctext = content.asCharacters().getData();
						}
					} else if (path.equals("/KIS/ACCREDITATIONTABLE/ACCTEXTW")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							acctextw = content.asCharacters().getData();
						}
					} else if (path.equals("/KIS/ACCREDITATIONTABLE/ACCTYPE")) {
						XMLEvent content = eventReader.nextEvent();
						if (content.isCharacters()) {
							acctype = content.asCharacters().getData();
						}
					}
					
				} else if (event.isEndElement()) {
					log.trace("{} ends", path);
					if (path.equals("/KIS/ACCREDITATIONTABLE")) {
						// Ends parent element >> register values
						if (acctype != null && acctext != null) {
							Resource r = model.createResource(Unistats.getAccreditationTypeURI(acctype));
							r.addProperty(RDFS.label, acctext, "en");
							r.addProperty(SKOS.prefLabel, acctext);
							if (acctextw != null) {
								r.addProperty(RDFS.label, acctextw, "cy");
							}
							if(accurl != null){
								r.addProperty(RDFS.seeAlso, accurl, XSDDatatype.XSDanyURI);
							}
							accurl = null;
							acctext = null;
							acctextw = null;
							acctype = null;
						} else {
							log.error("Skipping accreditation type: {} text: {}", acctype, acctext);
						}
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
		KISAccreditationType i = new KISAccreditationType(new File(args[0]));
		Model m = i.getModel();
		StmtIterator s = m.listStatements();
		while (s.hasNext()) {
			System.out.println(s.next());
		}
	}

}
