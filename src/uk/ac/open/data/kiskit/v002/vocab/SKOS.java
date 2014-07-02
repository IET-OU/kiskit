package uk.ac.open.data.kiskit.v002.vocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SKOS {
	public final static String NS = "http://www.w3.org/2004/02/skos/core#";
	public static final Property prefLabel = ResourceFactory.createProperty(NS + "prefLabel");
}
