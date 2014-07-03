package uk.ac.open.data.kiskit.v002.vocab;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Unistats {
	private final static String BASE = "http://data.linkedu.eu/";

	public final static String ns_DATA = BASE + "kis/";
	public final static String ns_ONTOLOGY = BASE + "kis/ontology/";

	private final static String ns_DSET = ns_DATA + "dataset/";
	private final static String ns_INSTITUTION = ns_DATA + "institution/";
	private final static String ns_COURSE = ns_DATA + "course/";
	private final static String ns_ACCREDITINGBODY = ns_DATA + "accreditingbody/";
	private final static String ns_ACCREDITINGTYPE = ns_DATA + "accreditationtype/";
	private final static String ns_ACCREDITATION = ns_DATA + "accreditation/";
	private static final String ns_JOBS = ns_DATA + "job/";
	private static final String ns_FEEMARKER = ns_DATA + "feemarker/";
	private static final String ns_AIM = ns_DATA + "aim/";
	private static final String ns_LOCATION = ns_DATA + "location/";
	public final static String ns_SKOLEMIZED = ns_DATA + "genid/";
	public final static String ns_OBSERVATION = ns_DATA + "observation/";

	// to be used as default in KisCourse
	public final static String ns_COURSE_DEFAULT = ns_COURSE;

	// External vocabularies
	public final static String ns_DC = "http://purl.org/dc/terms/";
	public final static String ns_QB = "http://purl.org/linked-data/cube#";
	public final static String ns_RKB_COURSEWARE = "http://courseware.rkbexplorer.com/ontologies/courseware#";
	public static final String ns_AIISO = "http://purl.org/vocab/aiiso/schema#";
	public final static String ns_GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";

	// Known universities
	public final static String ns_SouthHampton = "http://id.southampton.ac.uk/";
	private final static URL vocabularyTTL = Unistats.class.getResource("/uk/ac/open/data/kiskit/v002/vocab/vocabulary.ttl");

	/*
	 * Classes
	 */
	public final static Resource AIISOInstitution = ResourceFactory.createResource(ns_AIISO + "Institution"); 
	public final static Resource Institution = ResourceFactory.createResource(ns_ONTOLOGY + "Institution"); 
	public final static Resource RKBCourse = ResourceFactory.createResource(ns_RKB_COURSEWARE + "Course");
	public final static Resource AIISOCourse = ResourceFactory.createResource(ns_AIISO + "Course");
	public final static Resource Course = ResourceFactory.createResource(ns_ONTOLOGY + "Course");
	public final static Resource PartTimeCourse = ResourceFactory.createResource(ns_ONTOLOGY + "PartTimeCourse");
	public final static Resource FullTimeCourse = ResourceFactory.createResource(ns_ONTOLOGY + "FullTimeCourse");
	public final static Resource FullCourseLevelKIS = ResourceFactory.createResource(ns_ONTOLOGY + "FullCourseLevelKIS");
	public final static Resource MultipleSubjectCoursePlaceholderKIS = ResourceFactory.createResource(ns_ONTOLOGY + "MultipleSubjectCoursePlaceholderKIS");
	public final static Resource SubjectLevelKIS = ResourceFactory.createResource(ns_ONTOLOGY + "SubjectLevelKIS");
	public final static Resource Accreditation = ResourceFactory.createResource(ns_ONTOLOGY + "Accreditation");
	public final static Resource AccreditingBody = ResourceFactory.createResource(ns_ONTOLOGY + "AccreditingBody");
	public final static Resource Observation = ResourceFactory.createResource(ns_QB + "Observation");

	/**
	 * instances
	 * 
	 */
	public final static Resource ontology = ResourceFactory.createResource(ns_ONTOLOGY);
	
	public final static Resource commonJobs = ResourceFactory.createResource(ns_DSET + "commonJobs");
	public final static Resource continuation = ResourceFactory.createResource(ns_DSET + "continuation");
	public final static Resource courseStages = ResourceFactory.createResource(ns_DSET + "courseStages");
	public static final Resource employment = ResourceFactory.createResource(ns_DSET + "employment");
	public static final Resource degreeClasses = ResourceFactory.createResource(ns_DSET + "degreeClasses");
	public static final Resource entryQualifications = ResourceFactory.createResource(ns_DSET + "entryQualifications");
	public static final Resource jobTypes = ResourceFactory.createResource(ns_DSET + "jobTypes");
	public static final Resource nationalStudentSurveyResults = ResourceFactory.createResource(ns_DSET + "nationalStudentSurveyResults");
	public static final Resource nationalStudentSurveyNHSResults = ResourceFactory.createResource(ns_DSET + "nationalStudentSurveyNHSResults");
	public static final Resource salaries = ResourceFactory.createResource(ns_DSET + "salaries");
	public static final Resource tariffs = ResourceFactory.createResource(ns_DSET + "tariffs");

	public final static Resource commonJobsStructure = ResourceFactory.createResource(ns_DSET + "commonJobsStructure");
	public final static Resource continuationStructure = ResourceFactory.createResource(ns_DSET + "continuationStructure");
	public final static Resource courseStagesStructure = ResourceFactory.createResource(ns_DSET + "courseStagesStructure");
	public static final Resource employmentStructure = ResourceFactory.createResource(ns_DSET + "employmentStructure");
	public static final Resource degreeClassesStructure = ResourceFactory.createResource(ns_DSET + "degreeClassesStructure");
	public static final Resource entryQualificationsStructure = ResourceFactory.createResource(ns_DSET + "entryQualificationsStructure");
	public static final Resource jobTypesStructure = ResourceFactory.createResource(ns_DSET + "jobTypesStructure");
	public static final Resource nationalStudentSurveyResultsStructure = ResourceFactory.createResource(ns_DSET + "nationalStudentSurveyResultsStructure");
	public static final Resource nationalStudentSurveyNHSResultsStructure = ResourceFactory.createResource(ns_DSET + "nationalStudentSurveyNHSResultsStructure");
	public static final Resource salariesStructure = ResourceFactory.createResource(ns_DSET + "salariesStructure");
	public static final Resource tariffsStructure = ResourceFactory.createResource(ns_DSET + "tariffsStructure");

	/*
	 * Properties
	 */
	public final static Property courseId = ResourceFactory.createProperty(ns_ONTOLOGY + "courseId");
	public final static Property type = ResourceFactory.createProperty(ns_ONTOLOGY + "type");
	public final static Property title = ResourceFactory.createProperty(ns_DC + "title");
	public final static Property accomodationUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "accomodationUrl");
	public final static Property accomodationUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "accomodationUrlW");
	public final static Property courseUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "courseUrl");
	public final static Property relatedCourses = ResourceFactory.createProperty(ns_ONTOLOGY + "courseUrl");
	public final static Property ukPrnApply = ResourceFactory.createProperty(ns_ONTOLOGY + "ukPrnApply");
	public final static Property ukprn = ResourceFactory.createProperty(ns_ONTOLOGY + "ukprn");
	public final static Property courseUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "courseUrlW");
	public final static Property supportUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "supportUrl");
	public final static Property supportUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "supportUrlW");
	public final static Property employUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "employUrl");
	public final static Property employUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "employUrlW");
	public final static Property learningAndTeachingUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "learningAndTeachingUrl");
	public final static Property learningAndTeachingUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "learningAndTeachingUrlW");
	public final static Property jacsCode = ResourceFactory.createProperty(ns_ONTOLOGY + "jacsCode");
	public final static Property ldcsCode = ResourceFactory.createProperty(ns_ONTOLOGY + "ldcsCode");
	public final static Property nonCreditAssessment = ResourceFactory.createProperty(ns_ONTOLOGY + "nonCreditAssessment");
	public final static Property assessmentMethodsUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "assessmentMethodsUrl");
	public final static Property assessmentMethodsUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "assessmentMethodsUrlW");
	public final static Property ucasCourseId = ResourceFactory.createProperty(ns_ONTOLOGY + "ucasCourseId");
	public final static Property feeVariesByYear = ResourceFactory.createProperty(ns_ONTOLOGY + "feeVariesByYear");
	public final static Property feeToBeConfirmed = ResourceFactory.createProperty(ns_ONTOLOGY + "feeToBeConfirmed");
	public final static Property waiverAvailable = ResourceFactory.createProperty(ns_ONTOLOGY + "waiverAvailable");
	public final static Property meansSupport = ResourceFactory.createProperty(ns_ONTOLOGY + "meansSupport");
	public final static Property otherSupport = ResourceFactory.createProperty(ns_ONTOLOGY + "otherSupport");
	public final static Property englishFee = ResourceFactory.createProperty(ns_ONTOLOGY + "englishFee");
	public final static Property northernIrishFee = ResourceFactory.createProperty(ns_ONTOLOGY + "northernIrishFee");
	public final static Property scottishFee = ResourceFactory.createProperty(ns_ONTOLOGY + "scottishFee");
	public final static Property walesFee = ResourceFactory.createProperty(ns_ONTOLOGY + "walesFee");
	public final static Property availableInWelsh = ResourceFactory.createProperty(ns_ONTOLOGY + "availableInWelsh");
	public final static Property mode = ResourceFactory.createProperty(ns_ONTOLOGY + "mode");
	public final static Property awardLevel = ResourceFactory.createProperty(ns_ONTOLOGY + "awardLevel");
	public final static Property averageOfWrittenAssessment = ResourceFactory.createProperty(ns_ONTOLOGY + "averageOfWrittenAssessment");
	public final static Property averageOfCourseworkAssessment = ResourceFactory.createProperty(ns_ONTOLOGY + "averageOfCourseworkAssessment");
	public final static Property averageOfScheduledActivities = ResourceFactory.createProperty(ns_ONTOLOGY + "averageOfScheduledActivities");
	public final static Property accreditation = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditation");
	public final static Property accreditationUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditationUrl");
	public final static Property accreditingBody = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditingBody");
	public final static Property accreditationType = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditationType");
	public final static Property accreditationDependantOnChoice = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditationDependantOnChoice");
	public final static Property accreditationDependantOnChoiceUrl = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditationDependantOnChoiceUrl");
	public final static Property accreditationDependantOnChoiceUrlW = ResourceFactory.createProperty(ns_ONTOLOGY + "accreditationDependantOnChoiceUrlW");
	public final static Property subject = ResourceFactory.createProperty(ns_DC + "subject");
	public final static Property course = ResourceFactory.createProperty(ns_ONTOLOGY + "course");
	public final static Property institution = ResourceFactory.createProperty(ns_ONTOLOGY + "institution");
	public final static Property teaches = ResourceFactory.createProperty(ns_AIISO + "teaches");
	public final static Property taughtAt = ResourceFactory.createProperty(ns_RKB_COURSEWARE + "taught-at");
	public final static Property job = ResourceFactory.createProperty(ns_ONTOLOGY + "job");
	public final static Property percentage = ResourceFactory.createProperty(ns_ONTOLOGY + "percentage");
	public final static Property order = ResourceFactory.createProperty(ns_ONTOLOGY + "order");
	public final static Property population = ResourceFactory.createProperty(ns_ONTOLOGY + "population");
	public final static Property aggregation = ResourceFactory.createProperty(ns_ONTOLOGY + "aggregation");
	public final static Property dataSet = ResourceFactory.createProperty(ns_QB + "dataSet");
	public final static Property continuing = ResourceFactory.createProperty(ns_ONTOLOGY + "continuing");
	public final static Property dormant = ResourceFactory.createProperty(ns_ONTOLOGY + "dormant");
	public final static Property left = ResourceFactory.createProperty(ns_ONTOLOGY + "left");
	public final static Property gained = ResourceFactory.createProperty(ns_ONTOLOGY + "gained");
	public final static Property lower = ResourceFactory.createProperty(ns_ONTOLOGY + "lower");
	public final static Property stage = ResourceFactory.createProperty(ns_ONTOLOGY + "stage");
	public static final Property assessmentMethod = ResourceFactory.createProperty(ns_ONTOLOGY + "assessmentMethod");
	public static final Property coursework = ResourceFactory.createProperty(ns_ONTOLOGY + "coursework");
	public static final Property written = ResourceFactory.createProperty(ns_ONTOLOGY + "written");
	public static final Property practical = ResourceFactory.createProperty(ns_ONTOLOGY + "practical");
	public static final Property learningAndTeachingMethod = ResourceFactory.createProperty(ns_ONTOLOGY + "learningAndTeachingMethod");
	public static final Property independent = ResourceFactory.createProperty(ns_ONTOLOGY + "independent");
	public static final Property placement = ResourceFactory.createProperty(ns_ONTOLOGY + "placement");
	public static final Property scheduled = ResourceFactory.createProperty(ns_ONTOLOGY + "scheduled");
	public static final Property workOrStudy = ResourceFactory.createProperty(ns_ONTOLOGY + "workOrStudy");
	public static final Property assumedUnemployed = ResourceFactory.createProperty(ns_ONTOLOGY + "assumedUnemployed");
	public static final Property workAndStudy = ResourceFactory.createProperty(ns_ONTOLOGY + "workAndStudy");
	public static final Property notAvailable = ResourceFactory.createProperty(ns_ONTOLOGY + "notAvailable");
	public static final Property study = ResourceFactory.createProperty(ns_ONTOLOGY + "study");
	public static final Property work = ResourceFactory.createProperty(ns_ONTOLOGY + "work");
	public static final Property firstClass = ResourceFactory.createProperty(ns_ONTOLOGY + "firstClass");
	public static final Property upperSecondClass = ResourceFactory.createProperty(ns_ONTOLOGY + "upperSecondClass");
	public static final Property lowerSecondClass = ResourceFactory.createProperty(ns_ONTOLOGY + "lowerSecondClass");
	public static final Property otherHonours = ResourceFactory.createProperty(ns_ONTOLOGY + "otherHonours");
	public static final Property ordinary = ResourceFactory.createProperty(ns_ONTOLOGY + "ordinary");
	public static final Property notClassified = ResourceFactory.createProperty(ns_ONTOLOGY + "notClassified");
	public static final Property accessCourse = ResourceFactory.createProperty(ns_ONTOLOGY + "accessCourse");
	public static final Property aLevel = ResourceFactory.createProperty(ns_ONTOLOGY + "aLevel");
	public static final Property baccalaureate = ResourceFactory.createProperty(ns_ONTOLOGY + "baccalaureate");
	public static final Property degree = ResourceFactory.createProperty(ns_ONTOLOGY + "degree");
	public static final Property diploma = ResourceFactory.createProperty(ns_ONTOLOGY + "diploma");
	public static final Property foundation = ResourceFactory.createProperty(ns_ONTOLOGY + "foundation");
	public static final Property noQualifications = ResourceFactory.createProperty(ns_ONTOLOGY + "noQualifications");
	public static final Property other = ResourceFactory.createProperty(ns_ONTOLOGY + "other");
	public static final Property otherHE = ResourceFactory.createProperty(ns_ONTOLOGY + "otherHE");
	public static final Property professional = ResourceFactory.createProperty(ns_ONTOLOGY + "professional");
	public static final Property nonProfessional = ResourceFactory.createProperty(ns_ONTOLOGY + "nonProfessional");
	public static final Property unknown = ResourceFactory.createProperty(ns_ONTOLOGY + "unknown");

	/*
	 * NSS questions
	 */
	public static final Property nssQuestion1 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion1");
	public static final Property nssQuestion2 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion2");
	public static final Property nssQuestion3 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion3");
	public static final Property nssQuestion4 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion4");
	public static final Property nssQuestion5 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion5");
	public static final Property nssQuestion6 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion6");
	public static final Property nssQuestion7 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion7");
	public static final Property nssQuestion8 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion8");
	public static final Property nssQuestion9 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion9");
	public static final Property nssQuestion10 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion10");
	public static final Property nssQuestion11 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion11");
	public static final Property nssQuestion12 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion12");
	public static final Property nssQuestion13 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion13");
	public static final Property nssQuestion14 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion14");
	public static final Property nssQuestion15 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion15");
	public static final Property nssQuestion16 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion16");
	public static final Property nssQuestion17 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion17");
	public static final Property nssQuestion18 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion18");
	public static final Property nssQuestion19 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion19");
	public static final Property nssQuestion20 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion20");
	public static final Property nssQuestion21 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion21");
	public static final Property nssQuestion22 = ResourceFactory.createProperty(ns_ONTOLOGY, "nssQuestion22");
	public static final Property sectorLowerForty = ResourceFactory.createProperty(ns_ONTOLOGY, "sectorLowerForty");
	public static final Property sectorMedianForty = ResourceFactory.createProperty(ns_ONTOLOGY, "sectorMedianForty");
	public static final Property sectorUpperForty = ResourceFactory.createProperty(ns_ONTOLOGY, "sectorUpperForty");
	public static final Property sectorLowerSix = ResourceFactory.createProperty(ns_ONTOLOGY, "sectorLowerSix");
	public static final Property sectorMedianSix = ResourceFactory.createProperty(ns_ONTOLOGY, "sectorMedianSix");
	public static final Property sectorUpperSix = ResourceFactory.createProperty(ns_ONTOLOGY, "sectorUpperSix");
	public static final Property courseLower = ResourceFactory.createProperty(ns_ONTOLOGY, "courseLower");
	public static final Property courseMedian = ResourceFactory.createProperty(ns_ONTOLOGY, "courseMedian");
	public static final Property courseUpper = ResourceFactory.createProperty(ns_ONTOLOGY, "courseUpper");
	// XXX Note: the following properties are used through reflection; "nhsNssQuestion*"
	public static final Property nhsNssQuestion1 = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsNssQuestion1");
	public static final Property nhsNssQuestion2 = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsNssQuestion2");
	public static final Property nhsNssQuestion3 = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsNssQuestion3");
	public static final Property nhsNssQuestion4 = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsNssQuestion4");
	public static final Property nhsNssQuestion5 = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsNssQuestion5");
	public static final Property nhsNssQuestion6 = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsNssQuestion6");
	public static final Property tariff1 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff1");
	public static final Property tariff120 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff120");
	public static final Property tariff160 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff160");
	public static final Property tariff200 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff200");
	public static final Property tariff240 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff240");
	public static final Property tariff280 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff280");
	public static final Property tariff320 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff320");
	public static final Property tariff360 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff360");
	public static final Property tariff400 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff400");
	public static final Property tariff440 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff440");
	public static final Property tariff480 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff480");
	public static final Property tariff520 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff520");
	public static final Property tariff560 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff560");
	public static final Property tariff600 = ResourceFactory.createProperty(ns_ONTOLOGY, "tariff600");
	public static final Property numberOfBeds = ResourceFactory.createProperty(ns_ONTOLOGY, "numberOfBeds");
	public static final Property bedsLowerPrice = ResourceFactory.createProperty(ns_ONTOLOGY, "bedsLowerPrice");
	public static final Property bedsUpperPrice = ResourceFactory.createProperty(ns_ONTOLOGY, "bedsUpperPrice");
	public static final Property privateBedsLowerPrice = ResourceFactory.createProperty(ns_ONTOLOGY, "privateBedsLowerPrice");
	public static final Property privateBedsUpperPrice = ResourceFactory.createProperty(ns_ONTOLOGY, "privateBedsUpperPrice");
	public static final Property country = ResourceFactory.createProperty(ns_ONTOLOGY, "country");
	public static final Property nspParticipant = ResourceFactory.createProperty(ns_ONTOLOGY, "nspParticipant");
	public static final Property studentsUnionSatisfactory = ResourceFactory.createProperty(ns_ONTOLOGY, "studentsUnionSatisfactory");
	public static final Property nssPopulation = ResourceFactory.createProperty(ns_ONTOLOGY, "nssPopulation");

	// added in 002 (2013/2014)
	public static final Property distanceOnly = ResourceFactory.createProperty(ns_ONTOLOGY, "distanceOnly");
	public static final Property honoursDegreeCourse = ResourceFactory.createProperty(ns_ONTOLOGY, "honoursDegreeCourse");
	public static final Property locationChange = ResourceFactory.createProperty(ns_ONTOLOGY, "locationChange");
	public static final Property sandwitch = ResourceFactory.createProperty(ns_ONTOLOGY, "sandwitch");
	public static final Property aim = ResourceFactory.createProperty(ns_ONTOLOGY, "aim");
	public static final Property studentsUnionURL = ResourceFactory.createProperty(ns_ONTOLOGY, "studentsUnionURL");
	public static final Property studentsUnionWelshURL = ResourceFactory.createProperty(ns_ONTOLOGY, "studentsUnionWelshURL");
	public static final Resource Aim = ResourceFactory.createResource(ns_ONTOLOGY + "Aim");
	public static final Property yearAbroadAvailable = ResourceFactory.createProperty(ns_ONTOLOGY, "yearAbroadAvailable");
	public static final Resource AccreditationType = ResourceFactory.createResource(ns_ONTOLOGY + "AccreditationType");
	public static final Property nhsFundedStudents = ResourceFactory.createProperty(ns_ONTOLOGY, "nhsFundedStudents");
	public static final Property numStage = ResourceFactory.createProperty(ns_ONTOLOGY, "numStage");
	public static final Resource firstDegreeAward = ResourceFactory.createResource(ns_ONTOLOGY + "firstDegreeAward");
	public static final Resource otherUndergraduateAward = ResourceFactory.createResource(ns_ONTOLOGY + "otherUndergraduateAward");

	public static final Resource Job = ResourceFactory.createResource(ns_ONTOLOGY + "Job");
	
	

	public static Model getVocabulary() {
		Model m = ModelFactory.createDefaultModel();
		try {
			m.read(vocabularyTTL.toURI().toString(), "TTL");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return m;
	}

	public static final String getInstitutionURI(String ukprn) {
		return ns_INSTITUTION + urify(ukprn);
	}

	public static final String getCourseURI(String ukprn, String kiscourseId) {
		return getCourseURI(ukprn, kiscourseId, ns_COURSE_DEFAULT);
	}

	/**
	 * Builds a course URI with a specified base name
	 * 
	 * @param ukprn
	 * @param kiscourseId
	 * @param base
	 * @return
	 */
	public static final String getCourseURI(String ukprn, String kiscourseId, String base) {
		if (base == null) {
			base = ns_COURSE_DEFAULT;
		}
		return base + urify(ukprn) + "/" + urify(kiscourseId.toLowerCase());
	}

	private static String urify(String str) {
		// white spaces with +
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static String getJACSURI(String string) {
		// we don't urify this string
		return ns_SouthHampton + "JACS/" + string;
	}

	public static String getFeeMarkerURI(String fm) {
		return ns_FEEMARKER + urify(fm.toLowerCase());
	}

	public static String getAccreditationURI(String ukprn, String kisCourseId, int x) {
		return ns_ACCREDITATION + urify(ukprn) + "/" + urify(kisCourseId.toLowerCase()) + "/" + Integer.toString(x);
	}

	//
	public static String getAccreditingBodyURI(String code) {
		return ns_ACCREDITINGBODY + urify(code.toLowerCase());
	}

	public static String getJobURI(String codeFromDescription) {
		return ns_JOBS + urify(codeFromDescription.toLowerCase());
	}

	public static String skolemizedURI(AnonId bnodeId) {
		return ns_SKOLEMIZED + new String(Base64.encodeBase64(bnodeId.getLabelString().getBytes()));
	}

	public static String getObservationURI(Resource dataSet, String... objects){
		HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(dataSet);
		for(String o : objects){
			hcb.append(o);
		}
		
		return new StringBuilder().append(ns_OBSERVATION).append(dataSet.getLocalName()).append("/").append(Integer.toHexString(hcb.toHashCode())).toString();

	}
	
//	public static String createSkolemizedResource() {
//		return skolemizedURI(ResourceFactory.createResource().getId());
//	}

	public static String getAimURI(String value) {
		return ns_AIM + urify(value.toLowerCase());
	}

	public static String getLocationURI(String value) {
		return ns_LOCATION + urify(value.toLowerCase());
	}

	public static String getAccreditationTypeURI(String value) {
		return ns_ACCREDITINGTYPE + urify(value.toLowerCase());
	}
	
	public static void main(String args[]) throws URISyntaxException, IllegalArgumentException, IllegalAccessException {

		Model m = getVocabulary();
		//m = ModelFactory.createDefaultModel();
		PrintStream p = System.out;
		p.println(" -- Unistat Vocabulary :: Quality Check -- ");
		p.println();
		
		Field [] fields = Unistats.class.getFields();
		
		// Check that all entities starting with the unistats NS in vocabulary.ttl
		Set<String> fieldNames = new HashSet<String>();
		for(Field field : fields){
			fieldNames.add(field.getName());
		}
		// have a related field in the Unistats class
		for(Resource r : m.listResourcesWithProperty(RDF.type).toSet()){
			if(!r.isAnon() && r.getNameSpace().startsWith(Unistats.BASE)){
				String name = r.getLocalName();
				if(!fieldNames.contains(name) && !name.equals("")){
					p.println("Add <" + r.getURI() + "> to the fields of Unistats.class");
				}
			}
		}

		// Check that all constants are described in the vocabulary
		p.println(fields.length + " terms found.");
		for(Field field : fields){
			// only Resources
			if(RDFNode.class.isAssignableFrom(field.getType())){
				RDFNode n = (RDFNode) field.get(null);
				if(!m.containsResource(n)){
					p.println("\tDescribe " + n);
					//continue;
				}
				// Do further check only if KIS namespace
				if(n instanceof Resource){
					if(((Resource)n).getURI().startsWith(Unistats.ns_ONTOLOGY) ||
							((Resource)n).getURI().startsWith(Unistats.ns_DATA)){
						// Then describe carefully
					} else {
						// Mentioning is enough ...
						if(!m.contains((Resource)n, RDFS.isDefinedBy )){
							p.println("\tAdd rdfs:isDefinedBy to " + n);
						}
						continue;
					}
				}
				
				
				if(field.getType().equals(Property.class)){
					// is Property
					Property i = (Property) n;
					if(!m.contains(i, RDF.type, RDF.Property)){
						p.println("\tAdd type Property to " + n + " (is a property)");
					}
					if(!m.contains(i, RDF.type, OWL.ObjectProperty) && !m.contains(i, RDF.type, OWL.DatatypeProperty) && !m.contains(i, RDF.type, OWL.AnnotationProperty)){
						p.println("\tAdd OWL Property type to " + n );
					}
					if(!m.contains(i, RDFS.domain)){
						p.println("\tAdd rdfs:domain to " + n );
					}
					if(!m.contains(i, RDFS.range)){
						p.println("\tAdd rdfs:range to " + n );
					}
					
				} else if(field.getType().equals(Resource.class)){
					// is a Class or Resource or anything else, should have an rdf:type
					Resource i = (Resource) n;
					if(!m.contains(i, RDF.type)){
						p.println("\tAdd type to " + n + " (is a resource)");
					}
				} else {
					// This means a Literal, no point checking anything else
				}

				// check label, comment, isDefinedBy and seeAlso
				if(!m.contains((Resource) n, RDFS.label)){
					p.println("\tAdd rdfs:label to " + n);				
				} 
				if(!m.contains((Resource) n, SKOS.prefLabel)){
					p.println("\tAdd skos:prefLabel to " + n);				
				} 
				if(!m.contains((Resource) n, RDFS.comment)){
					p.println("\tAdd rdfs:comment to " + n);	
				} 
				if(!m.contains((Resource) n, RDFS.isDefinedBy)){
					p.println("\tAdd rdfs:isDefinedBy to " + n);	
				}
				if(!m.contains((Resource) n, RDFS.seeAlso)){
					p.println("\tAdd rdfs:seeAlso to " + n);	
				}
				
				// If it is a rdfs:Class, it must be also rdfs:subClassOf of something else (limit to owl:Thing)
				if(m.contains((Resource) n, RDF.type, RDFS.Class) || m.contains((Resource) n, RDF.type, OWL.Class)){
					if(m.contains((Resource) n, RDF.type, OWL.Class) && m.contains((Resource) n, RDF.type, OWL.Class)){
						// OK
					} else {
						p.println("\tNeeds to be both rdfs and owl Class :: " + n);
					}
					if(!m.contains((Resource) n, RDFS.subClassOf)){
						p.println("\tMust be rdfs:subClassOf something (owl:Thing?) :: " + n);
					}
				}
			}
		}

		//p.println(" -- Check that all classes and properties have a single skos:prefLabel without lang -- ");
		
		p.println();
		ResIterator resses = m.listResourcesWithProperty(RDF.type);
		while (resses.hasNext()) {
			Resource c = resses.next();
			StmtIterator aboutc = c.listProperties();
			int check = 0;
			while (aboutc.hasNext()) {
				Statement s = aboutc.next();
				if(s.getPredicate().equals(SKOS.prefLabel)){
					check++;
					if(s.getLiteral().getLanguage() != null && !s.getLiteral().getLanguage().equals("")){
						p.println("Remove lang from skos:prefLabel of " + c);
					}
				}
			}
			if(check == 0){
				p.println("Add skos:prefLabel to " + c);
			}else if(check>1){
				p.println("Too many (" + check + ") skos:prefLabel for " + c);
			}
		}
	}
}
