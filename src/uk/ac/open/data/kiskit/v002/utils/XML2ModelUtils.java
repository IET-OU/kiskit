package uk.ac.open.data.kiskit.v002.utils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class XML2ModelUtils {

	private static final Logger log = LoggerFactory.getLogger(XML2ModelUtils.class);

	/**
	 * Get next event, assume is a character stream and gets its content as
	 * String
	 * 
	 * @param eventReader
	 * @return
	 */
	public static final String stringValue(XMLEventReader eventReader) {
		try {
			XMLEvent content = eventReader.nextEvent();
			if (content.isCharacters()) {
				return content.asCharacters().getData();
			} else {
				log.error("Not a value: {}", content);
			}
		} catch (XMLStreamException e) {
			log.error("Not valid value: {}", e);
		}
		return null;
	}

	/**
	 * Proxies to stringValue and get the Integer out of it
	 * 
	 * @param eventReader
	 * @return Integer
	 */
	public static final Integer intValue(XMLEventReader eventReader) {
		try {
			return Integer.valueOf(stringValue(eventReader));
		} catch (Exception e) {
			log.error("Not a number? ", e);
			return null;
		}
	}

	/**
	 * Takes the first element in doc with name tag, set its value as property
	 * of subject in subject's Model using range as datatype provider
	 * 
	 * @param doc
	 * @param subject
	 * @param tag
	 * @param property
	 * @param range
	 */
	public static final void single(Document doc, Resource subject, String tag, Property property, RangeCallback range) {
		NodeList els = doc.getElementsByTagName(tag);
		if (els.getLength() == 0) {
			return;
		}
		String value = null;
		try {
			value = els.item(0).getTextContent();
			RDFNode n = range.get(value);
			subject.getModel().add(subject, property, n);
		} catch (Exception e) {
			log.warn("Skipping tag: {}, value: {}, message: {}", new Object[] { tag, value, e.getMessage() });
		}
	}

	/**
	 * Takes all the elements in "doc" with name "tag", set their values as
	 * "property" of "subject" in subject's Model using "range" as data type
	 * provider
	 * 
	 * @param doc
	 * @param subject
	 * @param tag
	 * @param p
	 * @param ob
	 */
	public static final void many(Document doc, Resource subject, String tag, Property property, RangeCallback range) {
		NodeList els = doc.getElementsByTagName(tag);
		for (int x = 0; x < els.getLength(); x++) {
			Node item = els.item(x);
			String value = item.getTextContent();
			try {
				RDFNode n = range.get(value);
				subject.getModel().add(subject, property, n);
			} catch (Exception e) {
				log.warn("Skipping tag: {}, value: {}, message: {}", new Object[] { tag, value, e.getMessage() });
			}
		}
	}

	/**
	 * Takes the first direct child of "node" with name "tag", set its value as
	 * "property" of "subject" in subject's Model using "range" as datatype
	 * provider
	 * 
	 * @param subject
	 * @param node
	 * @param tag
	 * @param property
	 * @param range
	 */
	public static final void single(Resource subject, Node node, String tag, Property property, RangeCallback range) {

		NodeList els = node.getChildNodes();
		if (els.getLength() == 0) {
			return;
		}
		for (int x = 0; x < els.getLength(); x++) {
			Node item = els.item(x);
			if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().toLowerCase().equals(tag.toLowerCase())) {
				String value = null;
				try {
					value = item.getTextContent();
					try {
						subject.getModel().add(subject, property, range.get(value));
					} catch (NullPointerException npe) {
						npe.printStackTrace();
					}
					return; // only the first
				} catch (Exception e) {
					//log.debug("subject: {}, node: {}, tag: {}, p: {}, ob: {} ", new Object[] { subject, node, tag, property, range });
					log.warn("Skipping tag: {}, value: {}, message: {}, exception: {}", new Object[] { tag, value, e.getMessage(), e.getClass().getCanonicalName() });
				}
			}
		}
	}

	/**
	 * Takes all the direct children of "node" with name "tag", set the values as
	 * "property" of "subject" in subject's Model using "range" as datatype
	 * provider
	 * 
	 * @param subject
	 * @param node
	 * @param tag
	 * @param property
	 * @param range
	 */
	public static final void many(Resource subject, Node node, String tag, Property property, RangeCallback range) {
		NodeList els = node.getChildNodes();
		for (int x = 0; x < els.getLength(); x++) {
			Node item = els.item(x);
			if (!item.getNodeName().toLowerCase().equals(tag.toCharArray())) {
				continue;
			}
			String value = item.getTextContent();
			try {
				subject.getModel().add(subject, property, range.get(value));
			} catch (Exception e) {
				log.warn("Skipping tag: {}, value: {}, message: {}", new Object[] { tag, value, e.getMessage() });
			}
		}
	}

	/**
	 * Gets the value of the first direct child of "node" named "tag".
	 * Returns null if none is found.
	 * 
	 * @param node
	 * @param tag
	 * @return
	 */
	public static final String single(Node node, String tag) {
		NodeList els = node.getChildNodes();
		if (els.getLength() == 0) {
			return null;
		}
		for (int x = 0; x < els.getLength(); x++) {
			Node item = els.item(x);
			if (item.getNodeName().toLowerCase().equals(tag.toLowerCase())) {
				return item.getTextContent();
			}
		}
		return null;
	}
}
