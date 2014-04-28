package uk.ac.open.data.kiskit.v002.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

public class RegexSameAsLinksBuilder {

	private Map<String, String> mappings = null;
	private boolean bothDirections = false;

	public RegexSameAsLinksBuilder(boolean bothDirections) {
		mappings = new HashMap<String, String>();
		this.bothDirections = bothDirections;
	}

	public RegexSameAsLinksBuilder() {
		this(false);
	}

	/**
	 * To be used within <br/>
	 * <code>
	 * String.replaceFirst(regexMatch, regexReplace)
	 * </code>
	 * 
	 * @param regexMatch
	 * @param regexReplace
	 */
	public void addMapping(String regexMatch, String regexReplace) {
		mappings.put(regexMatch, regexReplace);
	}

	public Map<String, String> getMappings() {
		return Collections.unmodifiableMap(mappings);
	}

	public Model buildSameAsLinks(Model from) {
		return buildSameAsLinks(from, this.bothDirections);
	}

	public Model buildSameAsLinks(Model from, boolean lbothDirections) {
		Model links = ModelFactory.createDefaultModel();
		ResIterator rit = from.listResourcesWithProperty(null);
		while (rit.hasNext()) {
			Resource r = rit.next();
			for (Entry<String, String> e : mappings.entrySet()) {
				if (r.toString().matches(e.getKey())) {
					String rs = r.toString();
					String sameAs = rs.replaceFirst(e.getKey(), e.getValue());
					Resource sr = links.createResource(sameAs);
					links.add(r, OWL.sameAs, sr);
					if (lbothDirections) {
						links.add(sr, OWL.sameAs, r);
					}
				}
			}
		}
		return links;
	}
}
