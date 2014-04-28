package uk.ac.open.data.kiskit.v002.xml;

import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.stringValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extracts the XML elements KISCOURSE of the institution with the UKPRN number
 * given. The KISCOURSE fragments are returned as stand-alone XML strings.
 * 
 * @author ed4565
 * 
 */
public class KISCoursesXMLSlicer {

	private static Logger log = LoggerFactory.getLogger(KISCoursesXMLSlicer.class);
	private File input = null;
	private String ukprn = null;
	private List<String> fragments = null;

	public KISCoursesXMLSlicer(File inputFile, String ukprn) {
		this.ukprn = ukprn;
		this.input = inputFile;
	}

	public Set<String> getXMLFragments() {
		if (fragments == null) {
			extractXMLFragments();
		}
		return new HashSet<String>(fragments);
	}

	private void extractXMLFragments() {
		fragments = new ArrayList<String>();
		
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			boolean institutionFound = false;
			boolean recording = false;

			StringBuilder recorder = null;
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(input));

			String path = "";
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					path += "/" + event.asStartElement().getName();
					if (path.equals("/KIS/INSTITUTION/UKPRN")) {
						String lukprn = stringValue(eventReader);
						if(lukprn.equals(this.ukprn)){
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
					} else  if (path.equals("/KIS/INSTITUTION/KISCOURSE") && institutionFound) {
						// record closing element
						StringWriter writer = new StringWriter();
						event.writeAsEncodedUnicode(writer);
						recorder.append(writer.getBuffer());
						// stop loading this course and flush
						String str = recorder.toString();
						log.debug("----\n{}\n----", str);
						fragments.add(str);
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
		log.info(" - {} found.", fragments.size());
	}
}
