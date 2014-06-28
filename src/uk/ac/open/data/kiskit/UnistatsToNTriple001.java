package uk.ac.open.data.kiskit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import uk.ac.open.data.kiskit.v001.data.Institutions;
import uk.ac.open.data.kiskit.v001.vocab.Unistats;
import uk.ac.open.data.kiskit.v001.xml.KISCourse;
import uk.ac.open.data.kiskit.v001.xml.KISCoursesXMLSlicer;
import uk.ac.open.data.kiskit.v001.xml.KISInstitution;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

public class UnistatsToNTriple001 {

	private static PrintStream o = System.out;
	public final static URL staticTTL = UnistatsToNTriple001.class.getResource("/uk/ac/open/data/kiskit/static.v001.ttl");
	public static void main(String[] args) throws IOException {
		File in = null;
		File out = null;

		in = new File(args[0]);
		out = new File(args[1]);

		o.println("Reading from: " + in.getAbsolutePath());
		o.println("Writing to: " + out.getAbsolutePath());
		long start = System.currentTimeMillis();

		BufferedOutputStream bos;
		try {
			OutputStream os = new FileOutputStream(out);
			bos = new BufferedOutputStream(os);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			help();
			return;
		}
		// Put vocabulary
		Unistats.getVocabulary().write(bos, "N-TRIPLE");

		// Put static data
		Model staticrdf = ModelFactory.createDefaultModel();
		try {
			staticrdf.read(staticTTL.toURI().toString(), "TTL");
			staticrdf.write(bos, "N-TRIPLE");
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		// Institutions
		KISInstitution institutions = new KISInstitution(in);
		institutions.getModel().write(bos, "N-TRIPLE");
		// labels
		Model istlbl = ModelFactory.createDefaultModel();
		for (Entry<String, String> e : Institutions.getInstance().getMap().entrySet()) {
			istlbl.add(ResourceFactory.createResource(Unistats.getInstitutionURI(e.getKey())), RDFS.label, ResourceFactory.createPlainLiteral(e.getValue()));
		}
		istlbl.write(bos, "N-TRIPLE");

		// Courses
		for (String ukprn : institutions.ukprnCodes()) {
			KISCoursesXMLSlicer slicer = new KISCoursesXMLSlicer(in, ukprn);
			for (String xml : slicer.getXMLFragments()) {
				KISCourse kc;
				try {
					kc = new KISCourse(ukprn, xml);
					kc.getModel().write(bos, "N-TRIPLE");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
			}
		}
		bos.close();
		long end = System.currentTimeMillis();
		o.println("Done in +" + ((end - start) * 1000) + "s");
	}

	public static void help() {
		o.println("Usage:");
		o.println("\tjava -classpath \"...\" uk.ac.open.data.extraction.unistats.UnistatsToNTriple001 <inputFile> <outputFile>");
		o.println();
	}
}
