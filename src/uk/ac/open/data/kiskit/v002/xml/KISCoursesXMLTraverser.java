package uk.ac.open.data.kiskit.v002.xml;

import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.stringValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * Extracts the XML elements KISCOURSE of the institution with the UKPRN number
 * given. The KISCOURSE fragments are returned as stand-alone XML strings.
 * 
 * @author ed4565
 * 
 */
public abstract class KISCoursesXMLTraverser {

	private static Logger log = LoggerFactory.getLogger(KISCoursesXMLTraverser.class);
	private File input = null;
	private String singleUkprn = null;

	public KISCoursesXMLTraverser(File inputFile, String ukprn) {
		this.input = inputFile;
		this.singleUkprn = ukprn;
	}
	
	public KISCoursesXMLTraverser(File inputFile) {
		this.input = inputFile;
	}

	public void setSingleUkprn(String ukprn){
		this.singleUkprn = ukprn;
	}
	
	public abstract void handle(String ukprn, Model model);

	public void traverse() {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			boolean institutionFound = false;
			boolean recording = false;

			StringBuilder recorder = null;
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(input));

			String currentUKPRN = null;
			String path = "";
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					path += "/" + event.asStartElement().getName();
					if (path.equals("/KIS/INSTITUTION/UKPRN")) {
						currentUKPRN = stringValue(eventReader);
						if(singleUkprn == null || currentUKPRN.equals(singleUkprn)){
							institutionFound = true;
						}
					} else if (path.equals("/KIS/INSTITUTION/KISCOURSE") && institutionFound) {
						recorder = new StringBuilder();
						recorder.append("<?xml version='1.0' encoding='UTF-8'?>");
						// start loading this course
						recording = true;
					}
				} else if (event.isEndElement()) {
					log.trace("{} ends", path);
					if (path.equals("/KIS/INSTITUTION")) {
						// finished
						institutionFound = false;
						currentUKPRN = null;
					} else  if (path.equals("/KIS/INSTITUTION/KISCOURSE") && institutionFound) {
						// record closing element
						StringWriter writer = new StringWriter();
						event.writeAsEncodedUnicode(writer);
						recorder.append(writer.getBuffer());
						// stop loading this course and flush
						try {
							handle(currentUKPRN, new KISCourse(currentUKPRN, recorder.toString()).getModel());
						} catch (IOException e) {
							log.error("Skipping course", e);
						} catch (ParserConfigurationException e) {
							log.error("Skipping course", e);
						} catch (SAXException e) {
							log.error("Skipping course", e);
						}
						recording = false;
					}
					path = path.substring(0, path.lastIndexOf('/'));
				} 
				if (recording) {
					// record
					StringWriter writer = new StringWriter();
					event.writeAsEncodedUnicode(writer);
					recorder.append(writer.getBuffer());
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
}
