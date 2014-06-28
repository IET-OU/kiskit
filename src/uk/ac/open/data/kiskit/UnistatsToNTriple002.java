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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import uk.ac.open.data.kiskit.v002.data.AccreditationTypes;
import uk.ac.open.data.kiskit.v002.data.AccreditationTypes.AccType;
import uk.ac.open.data.kiskit.v002.data.Institutions;
import uk.ac.open.data.kiskit.v002.vocab.Unistats;
import uk.ac.open.data.kiskit.v002.xml.KISAccreditationType;
import uk.ac.open.data.kiskit.v002.xml.KISAim;
import uk.ac.open.data.kiskit.v002.xml.KISCoursesXMLTraverser;
import uk.ac.open.data.kiskit.v002.xml.KISInstitution;
import uk.ac.open.data.kiskit.v002.xml.KISLocation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.ibm.icu.impl.Assert;

public class UnistatsToNTriple002 {

	private static PrintStream o = System.out;
	public final static URL staticTTL = UnistatsToNTriple002.class.getResource("/uk/ac/open/data/kiskit/static.v002.ttl");

	public static void main(String[] args) {
		try {
			if (args.length < 2)
				help();
			// if arg size is 2, make all
			if (args.length == 2) {
				makeAll(args);
			} else if (args.length > 2) {
				makeSingle(args);
			}
		} catch (IOException e) {
			o.println("An error occurred: " + e.getMessage());
			help();
		}
	}
	
	public static void makeSingle(String[] args) {
		File dumpDirectory = null;
		File out = null;
		
		dumpDirectory = new File(args[0]);
		out = new File(args[1]);

		File in = null;
		// This must be the directory of the dump of unistats
		if (dumpDirectory.isDirectory() && dumpDirectory.exists()) {
			// Check if the needed files do exist
			Collection<?> xmlfiles = FileUtils.listFiles(dumpDirectory, new String[]{"xml"}, false);
			Assert.assrt("The input directory must contain a single XML file", xmlfiles.size() == 1);
			in = (File) xmlfiles.iterator().next();
		}
		
		o.println("Reading from: " + in.getAbsolutePath());
		o.println("Writing to: " + out.getAbsolutePath());
		
		final String ukprn = args[2];
		boolean exists = false;
		// labels of the institution
		Model istlbl = Institutions.getInstance().toModel();
		o.print("Institution: ");
		for (Entry<String, String> e : Institutions.getInstance().getMap().entrySet()) {
			if(e.getKey().equals(ukprn)){
				exists = true;
				o.println(e.getKey() + " " +e.getValue());
			}
		}
		
		if(!exists){
			o.println(ukprn + " does not exists");
			return;
		}
		
		// Prepare to write
		BufferedOutputStream bos;
		try {
			OutputStream os = new FileOutputStream(out);
			bos = new BufferedOutputStream(os);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			help();
			return;
		}
		
		// Institutions
		Model institution = new KISInstitution(in, Unistats.ns_COURSE_DEFAULT + ukprn + "/" , ukprn).getModel();
		institution.add(istlbl.listStatements(new SimpleSelector(institution.createResource(Unistats.getInstitutionURI(ukprn)), null, (RDFNode) null)));
		institution.write(bos, "N-TRIPLE");
		o.println("Got data about institution: " + institution.size() + " triples written.");
				
		// Locations
		KISLocation locations = new KISLocation(in, ukprn);
		locations.getModel().write(bos, "N-TRIPLES");
		o.println("Got data about locations: " + locations.getModel().size() + " triples written.");

		extractCourses(in, bos, ukprn);
	}
	
