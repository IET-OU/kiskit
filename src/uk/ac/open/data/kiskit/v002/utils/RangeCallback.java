package uk.ac.open.data.kiskit.v002.utils;

import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public interface RangeCallback {
	public RDFNode get(String value) throws Exception;

	public final static RangeCallback xsdString = new RangeCallback() {
		@Override
		public RDFNode get(String value) {
			return ResourceFactory.createTypedLiteral(value);
		}
	};

	public final static RangeCallback xsdInt = new RangeCallback() {
		@Override
		public RDFNode get(String value) {
			return ResourceFactory.createTypedLiteral(new Integer(value));
		}
	};

	public final static RangeCallback anyUri = new RangeCallback() {
		@Override
		public RDFNode get(String value) throws URISyntaxException {
			return ResourceFactory.createTypedLiteral(new URI(value));
		}
	};

	public final static RangeCallback en = new RangeCallback() {
		// this is idiot... there is no other way to build a langed
		// literal???
		// ResourceFactory does not have this method...
		@Override
		public RDFNode get(String value) throws URISyntaxException {
			return ModelFactory.createDefaultModel().createLiteral(value, "en");
		}
	};
	public final static RangeCallback cy = new RangeCallback() {
		// this is idiot... there is no other way to build a langed
		// literal???
		// ResourceFactory does not have this method...
		@Override
		public RDFNode get(String value) throws URISyntaxException {
			return ModelFactory.createDefaultModel().createLiteral(value, "en");
		}
	};
	public final static RangeCallback plain = new RangeCallback() {
		@Override
		public RDFNode get(String value) throws URISyntaxException {
			return ResourceFactory.createPlainLiteral(value);
		}
	};
	/**
	 * String is Boolean True when is:
	 * a) True (ignore case)
	 * b) 1
	 */
	public final static RangeCallback bool = new RangeCallback() {
		@Override
		public RDFNode get(String value) throws URISyntaxException {
			return ResourceFactory.createTypedLiteral(new Boolean(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") ? "True" : "False"));
		}
	};

}
