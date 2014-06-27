package uk.ac.open.data.kiskit.v001.xml;

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
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(input));
			boolean institutionFound = false;
			boolean recording = false;
			StringBuilder recorder = null;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if ((!institutionFound) && event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("ukprn")) {
					XMLEvent content = eventReader.nextEvent();
					if (content.isCharacters()) {
						String lukprn = content.asCharacters().getData();
						if (lukprn.equals(this.ukprn)) {
							institutionFound = true;
							log.debug("[institution:start]");
						}
					}
				} else if (institutionFound && event.isEndElement() && event.asEndElement().getName().getLocalPart().toLowerCase().equals("institution")) {
					log.debug("[institution:end]");
					// stop
					break;
				} else if (institutionFound && event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("kiscourse")) {
					recorder = new StringBuilder();
					recorder.append("<?xml version='1.0' encoding='UTF-8'?>").append("<KISCOURSE>");
					// start loading this course
					recording = true;
				} else if (institutionFound && event.isEndElement() && event.asEndElement().getName().getLocalPart().toLowerCase().equals("kiscourse")) {
					// close root element
					recorder.append("</KISCOURSE>");
					// stop loading this course and flush
					fragments.add(recorder.toString());
					recording = false;
				} else if (recording) {
					// record
					StringWriter writer = new StringWriter();
					event.writeAsEncodedUnicode(writer);
					recorder.append(writer.getBuffer());
				}
			}
		} catch (FileNotFoundException e) {
			log.error("", e);
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			log.error("", e);
			throw new RuntimeException(e);
		}

	}
}
