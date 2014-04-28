package uk.ac.open.data.kiskit.v002.jacs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Resource;

public class JacsLister {

	private static PrintStream o = System.out;

	public static void main(String[] args) {
		File in = null;

		in = new File(args[0]);

		o.println("Reading from: " + in.getAbsolutePath());
		o.println("Writing to: " + System.out);

		List<String> tagStack = new ArrayList<String>();
		Map<String, String> map = new HashMap<String, String>();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(in));
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					tagStack.add(event.asStartElement().getName().getLocalPart());
				} else if (event.isEndElement()) {
					tagStack.remove(tagStack.size() - 1);
				}
				// show the value if tag JACS (should be jacs 2.0)
				if (event.isStartElement() && (event.asStartElement().getName().getLocalPart().toLowerCase().equals("jacs"))) {
					XMLEvent content = eventReader.nextEvent();
					if (content.isCharacters()) {
						StringBuilder sb = new StringBuilder();
						for (String s : tagStack) {
							sb.append("/").append(s);
						}
						sb.append("\t").append(content);
						sb.append("\t");
						try {
							String jacsCode = content.asCharacters().getData();
							// try with
							Resource r = Jacs.jacs2_0(jacsCode);
							String jacsUri = null;
							if (r != null) {
								jacsUri = r.getURI();
							}
							map.put(jacsCode, jacsUri);
							sb.append(jacsUri);
						} catch (Exception e) {
							sb.append("ERROR: ").append(e.getMessage());
						}
						sb.append("\n");
						o.append(sb.toString());
					}
				} else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().toLowerCase().equals("sbj")) {
					// SBJ should be jacs levels 2 or 3
					XMLEvent content = eventReader.nextEvent();
					if (content.isCharacters()) {
						StringBuilder sb = new StringBuilder();
						for (String s : tagStack) {
							sb.append("/").append(s);
						}
						sb.append("\t").append(content);
						sb.append("\t");
						try {
							String jacsCode = content.asCharacters().getData();
							// try with
							Resource r = Jacs.jacs_l2_l3(jacsCode);
							String jacsUri = null;
							if (r != null) {
								jacsUri = r.getURI();
							}
							map.put(jacsCode, jacsUri);
							sb.append(jacsUri);
						} catch (Exception e) {
							sb.append("ERROR: ").append(e.getMessage());
						}
						sb.append("\n");
						o.append(sb.toString());
					}
				}
			}
			eventReader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		o.println("##############################################################");
	}
}
