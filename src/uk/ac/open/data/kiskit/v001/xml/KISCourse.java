package uk.ac.open.data.kiskit.v001.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.open.data.kiskit.v001.data.AccreditationBodies;
import uk.ac.open.data.kiskit.v001.data.Jobs;
import uk.ac.open.data.kiskit.v001.jacs.Jacs;
import uk.ac.open.data.kiskit.v001.utils.RangeCallback;
import uk.ac.open.data.kiskit.v001.utils.UnknownValueError;
import uk.ac.open.data.kiskit.v001.vocab.Unistats;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class KISCourse {

	private static final Logger log = LoggerFactory.getLogger(KISCourse.class);

	private Model model = null;
	private Document doc = null;
	private String ns_COURSE_BASE = null;
	private String ukprn = null;

	public KISCourse(String ukprn, String source) throws IOException, ParserConfigurationException, SAXException {
		this(ukprn, source, null);

	}

	public KISCourse(String ukprn, String source, String baseURIcourse) throws IOException, ParserConfigurationException, SAXException {
		this.ns_COURSE_BASE = baseURIcourse;
		this.ukprn = ukprn;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(source.getBytes());
		doc = dBuilder.parse(is);
	}

	/**
	 * Extract a single value looking for the first child of node named tag
	 * level
	 * 
	 * @param tag
	 * @return
	 */
	String single(Node node, String tag) {
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

	/**
	 * populates the model with the given subject using the first tag at any
	 * level in the document and ob as callback
	 * 
	 * @param tag
	 * @param p
	 * @param ob
	 *            range callback
	 */
	void single(Resource subject, String tag, Property p, RangeCallback ob) {
		NodeList els = doc.getElementsByTagName(tag);
		if (els.getLength() == 0) {
			return;
		}
		String value = null;
		try {
			value = els.item(0).getTextContent();
			RDFNode n = ob.get(value);
			model.add(subject, p, n);
		} catch (Exception e) {
			log.warn("Skipping tag: {}, value: {}, message: {}", new Object[] { tag, value, e.getMessage() });
		}
	}

	/**
	 * populates the model with the current subject using the all elements named
	 * "tag" at any level in the document and ob as callback
	 * 
	 * @param tag
	 * @param p
	 * @param ob
	 */
	void many(Resource subject, String tag, Property p, RangeCallback ob) {
		NodeList els = doc.getElementsByTagName(tag);
		for (int x = 0; x < els.getLength(); x++) {
			Node item = els.item(x);
			String value = item.getTextContent();
			try {
				RDFNode n = ob.get(value);
				model.add(subject, p, n);

			} catch (Exception e) {
				log.warn("Skipping tag: {}, value: {}, message: {}", new Object[] { tag, value, e.getMessage() });
			}
		}
	}

	/**
	 * from the first direct children with element "tag"
	 * 
	 * @param node
	 * @param tag
	 * @param p
	 * @param ob
	 */
	void single(Resource subject, Node node, String tag, Property p, RangeCallback ob) {

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
					try{
						model.add(subject, p, ob.get(value));
					}catch(NullPointerException npe){
						npe.printStackTrace();
					}
					return; // only the first
				} catch (Exception e) {
					log.debug(" subject: {}, node: {}, tag: {}, p: {}, ob: {} ", new Object[]{subject, node, tag, p, ob});
					log.warn("Skipping tag: {}, value: {}, message: {}, exception: {}", new Object[] { tag, value, e.getMessage(), e.getClass().getCanonicalName() });
				}
			}
		}
	}

	/**
	 * from the value of the direct childrens named "tag"
	 * 
	 * @param node
	 * @param tag
	 * @param p
	 * @param ob
	 */
	void many(Resource subject, Node node, String tag, Property p, RangeCallback ob) {
		NodeList els = node.getChildNodes();
		for (int x = 0; x < els.getLength(); x++) {
			Node item = els.item(x);
			if (!item.getNodeName().toLowerCase().equals(tag.toCharArray())) {
				continue;
			}
			String value = item.getTextContent();
			try {
				model.add(subject, p, ob.get(value));
			} catch (Exception e) {
				log.warn("Skipping tag: {}, value: {}, message: {}", new Object[] { tag, value, e.getMessage() });
			}
		}
	}

	private void buildRDFModel() {

		model = ModelFactory.createDefaultModel();
		String kisCourseId = doc.getElementsByTagName("KISCOURSEID").item(0).getTextContent();

		final Resource courseResource = model.createResource(Unistats.getCourseURI(ukprn, kisCourseId,  ns_COURSE_BASE));

		model.add(courseResource, RDF.type, Unistats.Course);
		model.add(courseResource, Unistats.taughtAt, ResourceFactory.createResource(Unistats.getInstitutionURI(ukprn)));

		// FIELDS
		single(courseResource, "KISCOURSEID", Unistats.courseId, RangeCallback.xsdString);
		single(courseResource, "KISTYPE", Unistats.type, RangeCallback.xsdInt);

		many(courseResource, "RELATEDKIS", Unistats.relatedCourses, new RangeCallback() {
			@Override
			public RDFNode get(String value) throws Exception {
				return ResourceFactory.createResource(Unistats.getCourseURI(ukprn, value,  ns_COURSE_BASE));
			}
		});
		many(courseResource, "UKPRNAPPLY", Unistats.ukPrnApply, RangeCallback.xsdString);

		many(courseResource, "LDCS", Unistats.ldcsCode, RangeCallback.xsdString);

		many(courseResource, "JACS", Unistats.jacsCode, Jacs.getRangeCallback_2_0());

		single(courseResource, "TITLE", Unistats.title, RangeCallback.en);
		single(courseResource, "TITLEW", Unistats.title, RangeCallback.cy);

		single(courseResource, "CRSEURL", Unistats.courseUrl, RangeCallback.anyUri);
		single(courseResource, "CRSEURLW", Unistats.courseUrlW, RangeCallback.anyUri);
		single(courseResource, "SUPPORTURL", Unistats.supportUrl, RangeCallback.anyUri);
		single(courseResource, "SUPPORTURLW", Unistats.supportUrlW, RangeCallback.anyUri);
		single(courseResource, "NONCREDITASS", Unistats.nonCreditAssessment, RangeCallback.bool);
		single(courseResource, "LTURL", Unistats.learningAndTeachingUrl, RangeCallback.anyUri);
		single(courseResource, "LTURLW", Unistats.learningAndTeachingUrlW, RangeCallback.anyUri);
		single(courseResource, "EMPLOYURL", Unistats.employUrl, RangeCallback.anyUri);
		single(courseResource, "EMPLOYURLW", Unistats.employUrlW, RangeCallback.anyUri);
		single(courseResource, "ASSURL", Unistats.assessmentMethodsUrl, RangeCallback.anyUri);
		single(courseResource, "ASSURLW", Unistats.assessmentMethodsUrlW, RangeCallback.anyUri);
		// KISKEY No longer used
		single(courseResource, "UCASCOURSEID", Unistats.ucaCourseId, RangeCallback.xsdString);
		single(courseResource, "VARFEE", Unistats.feeVariesByYear, new RangeCallback() {
			@Override
			public RDFNode get(String value) throws Exception {
				return ResourceFactory.createResource(Unistats.getFeeMarkerURI(value));
			}
		});
		single(courseResource, "FEETBC", Unistats.feeToBeConfirmed, RangeCallback.bool);
		single(courseResource, "WAIVER", Unistats.waiverAvailable, RangeCallback.bool);
		single(courseResource, "MEANSSUP", Unistats.meansSupport, RangeCallback.bool);
		single(courseResource, "OTHSUP", Unistats.otherSupport, RangeCallback.bool);
		single(courseResource, "ENGFEE", Unistats.englishFee, RangeCallback.xsdInt);
		single(courseResource, "SCOTFEE", Unistats.scottishFee, RangeCallback.xsdInt);
		single(courseResource, "NIFEE", Unistats.northernIrishFee, RangeCallback.xsdInt);
		single(courseResource, "WAFEE", Unistats.walesFee, RangeCallback.xsdInt);
		single(courseResource, "WELSH", Unistats.availableInWelsh, RangeCallback.xsdInt);
		single(courseResource, "MODE", Unistats.mode, new RangeCallback() {
			@Override
			public RDFNode get(String value) throws Exception {
				Resource ct;
				Integer val = new Integer(value);
				if (val == 2) {
					ct = Unistats.PartTimeCourse;
				} else {
					ct = Unistats.FullTimeCourse;
				}

				// we use two classes: 1. Part time course 2. Full time
				// course
				model.add(courseResource, RDF.type, ct);
				// We keep the original value for the property
				return ResourceFactory.createTypedLiteral(val);
			}
		});
		single(courseResource, "LEVEL", Unistats.awardLevel, RangeCallback.xsdInt);
		single(courseResource, "AVGWRITTEN", Unistats.averageOfWrittenAssessment, RangeCallback.xsdInt);
		single(courseResource, "AVGCOURSEWORK", Unistats.averageOfCourseworkAssessment, RangeCallback.xsdInt);
		single(courseResource, "AVGSCHEDULED", Unistats.averageOfScheduledActivities, RangeCallback.xsdInt);

		// XXX There is no clear meaning of KISCOURSE/SBJ,
		// documentation is incomplete and inconsistent with actual values
		// we adopted a set of strategies considering that this
		// should refer to jacs levels 2 or 3
		many(courseResource, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

		/**
		 * KISCOURSE/ACCREDITATION (s)
		 */
		NodeList accreditationsNodeList = doc.getElementsByTagName("ACCREDITATION");
		for (int x = 1; x < accreditationsNodeList.getLength() + 1; x++) {
			//Resource accreditationRes = ResourceFactory.createResource(Unistats.ns_ACCREDITATION + ukprn + "/" + kisCourseId.toLowerCase() + "/" + x);
			Resource accreditationRes = ResourceFactory.createResource(Unistats.getAccreditationURI( ukprn , kisCourseId, x));
			model.add(courseResource, Unistats.accreditation, accreditationRes);

			// about the accreditation
			model.add(accreditationRes, RDF.type, Unistats.Accreditation);
			try {
				String albl = "Accreditation of: " + courseResource.getProperty(Unistats.title).getLiteral().getString();
				model.add(accreditationRes, RDFS.label, model.createLiteral(albl, "en"));
			} catch (Exception e) {
				log.warn("Cannot produce label for {}", accreditationRes);
			}
			single(accreditationRes, "ACCTYPE", Unistats.accreditationType, RangeCallback.en);
			// Accreditation Body Id
			single(accreditationRes, "ACCBODYID", Unistats.accreditingBody, new RangeCallback() {
				@Override
				public RDFNode get(String code) throws Exception {
					Resource accbody = ResourceFactory.createResource(Unistats.getAccreditingBodyURI(code));
					model.add(accbody, RDF.type, Unistats.AccreditingBody);
					model.add(accbody, RDFS.label, AccreditationBodies.getInstance().getDescription(code));
					return accbody;
				}
			});
			single(accreditationRes, "ACCURL", Unistats.accreditationUrl, RangeCallback.anyUri);
			single(accreditationRes, "ACCDEPEND", Unistats.accreditationDependantOnChoice, RangeCallback.bool);
			single(accreditationRes, "ACCDEPENDURL", Unistats.accreditationDependantOnChoiceUrl, RangeCallback.anyUri);
			single(accreditationRes, "ACCDEPENDURLW", Unistats.accreditationDependantOnChoiceUrlW, RangeCallback.anyUri);
		}

		/**
		 * COMMON (Common Job Types) 0/3
		 */
		NodeList commonNodes = doc.getElementsByTagName("COMMON");
		for (int i = 0; i < commonNodes.getLength(); i++) {
			// we build the URI using the value in pop and agg
			Node commonNode = commonNodes.item(i);
			String pop = single(commonNode, "POP");
			String agg = single(commonNode, "AGG");
			String sbj = single(commonNode, "SBJ");
			NodeList cn = commonNode.getChildNodes();
			// iterate over each joblits item
			for (int q = 0; q < cn.getLength(); q++) {
				Node n = cn.item(q);
				if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().toUpperCase().equals("JOBLIST")) {

					// this is an observation
					Resource commonJobsResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
					
					model.add(commonJobsResource, RDF.type, Unistats.Observation);

					single(commonJobsResource, n, "JOB", Unistats.job, new RangeCallback() {
						@Override
						public RDFNode get(String value) throws Exception {
							String j = Jobs.getInstance().getCodeFromDescription(value);
							if(j==null){
								throw new UnknownValueError("Job", value);
							}
							String job = Unistats.getJobURI(j);
							Resource jobr = ResourceFactory.createResource(job);
							model.add(jobr, RDFS.label, value);
							return jobr;
						}
					});

					model.add(commonJobsResource, Unistats.course, courseResource);
					model.add(commonJobsResource, Unistats.dataSet, Unistats.commonJobs);
					model.add(commonJobsResource, Unistats.population, pop);
					model.add(commonJobsResource, Unistats.aggregation, agg);
					if (sbj != null) {
						try {
							model.add(commonJobsResource, Unistats.subject, Jacs.getRangeCallback_l2_l3().get(sbj));
						} catch (Exception e) {
							log.warn("Cannot put the value for SBJ. Was: {}. Error: {}", sbj, e.getMessage());
						}
					}

					single(commonJobsResource, n, "PERC", Unistats.percentage, RangeCallback.xsdInt);
					single(commonJobsResource, n, "ORDER", Unistats.order, RangeCallback.xsdInt);
				}
			}

			/**
			 * CONTINUATION
			 * 
			 */
			NodeList continuationNodes = doc.getElementsByTagName("CONTINUATION");
			for (int ii = 0; ii < continuationNodes.getLength(); ii++) {
				Node continuationNode = continuationNodes.item(ii);
				Resource continuationResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(continuationResource, RDF.type, Unistats.Observation);
				model.add(continuationResource, Unistats.dataSet, Unistats.continuation);
				model.add(continuationResource, Unistats.course, courseResource);
				single(continuationResource, continuationNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(continuationResource, continuationNode, "AGG", Unistats.aggregation, RangeCallback.xsdInt);
				single(continuationResource, continuationNode, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
				single(continuationResource, continuationNode, "UCONT", Unistats.continuing, RangeCallback.xsdInt);
				single(continuationResource, continuationNode, "UDORMANT", Unistats.dormant, RangeCallback.xsdInt);
				single(continuationResource, continuationNode, "UGAINED", Unistats.gained, RangeCallback.xsdInt);
				single(continuationResource, continuationNode, "ULEFT", Unistats.left, RangeCallback.xsdInt);
				single(continuationResource, continuationNode, "ULOWER", Unistats.lower, RangeCallback.xsdInt);
			}

			/**
			 * COURSESTAGE
			 */
			NodeList courseStageNodes = doc.getElementsByTagName("COURSESTAGE");
			for (int ii = 0; ii < courseStageNodes.getLength(); ii++) {
				Node courseStageNode = courseStageNodes.item(ii);
				Resource courseStageResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(courseStageResource, RDF.type, Unistats.Observation);
				model.add(courseStageResource, Unistats.dataSet, Unistats.courseStages);
				model.add(courseStageResource, Unistats.course, courseResource);
				// XXX Stages may be resources, we leave the value for the
				// moment
				single(courseStageResource, courseStageNode, "STAGE", Unistats.stage, RangeCallback.xsdInt);
				single(courseStageResource, courseStageNode, "ASSACT", Unistats.assessmentMethod, RangeCallback.xsdString);
				single(courseStageResource, courseStageNode, "COURSEWORK", Unistats.coursework, RangeCallback.xsdInt);
				single(courseStageResource, courseStageNode, "WRITTEN", Unistats.written, RangeCallback.xsdInt);
				single(courseStageResource, courseStageNode, "PRACTICAL", Unistats.practical, RangeCallback.xsdInt);
				single(courseStageResource, courseStageNode, "LTACT", Unistats.learningAndTeachingMethod, RangeCallback.xsdString);
				single(courseStageResource, courseStageNode, "INDEPENDENT", Unistats.independent, RangeCallback.xsdInt);
				single(courseStageResource, courseStageNode, "PLACEMENT", Unistats.placement, RangeCallback.xsdInt);
				single(courseStageResource, courseStageNode, "SCHEDULED", Unistats.scheduled, RangeCallback.xsdInt);
			}

			/**
			 * EMPLOYMENT
			 */
			NodeList employmentNodes = doc.getElementsByTagName("EMPLOYMENT");
			for (int ii = 0; ii < employmentNodes.getLength(); ii++) {
				Node employmentNode = employmentNodes.item(ii);
				Resource employmentResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(employmentResource, RDF.type, Unistats.Observation);
				model.add(employmentResource, Unistats.dataSet, Unistats.employment);
				model.add(employmentResource, Unistats.course, courseResource);

				single(employmentResource, employmentNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(employmentResource, employmentNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(employmentResource, employmentNode, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
				single(employmentResource, employmentNode, "WORKSTUDY", Unistats.workOrStudy, RangeCallback.xsdString);
				single(employmentResource, employmentNode, "ASSUNEMP", Unistats.assumedUnemployed, RangeCallback.xsdString);
				single(employmentResource, employmentNode, "BOTH", Unistats.workAndStudy, RangeCallback.xsdString);
				single(employmentResource, employmentNode, "NOAVAIL", Unistats.notAvailable, RangeCallback.xsdString);
				single(employmentResource, employmentNode, "STUDY", Unistats.study, RangeCallback.xsdString);
				single(employmentResource, employmentNode, "WORK", Unistats.work, RangeCallback.xsdString);
			}

			/**
			 * DEGREECLASS
			 */
			NodeList degreeClassNodes = doc.getElementsByTagName("DEGREECLASS");
			for (int ii = 0; ii < degreeClassNodes.getLength(); ii++) {
				Node degreeClassNode = degreeClassNodes.item(ii);
				Resource degreeClassResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(degreeClassResource, RDF.type, Unistats.Observation);
				model.add(degreeClassResource, Unistats.dataSet, Unistats.degreeClasses);
				model.add(degreeClassResource, Unistats.course, courseResource);

				single(degreeClassResource, degreeClassNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(degreeClassResource, degreeClassNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(degreeClassResource, degreeClassNode, "UFIRST", Unistats.firstClass, RangeCallback.xsdString);
				single(degreeClassResource, degreeClassNode, "UUPPER", Unistats.upperSecondClass, RangeCallback.xsdString);
				single(degreeClassResource, degreeClassNode, "ULOWER", Unistats.lowerSecondClass, RangeCallback.xsdString);
				single(degreeClassResource, degreeClassNode, "UOTHER", Unistats.otherHonours, RangeCallback.xsdString);
				single(degreeClassResource, degreeClassNode, "UORDINARY", Unistats.ordinary, RangeCallback.xsdString);
				single(degreeClassResource, degreeClassNode, "UNA", Unistats.notClassified, RangeCallback.xsdString);
			}

			/**
			 * ENTRY
			 */
			NodeList entryNodes = doc.getElementsByTagName("ENTRY");
			for (int ii = 0; ii < entryNodes.getLength(); ii++) {
				Node entryNode = entryNodes.item(ii);
				Resource entryResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(entryResource, RDF.type, Unistats.Observation);
				model.add(entryResource, Unistats.dataSet, Unistats.entryQualifications);
				model.add(entryResource, Unistats.course, courseResource);

				single(entryResource, entryNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(entryResource, entryNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(entryResource, entryNode, "ACCESS", Unistats.accessCourse, RangeCallback.xsdInt);
				single(entryResource, entryNode, "ALEVEL", Unistats.aLevel, RangeCallback.xsdInt);
				single(entryResource, entryNode, "BACC", Unistats.baccalaureate, RangeCallback.xsdInt);
				single(entryResource, entryNode, "DEGREE", Unistats.degree, RangeCallback.xsdInt);
				single(entryResource, entryNode, "DIPLOMA", Unistats.diploma, RangeCallback.xsdInt);
				single(entryResource, entryNode, "FOUNDATION", Unistats.foundation, RangeCallback.xsdInt);
				single(entryResource, entryNode, "NOQUALS", Unistats.noQualifications, RangeCallback.xsdInt);
				single(entryResource, entryNode, "OTHER", Unistats.other, RangeCallback.xsdInt);
				single(entryResource, entryNode, "OTHERHE", Unistats.otherHE, RangeCallback.xsdInt);

			}

			/**
			 * JOBTYPE
			 */
			NodeList jobtypeNodes = doc.getElementsByTagName("JOBTYPE");
			for (int ii = 0; ii < jobtypeNodes.getLength(); ii++) {
				Node jobtypeNode = jobtypeNodes.item(ii);
				Resource jobtypeResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(jobtypeResource, RDF.type, Unistats.Observation);
				model.add(jobtypeResource, Unistats.dataSet, Unistats.jobTypes);
				model.add(jobtypeResource, Unistats.course, courseResource);

				single(jobtypeResource, jobtypeNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(jobtypeResource, jobtypeNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(jobtypeResource, jobtypeNode, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

				single(jobtypeResource, jobtypeNode, "PROFMAN", Unistats.professional, RangeCallback.xsdInt);
				single(jobtypeResource, jobtypeNode, "OTHERJOB", Unistats.nonProfessional, RangeCallback.xsdInt);
				single(jobtypeResource, jobtypeNode, "UNKWN", Unistats.unknown, RangeCallback.xsdInt);
			}

			/**
			 * NSS
			 */
			NodeList nssNodes = doc.getElementsByTagName("NSS");
			for (int ii = 0; ii < nssNodes.getLength(); ii++) {
				Node nssNode = nssNodes.item(ii);
				Resource nssResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(nssResource, RDF.type, Unistats.Observation);
				model.add(nssResource, Unistats.dataSet, Unistats.nationalStudentSurveyResults);
				model.add(nssResource, Unistats.course, courseResource);

				single(nssResource, nssNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(nssResource, nssNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(nssResource, nssNode, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

				NodeList nssChildNodes = nssNode.getChildNodes();
				// Extracting questions Q1,Q2... Q22
				for (int q = 1; q < nssChildNodes.getLength(); q++) {
					Node qn = nssChildNodes.item(q);
					String nn = qn.getNodeName();
					if (nn.startsWith("Q")) {
						try {
							Field f = Unistats.class.getField("nssQuestion" + nn.substring(1));
							String value = qn.getTextContent();
							Property quri = (Property) f.get(null);
							model.add(nssResource, quri, ResourceFactory.createTypedLiteral(new Integer(value)));
						} catch (Exception e) {
							log.warn("Cannot extract value of nss question. Skipping. {}:{}", e.getClass(), e.getMessage());
						}
					}
				}
			}

			/**
			 * NHS NSS
			 */
			NodeList nhsnssNodes = doc.getElementsByTagName("NHSNSS");

			for (int ii = 0; ii < nhsnssNodes.getLength(); ii++) {
				Node nhsnssNode = nhsnssNodes.item(ii);
				Resource nhsnssResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(nhsnssResource, RDF.type, Unistats.Observation);
				model.add(nhsnssResource, Unistats.dataSet, Unistats.nationalStudentSurveyNHSResults);
				model.add(nhsnssResource, Unistats.course, courseResource);

				single(nhsnssResource, nhsnssNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(nhsnssResource, nhsnssNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(nhsnssResource, nhsnssNode, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

				NodeList nhsnssChildNodes = nhsnssNode.getChildNodes();
				// Extracting questions NSHQ1,NSHQ2... NSHQ6
				for (int q = 1; q < nhsnssChildNodes.getLength(); q++) {
					Node qn = nhsnssChildNodes.item(q);
					String nn = qn.getNodeName();
					if (nn.startsWith("NHSQ")) {
						try {
							Field f = Unistats.class.getField("nhsnssQuestion" + nn.substring(4));
							String value = qn.getTextContent();
							Property quri = (Property) f.get(null);
							model.add(nhsnssResource, quri, ResourceFactory.createTypedLiteral(new Integer(value)));
						} catch (Exception e) {
							log.warn("Cannot extract value of nss (nhs) question. Skipping. {}:{}", e.getClass(), e.getMessage());
						}
					}
				}
			}

			/**
			 * SALARY
			 */
			NodeList salaryNodes = doc.getElementsByTagName("SALARY");
			for (int ii = 0; ii < salaryNodes.getLength(); ii++) {
				Node salaryNode = salaryNodes.item(ii);
				Resource salaryResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(salaryResource, RDF.type, Unistats.Observation);
				model.add(salaryResource, Unistats.dataSet, Unistats.salaries);
				model.add(salaryResource, Unistats.course, courseResource);

				single(salaryResource, salaryNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(salaryResource, salaryNode, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

				single(salaryResource, salaryNode, "LDLQ", Unistats.sectorLowerForty, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "LDMED", Unistats.sectorMedianForty, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "LDUQ", Unistats.sectorUpperForty, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "LQ", Unistats.sectorLowerSix, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "MED", Unistats.sectorMedianSix, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "UQ", Unistats.sectorUpperSix, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "INSTLQ", Unistats.courseLower, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "INSTMED", Unistats.courseMedian, RangeCallback.xsdInt);
				single(salaryResource, salaryNode, "INSTUQ", Unistats.courseUpper, RangeCallback.xsdInt);
			}

			/**
			 * TARIFF
			 */
			NodeList tariffNodes = doc.getElementsByTagName("TARIFF");
			for (int ii = 0; ii < tariffNodes.getLength(); ii++) {
				Node tariffNode = tariffNodes.item(ii);
				Resource tariffResource = ResourceFactory.createResource(Unistats.createSkolemizedResource());
				model.add(tariffResource, RDF.type, Unistats.Observation);
				model.add(tariffResource, Unistats.dataSet, Unistats.tariffs);
				model.add(tariffResource, Unistats.course, courseResource);

				single(tariffResource, tariffNode, "POP", Unistats.population, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "AGG", Unistats.aggregation, RangeCallback.xsdString);
				single(tariffResource, tariffNode, "T1", Unistats.tariff1, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T120", Unistats.tariff120, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T160", Unistats.tariff160, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T200", Unistats.tariff200, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T240", Unistats.tariff240, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T280", Unistats.tariff280, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T320", Unistats.tariff320, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T360", Unistats.tariff360, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T400", Unistats.tariff400, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T440", Unistats.tariff440, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T480", Unistats.tariff480, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T520", Unistats.tariff520, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T560", Unistats.tariff560, RangeCallback.xsdInt);
				single(tariffResource, tariffNode, "T600", Unistats.tariff600, RangeCallback.xsdInt);
			}
		}
	}

	public Model getModel() {
		if (model == null) {
			buildRDFModel();
		}
		return model;
	}
}
