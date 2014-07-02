package uk.ac.open.data.kiskit.v002.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.data.kiskit.v002.vocab.SKOS;
import uk.ac.open.data.kiskit.v002.vocab.Unistats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * TODO Support KISKI-17
 * 
 * @author ed4565
 * 
 */
public class AccreditationTypes {

	public class AccType {

		private String accBodyCode;

		public String getAccBodyCode() {
			return accBodyCode;
		}

		public void setAccBodyCode(String code) {
			this.accBodyCode = code;
		}

		public String getAccBodyLabel() {
			return accBodyLabel;
		}

		public void setAccBodyLabel(String label) {
			this.accBodyLabel = label;
		}

		/**
		 * XXX THIS IS THE UNIQUE ID
		 * @return
		 */
		public String getAccTypeCode() {
			return typeCode;
		}

		public void setAccTypeCode(String type) {
			this.typeCode = type;
		}

		public String getAccTypeLabel() {
			return accTypeLabel;
		}

		public void setCredit(String credit) {
			this.accTypeLabel = credit;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}

		private String accBodyLabel;
		private String typeCode;
		private String accTypeLabel;
		private String link;

		public AccType(String body, String bodylabel, String type, String typelabel, String link) {
			this.accBodyCode = body;
			this.accBodyLabel = bodylabel;
			this.typeCode = type;
			this.accTypeLabel = typelabel;
			this.link = link;
		}

		@Override
		public String toString() {
			return new StringBuilder(typeCode).append(' ').append(accBodyLabel).append(" (AccType: ").append(accBodyCode).append(")").toString();
		}

		@Override
		public boolean equals(Object obj) {
			return this.getClass().equals(obj.getClass()) && this.getAccBodyCode().equals(((AccType) obj).getAccBodyCode()) && this.getAccTypeCode().equals(((AccType) obj).getAccBodyCode());
		}
	}

	private static Logger log = LoggerFactory.getLogger(AccreditationTypes.class);
	private final static URL dataFile = AccreditationTypes.class.getResource("Accreditation_list.csv");
	private Map<String, AccType> accreditationTypes = new HashMap<String, AccType>();

	/**
	 * singleton
	 */
	private AccreditationTypes() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(dataFile.openStream()));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Pattern pattern = Pattern.compile("^([0-9]{3}),([^,]*|\"[^\"]*\")?,([0-9]+),([^,]*|\"[^\"]*\")?,(.*)$");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String bodyCode = matcher.group(1);
					String bodyLabel = matcher.group(2);
					String typeCode = matcher.group(3); // THIS IS THE UNIQ ID
					String typeLabel = matcher.group(4);
					String link = matcher.group(5);
					if(accreditationTypes.containsKey(typeCode)){
						log.warn("Key {} already exists", typeCode);
					}
					accreditationTypes.put(typeCode, new AccType(bodyCode, bodyLabel, typeCode, typeLabel, link));
				} else {
					log.error("SKIPPED The line cannot be parsed: \n{}\n", line);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		}
	}

	public Model toModel(){
		Model m = ModelFactory.createDefaultModel();
		Iterator<AccType> at = accreditationTypes.values().iterator();
		while(at.hasNext()){
			AccType y = at.next();
			Resource u = m.createResource(Unistats.getAccreditationTypeURI(y.getAccTypeCode()));
			u.addProperty(RDF.type, Unistats.AccreditationType);
			u.addProperty(RDFS.label, y.getAccTypeLabel(), "en");
			u.addProperty(SKOS.prefLabel, y.getAccTypeLabel());
			Resource accb = m.createResource(Unistats.getAccreditingBodyURI(y.getAccBodyCode()));
			u.addProperty(Unistats.accreditingBody, accb);
			accb.addProperty(RDF.type, Unistats.AccreditingBody);
			accb.addProperty(RDFS.label, y.getAccBodyLabel(), "en");
			accb.addProperty(SKOS.prefLabel, y.getAccBodyLabel());
		}
		return m;
	}
	public String getAccTypeLabel(String code) {
		if (accreditationTypes.containsKey(code)) {
			return accreditationTypes.get(code).getAccTypeLabel();
		}
		return "Unknown";
	}

	public boolean codeExists(String code) {
		return accreditationTypes.containsKey(code);
	}

	public final static Iterator<AccType> iterator() {
		return getInstance().accreditationTypes.values().iterator();
	}

	private static AccreditationTypes instance = null;

	public final static AccreditationTypes getInstance() {
		if (instance == null) {
			instance = new AccreditationTypes();
		}
		return instance;
	}

	public static void main(String[] args) {
		Iterator<AccType> a = AccreditationTypes.iterator();
		int x=0;
		while (a.hasNext()) {
			x++;
			System.out.println(a.next());
		}
		System.out.println(x + " accreditation types");
	}
}
