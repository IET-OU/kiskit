package uk.ac.open.data.kiskit.v002.xml;

import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.stringValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v002.vocab.Unistats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class KISAim {
	private static final Logger log = LoggerFactory.getLogger(KISLocation.class);

	private File input;
	private Model model = null;

	public KISAim(File inputFile) {
		this.input = inputFile;
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
			String KISAIMCODE = null;
			String KISAIMLABEL = null;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					path += "/" + event.asStartElement().getName();
					if (path.equals("/KIS/KISAIM/KISAIMCODE")) {
						KISAIMCODE = stringValue(eventReader);
					} else if (path.equals("/KIS/KISAIM/KISAIMLABEL")) {
						KISAIMLABEL = stringValue(eventReader);
					}
				} else if (event.isEndElement()) {
					log.trace("{} ends", path);
					if (path.equals("/KIS/KISAIM")) {
						if (KISAIMCODE != null && KISAIMLABEL != null) {
							Resource r = model.createResource(Unistats.getAimURI(KISAIMCODE));
							r.addProperty(RDF.type, Unistats.Aim);
							r.addProperty(RDFS.label, KISAIMLABEL);
						} else {
							log.error("Skipping location, KISAIMCODE: {} KISAIMLABEL: {} ", new Object[] { KISAIMCODE, KISAIMLABEL });
						}
						KISAIMCODE = null;
						KISAIMLABEL = null;
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
		KISAim i = new KISAim(new File(args[0]));
		Model m = i.getModel();
		StmtIterator s = m.listStatements();
		while (s.hasNext()) {
			System.out.println(s.next());
		}
	}

}
