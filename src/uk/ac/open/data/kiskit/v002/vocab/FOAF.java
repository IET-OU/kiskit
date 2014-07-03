package uk.ac.open.data.kiskit.v002.vocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class FOAF {
	public final static String NS = "http://xmlns.com/foaf/0.1/";
	public final static Property page = ResourceFactory.createProperty(NS + "page");
}