	public static void makeAll(String[] args) throws IOException {
		File dumpDirectory = null;
		File out = null;

		dumpDirectory = new File(args[0]);
		out = new File(args[1]);

		File in = null;
		// This must be the directory of the dump of unistats
		if (dumpDirectory.isDirectory() && dumpDirectory.exists()) {
			// Check if the needed files do exist
			Collection<?> xmlfiles = FileUtils.listFiles(dumpDirectory, new String[]{"xml"}, false);
			Assert.assrt("The input directory must contain a single XML file", xmlfiles.size() == 1);
			in = (File) xmlfiles.iterator().next();
		}
		
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
		o.println("KIS Ontology and vocabulary information: " + staticrdf.size() + " triples written");
		
		// labels of institutions
		Model istlbl = Institutions.getInstance().toModel();
		o.println("Institutions:");
		for (Entry<String, String> e : Institutions.getInstance().getMap().entrySet()) {
			o.println(" - " + e.getKey() + " " +e.getValue());
		}
		istlbl.write(bos, "N-TRIPLE");
		o.println("List of institutions: " + istlbl.size() + " triples written.");
		
		// Accreditations types
		Model acctypes = AccreditationTypes.getInstance().toModel();
		o.println("Accreditations types:");
		Iterator<AccType> is = AccreditationTypes.iterator();
		while (is.hasNext()) {
			AccType at = is.next();
			o.println(" - " + at.getAccTypeCode() + " " +at.getAccTypeLabel());
		}
		acctypes.write(bos, "N-TRIPLE");
		o.println("List of accreditation types: " + acctypes.size() + " triples written.");
		
		// Institutions
		KISInstitution institutions = new KISInstitution(in);
		institutions.getModel().write(bos, "N-TRIPLE");
		o.println("Got data about institutions: " + institutions.getModel().size() + " triples written.");
		// Accreditations
		KISAccreditationType accreditations = new KISAccreditationType(in);
		accreditations.getModel().write(bos, "N-TRIPLE");
		o.println("Got data about accreditations: " + accreditations.getModel().size() + " triples written.");
		// Aims
		KISAim aims = new KISAim(in);
		aims.getModel().write(bos, "N-TRIPLES");
		o.println("Got data about aims: " + aims.getModel().size() + " triples written.");
		// Locations
		KISLocation locations = new KISLocation(in);
		locations.getModel().write(bos, "N-TRIPLES");
		o.println("Got data about locations: " + locations.getModel().size() + " triples written.");
		
		// Courses
		//for ( String ukprn : institutions.ukprnCodes()) {
		extractCourses(in, bos, null);
		//}
		bos.close();
		long end = System.currentTimeMillis();
		o.println("Done in +" + ((end - start) / 1000) + "s");
	}

	private static int ttt = 0;
	private static int cix = 0;
	private static void extractCourses(File in, final BufferedOutputStream bos, String ukprn){
		KISCoursesXMLTraverser tr = new KISCoursesXMLTraverser(in) {
			String cukprn = null;
			
			@Override
			public void handle(String ukprn, Model model) {
				if(!ukprn.equals(cukprn)){
					o.print(' ');
					o.print(cix);
					o.print(':');
					o.print(ttt);
					o.println("t written");
					cukprn = ukprn;
					o.println("Getting data about courses of " + ukprn);
					ttt=0;
					cix=0;
				}
				cix++;
				model.write(bos, "N-TRIPLE");
				ttt += model.size() ;
				o.print('.');
				if(cix % 50 == 0){
					o.print(' ');
					o.print(cix);
					o.print(':');
					o.print(ttt);
					o.println('t');
				}
				o.flush();
			}
		};
		tr.setSingleUkprn(ukprn);
		tr.traverse();
		o.print(' ');
		try {
			o.print(cix);
			o.print(ttt);
		} catch (Exception e) {
			// XXX this is not really important...
			e.printStackTrace();
		} 
		o.print(':');
		o.println("t written");	
	}
//	
//	private static void extractCoursesOfUKPRN(File in, BufferedOutputStream bos, String ukprn){
//		KISCoursesXMLSlicer slicer = new KISCoursesXMLSlicer(in, ukprn);
//		o.println("Getting data about courses of " + ukprn + " (" + slicer.getXMLFragments().size() + ")");
//		int ttt = 0;
//		int cix = 0;
//		for (String xml : slicer.getXMLFragments()) {
//			cix++;
//			KISCourse kc;
//			try {
//				kc = new KISCourse(ukprn, xml);
//				kc.getModel().write(bos, "N-TRIPLE");
//				ttt += kc.getModel().size() ;
//				o.print('.');
//				if(cix % 50 == 0){
//					o.print(' ');
//					o.print(cix);
//					o.print(':');
//					o.print(ttt);
//					o.println('t');
//				}
//				o.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (ParserConfigurationException e) {
//				e.printStackTrace();
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//		}
//		o.print(' ');
//		o.print(cix);
//		o.print(':');
//		o.print(ttt);
//		o.println("t written");
//	}
	
	public static void help() {
		o.println("KIS-KIT 2013/2014 (v002)");
		o.println();
		o.println("To extract all unistats (including the vocabulary specification).");
		o.println();
		o.println("\tjava -jar <executable> <inputDir> <outputFile>");
		o.println();
		o.println("To extract data of a single institution.");
		o.println();
		o.println("\tjava -jar <executable> <inputDir> <outputFile> <ukprn>");
		o.println();
		o.println("inputDir - is the directory of a unistats dump");
		o.println("outputFile - is the file you want the output to be written");
		o.println("ukprn - is the UKPRN of the institution (eg \"10007773\")");
		o.println();
		o.println("-------------");
		o.println("KIS-KIT is brought to you by the Knowledge Media Institute (The Open University).");
		o.println();
		o.println("The Unistats Dataset is reproduced with the permission of the Higher Education Funding Council");
		o.println("for England (HEFCE), the Higher Education Statistics Agency (HESA) and their licensors. The Unistats");
		o.println("Dataset may be accessed in its original form here: http://www.hesa.ac.uk/unistatsdata.  All copyright ");
		o.println("and other intellectual property rights in the Unistats Dataset are owned by HEFCE, HESA and their licensors.");
	}
}
