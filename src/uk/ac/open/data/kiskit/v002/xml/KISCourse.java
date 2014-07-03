package uk.ac.open.data.kiskit.v002.xml;

import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.many;
import static uk.ac.open.data.kiskit.v002.utils.XML2ModelUtils.single;

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

import uk.ac.open.data.kiskit.v002.data.Jobs;
import uk.ac.open.data.kiskit.v002.jacs.Jacs;
import uk.ac.open.data.kiskit.v002.utils.RangeCallback;
import uk.ac.open.data.kiskit.v002.vocab.SKOS;
import uk.ac.open.data.kiskit.v002.vocab.Unistats;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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

	private void buildRDFModel() {

		model = ModelFactory.createDefaultModel();
		String kisCourseId = doc.getElementsByTagName("KISCOURSEID").item(0).getTextContent();
		log.debug("Scanning course {} @ {}", ukprn, kisCourseId);
		final Resource courseResource = model.createResource(Unistats.getCourseURI(ukprn, kisCourseId,  ns_COURSE_BASE));

		model.add(courseResource, RDF.type, Unistats.Course);
		model.add(courseResource, RDF.type, Unistats.RKBCourse);
		model.add(courseResource, RDF.type, Unistats.AIISOCourse);
		model.add(courseResource, Unistats.taughtAt, ResourceFactory.createResource(Unistats.getInstitutionURI(ukprn)));

		// FIELDS
		single(doc, courseResource, "KISCOURSEID", Unistats.courseId, RangeCallback.xsdString);
		//single(doc, courseResource, "KISTYPE", Unistats.type, RangeCallback.xsdInt);
		single(doc, courseResource, "KISTYPE", Unistats.type, new RangeCallback() {
			
			@Override
			public RDFNode get(String value) throws Exception {
				Resource ct = null;
				Integer val = new Integer(value);
				if (val == 1) {
					ct = Unistats.FullCourseLevelKIS;
				} else if (val == 2) {
					ct = Unistats.MultipleSubjectCoursePlaceholderKIS;
				} else if (val == 3) {
					ct = Unistats.SubjectLevelKIS;
				} else {
					log.error("Unsupported value for type: {}", val);
				}

				// course
				if(ct!=null){
					model.add(courseResource, RDF.type, ct);
				}
				// We keep the original value for the property
				return ResourceFactory.createTypedLiteral(val);
			}
		});
		single(doc, courseResource, "LOCCHNGE", Unistats.locationChange, RangeCallback.xsdInt);

		many(doc, courseResource, "RELATEDKIS", Unistats.relatedCourses, new RangeCallback() {
			@Override
			public RDFNode get(String value) throws Exception {
				return ResourceFactory.createResource(Unistats.getCourseURI(ukprn, value,  ns_COURSE_BASE));
			}
		});
		many(doc, courseResource, "UKPRNAPPLY", Unistats.ukPrnApply, RangeCallback.xsdString);

		many(doc, courseResource, "LDCS", Unistats.ldcsCode, RangeCallback.xsdString);

		many(doc, courseResource, "JACS", Unistats.jacsCode, Jacs.getRangeCallback_2_0());
		single(doc, courseResource, "TITLE", SKOS.prefLabel, RangeCallback.xsdString);
		single(doc, courseResource, "TITLE", Unistats.title, RangeCallback.en);
		single(doc, courseResource, "TITLE", RDFS.label, RangeCallback.en);
		single(doc, courseResource, "TITLEW", Unistats.title, RangeCallback.cy);
		single(doc, courseResource, "TITLEW", RDFS.label, RangeCallback.cy);

		single(doc, courseResource, "CRSEURL", Unistats.courseUrl, RangeCallback.anyUri);
		single(doc, courseResource, "CRSEURLW", Unistats.courseUrlW, RangeCallback.anyUri);
		single(doc, courseResource, "SUPPORTURL", Unistats.supportUrl, RangeCallback.anyUri);
		single(doc, courseResource, "SUPPORTURLW", Unistats.supportUrlW, RangeCallback.anyUri);
		single(doc, courseResource, "NONCREDITASSESS", Unistats.nonCreditAssessment, RangeCallback.bool);
		single(doc, courseResource, "LTURL", Unistats.learningAndTeachingUrl, RangeCallback.anyUri);
		single(doc, courseResource, "LTURLW", Unistats.learningAndTeachingUrlW, RangeCallback.anyUri);
		single(doc, courseResource, "EMPLOYURL", Unistats.employUrl, RangeCallback.anyUri);
		single(doc, courseResource, "EMPLOYURLW", Unistats.employUrlW, RangeCallback.anyUri);
		single(doc, courseResource, "ASSURL", Unistats.assessmentMethodsUrl, RangeCallback.anyUri);
		single(doc, courseResource, "ASSURLW", Unistats.assessmentMethodsUrlW, RangeCallback.anyUri);
		single(doc, courseResource, "HONOURS", Unistats.honoursDegreeCourse, RangeCallback.bool);
		
		single(doc, courseResource, "KISAIMCODE", Unistats.aim, new RangeCallback() {
			
			@Override
			public RDFNode get(String value) throws Exception {
				return ResourceFactory.createResource(Unistats.getAimURI(value));
			}
		});
		single(doc, courseResource, "LOCID", Unistats.aim, new RangeCallback() {
			
			@Override
			public RDFNode get(String value) throws Exception {
				return ResourceFactory.createResource(Unistats.getLocationURI(value));
			}
		});
		
		// KISKEY No longer used
		single(doc, courseResource, "UCASCOURSEID", Unistats.ucasCourseId, RangeCallback.xsdString);
		single(doc, courseResource, "VARFEE", Unistats.feeVariesByYear, new RangeCallback() {
			@Override
			public RDFNode get(String value) throws Exception {
				return ResourceFactory.createResource(Unistats.getFeeMarkerURI(value));
			}
		});
		single(doc, courseResource, "SANDWICH", Unistats.sandwitch, RangeCallback.bool);
		single(doc, courseResource, "YEARABROAD", Unistats.yearAbroadAvailable, RangeCallback.bool);
		single(doc, courseResource, "FEETBC", Unistats.feeToBeConfirmed, RangeCallback.bool);
		single(doc, courseResource, "DISTANCE", Unistats.distanceOnly, RangeCallback.bool);
		single(doc, courseResource, "WAIVER", Unistats.waiverAvailable, RangeCallback.bool);
		single(doc, courseResource, "MEANSSUP", Unistats.meansSupport, RangeCallback.bool);
		single(doc, courseResource, "OTHSUP", Unistats.otherSupport, RangeCallback.bool);
		single(doc, courseResource, "ENGFEE", Unistats.englishFee, RangeCallback.xsdInt);
		single(doc, courseResource, "SCOTFEE", Unistats.scottishFee, RangeCallback.xsdInt);
		single(doc, courseResource, "NIFEE", Unistats.northernIrishFee, RangeCallback.xsdInt);
		single(doc, courseResource, "WAFEE", Unistats.walesFee, RangeCallback.xsdInt);
		single(doc, courseResource, "WELSH", Unistats.availableInWelsh, RangeCallback.xsdInt);
		single(doc, courseResource, "KISMODE", Unistats.mode, new RangeCallback() {
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
		single(doc, courseResource, "LEVEL", Unistats.awardLevel, new RangeCallback(){
			@Override
			public RDFNode get(String value) throws Exception {
				return (value.equals("F") ? Unistats.firstDegreeAward : Unistats.otherUndergraduateAward);
			}
		});
		single(doc, courseResource, "AVGWRITTEN", Unistats.averageOfWrittenAssessment, RangeCallback.xsdInt);
		single(doc, courseResource, "AVGCOURSEWORK", Unistats.averageOfCourseworkAssessment, RangeCallback.xsdInt);
		single(doc, courseResource, "AVGSCHEDULED", Unistats.averageOfScheduledActivities, RangeCallback.xsdInt);

		// XXX There is no clear meaning of KISCOURSE/SBJ,
		// documentation is incomplete and inconsistent with actual values
		// we adopted a set of strategies considering that this
		// should refer to jacs levels 2 or 3
		many(doc, courseResource, "SBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
		log.debug(" > {} triples so far", model.size());
		/**
		 * KISCOURSE/ACCREDITATION (s)
		 */
		NodeList accreditationsNodeList = doc.getElementsByTagName("ACCREDITATION");
		log.debug(" > ACCREDITATION: {}", accreditationsNodeList.getLength());
		for (int x = 1; x < accreditationsNodeList.getLength() + 1; x++) {
			//Resource accreditationRes = ResourceFactory.createResource(Unistats.ns_ACCREDITATION + ukprn + "/" + kisCourseId.toLowerCase() + "/" + x);
			Resource accreditationRes = model.createResource(Unistats.getAccreditationURI( ukprn , kisCourseId, x));
			model.add(courseResource, Unistats.accreditation, accreditationRes);

			// about the accreditation
			model.add(accreditationRes, RDF.type, Unistats.Accreditation);
			try {
				String albl = "Accreditation of: " + courseResource.getProperty(Unistats.title).getLiteral().getString();
				model.add(accreditationRes, RDFS.label, model.createLiteral(albl, "en"));
				model.add(accreditationRes, SKOS.prefLabel, model.createLiteral(albl));
			} catch (Exception e) {
				log.warn("Cannot produce label for {}", accreditationRes);
			}
			single(doc, accreditationRes, "ACCTYPE", Unistats.accreditationType, new RangeCallback() {
				@Override
				public RDFNode get(String value) throws Exception {
					return model.createResource(Unistats.getAccreditationTypeURI(value));
				}
			});
			single(doc, accreditationRes, "ACCDEPEND", Unistats.accreditationDependantOnChoice, RangeCallback.bool);
			single(doc, accreditationRes, "ACCDEPENDURL", Unistats.accreditationDependantOnChoiceUrl, RangeCallback.anyUri);
			single(doc, accreditationRes, "ACCDEPENDURLW", Unistats.accreditationDependantOnChoiceUrlW, RangeCallback.anyUri);
		}
		log.debug(" > {} triples so far", model.size());
		/**
		 * COMMON (Common Job Types) 0/3
		 */
		NodeList commonNodes = doc.getElementsByTagName("COMMON");
		log.debug(" > COMMON: {}", commonNodes.getLength());
		for (int i = 0; i < commonNodes.getLength(); i++) {
			// we build the URI using the value in pop and agg
			Node commonNode = commonNodes.item(i);
			String pop = single(commonNode, "COMPOP"); // changed from 001
			String agg = single(commonNode, "COMAGG"); // changed from 001
			String sbj = single(commonNode, "COMSBJ");
			NodeList cn = commonNode.getChildNodes();
			// iterate over each joblits item
			for (int q = 0; q < cn.getLength(); q++) {
				Node n = cn.item(q);
				if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().toUpperCase().equals("JOBLIST")) {

					// this is an observation
					Resource commonJobsResource = model.createResource(Unistats.getObservationURI(Unistats.commonJobs, ukprn, kisCourseId, pop, agg, sbj));
					observationPreferredLabel(commonJobsResource);
					model.add(commonJobsResource, RDF.type, Unistats.Observation);
					model.add(commonJobsResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
					
					single(commonJobsResource, n, "JOB", Unistats.job, new RangeCallback() {
						@Override
						public RDFNode get(String value) throws Exception {
							String j = Jobs.getInstance().getCodeFromDescription(value);
							if(j==null){
								// We support also non-soc jobs
								// Strangely this  is not documented in 2013/2014
								//throw new UnknownValueError("Job", value);
								j = value;
							}
							String job = Unistats.getJobURI(j);
							Resource jobr = ResourceFactory.createResource(job);
							model.add(jobr, RDF.type, Unistats.Job);
							model.add(jobr, RDFS.label, value, "en");
							model.add(jobr, SKOS.prefLabel, value);
							return jobr;
						}
					});

					model.add(commonJobsResource, Unistats.course, courseResource);
					model.add(commonJobsResource, Unistats.dataSet, Unistats.commonJobs);
					model.add(commonJobsResource, Unistats.population, pop);
					model.add(commonJobsResource, Unistats.aggregation, agg, XSDDatatype.XSDstring);
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
		}
		log.debug(" > {} triples so far", model.size());
		
		/**
		 * CONTINUATION
		 * 
		 */
		NodeList continuationNodes = doc.getElementsByTagName("CONTINUATION");
		log.debug(" > CONTINUATION: {}", continuationNodes.getLength());
		for (int ii = 0; ii < continuationNodes.getLength(); ii++) {
			Node continuationNode = continuationNodes.item(ii);
			Resource continuationResource = model.createResource(Unistats.getObservationURI(Unistats.continuation, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(continuationResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(continuationResource);
			model.add(continuationResource, RDF.type, Unistats.Observation);
			model.add(continuationResource, Unistats.dataSet, Unistats.continuation);
			model.add(continuationResource, Unistats.course, courseResource);
			single(continuationResource, continuationNode, "CONTPOP", Unistats.population, RangeCallback.xsdInt);
			single(continuationResource, continuationNode, "CONTAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(continuationResource, continuationNode, "CONTSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
			single(continuationResource, continuationNode, "UCONT", Unistats.continuing, RangeCallback.xsdInt);
			single(continuationResource, continuationNode, "UDORMANT", Unistats.dormant, RangeCallback.xsdInt);
			single(continuationResource, continuationNode, "UGAINED", Unistats.gained, RangeCallback.xsdInt);
			single(continuationResource, continuationNode, "ULEFT", Unistats.left, RangeCallback.xsdInt);
			single(continuationResource, continuationNode, "ULOWER", Unistats.lower, RangeCallback.xsdInt);
		}
		log.debug(" > {} triples so far", model.size());
		
		// NUMSTAGE
		single(doc, courseResource, "NUMSTAGE", Unistats.numStage, RangeCallback.xsdInt);
		
		/**
		 * COURSESTAGE
		 */
		NodeList courseStageNodes = doc.getElementsByTagName("COURSESTAGE");
		log.debug(" > COURSESTAGE: {}", courseStageNodes.getLength());
		for (int ii = 0; ii < courseStageNodes.getLength(); ii++) {
			Node courseStageNode = courseStageNodes.item(ii);
			Resource courseStageResource = model.createResource(Unistats.getObservationURI(Unistats.courseStages, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(courseStageResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(courseStageResource);
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
		log.debug(" > {} triples so far", model.size());

		/**
		 * EMPLOYMENT
		 */
		NodeList employmentNodes = doc.getElementsByTagName("EMPLOYMENT");
		log.debug(" > EMPLOYMENT: {}", employmentNodes.getLength());
		for (int ii = 0; ii < employmentNodes.getLength(); ii++) {
			Node employmentNode = employmentNodes.item(ii);
			Resource employmentResource = model.createResource(Unistats.getObservationURI(Unistats.employment, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(employmentResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(employmentResource);
			model.add(employmentResource, RDF.type, Unistats.Observation);
			model.add(employmentResource, Unistats.dataSet, Unistats.employment);
			model.add(employmentResource, Unistats.course, courseResource);

			single(employmentResource, employmentNode, "EMPPOP", Unistats.population, RangeCallback.xsdInt);
			single(employmentResource, employmentNode, "ENPAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(employmentResource, employmentNode, "EMPSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
			single(employmentResource, employmentNode, "WORKSTUDY", Unistats.workOrStudy, RangeCallback.xsdInt);
			single(employmentResource, employmentNode, "ASSUNEMP", Unistats.assumedUnemployed, RangeCallback.xsdInt);
			single(employmentResource, employmentNode, "BOTH", Unistats.workAndStudy, RangeCallback.xsdInt);
			single(employmentResource, employmentNode, "NOAVAIL", Unistats.notAvailable, RangeCallback.xsdInt);
			single(employmentResource, employmentNode, "STUDY", Unistats.study, RangeCallback.xsdInt);
			single(employmentResource, employmentNode, "WORK", Unistats.work, RangeCallback.xsdInt);
		}
		log.debug(" > {} triples so far", model.size());

		/**
		 * DEGREECLASS
		 */
		NodeList degreeClassNodes = doc.getElementsByTagName("DEGREECLASS");
		log.debug(" > DEGREECLASS: {}", degreeClassNodes.getLength());
		for (int ii = 0; ii < degreeClassNodes.getLength(); ii++) {
			Node degreeClassNode = degreeClassNodes.item(ii);
			Resource degreeClassResource = model.createResource(Unistats.getObservationURI(Unistats.degreeClasses, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(degreeClassResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(degreeClassResource);
			model.add(degreeClassResource, RDF.type, Unistats.Observation);
			model.add(degreeClassResource, Unistats.dataSet, Unistats.degreeClasses);
			model.add(degreeClassResource, Unistats.course, courseResource);

			single(degreeClassResource, degreeClassNode, "DEGPOP", Unistats.population, RangeCallback.xsdInt);
			single(degreeClassResource, degreeClassNode, "DEGAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(degreeClassResource, degreeClassNode, "DEGSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
			single(degreeClassResource, degreeClassNode, "UFIRST", Unistats.firstClass, RangeCallback.xsdInt);
			single(degreeClassResource, degreeClassNode, "UUPPER", Unistats.upperSecondClass, RangeCallback.xsdInt);
			single(degreeClassResource, degreeClassNode, "ULOWER", Unistats.lowerSecondClass, RangeCallback.xsdInt);
			single(degreeClassResource, degreeClassNode, "UOTHER", Unistats.otherHonours, RangeCallback.xsdInt);
			single(degreeClassResource, degreeClassNode, "UORDINARY", Unistats.ordinary, RangeCallback.xsdInt);
			single(degreeClassResource, degreeClassNode, "UNA", Unistats.notClassified, RangeCallback.xsdInt);
		}
		log.debug(" > {} triples so far", model.size());

		/**
		 * ENTRY
		 */
		NodeList entryNodes = doc.getElementsByTagName("ENTRY");
		log.debug(" > ENTRY: {}", entryNodes.getLength());
		for (int ii = 0; ii < entryNodes.getLength(); ii++) {
			Node entryNode = entryNodes.item(ii);
			Resource entryResource = model.createResource(Unistats.getObservationURI(Unistats.entryQualifications, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(entryResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(entryResource);
			model.add(entryResource, RDF.type, Unistats.Observation);
			model.add(entryResource, Unistats.dataSet, Unistats.entryQualifications);
			model.add(entryResource, Unistats.course, courseResource);

			single(entryResource, entryNode, "ENTPOP", Unistats.population, RangeCallback.xsdInt);
			single(entryResource, entryNode, "ENTAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(entryResource, entryNode, "ENTSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
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
		log.debug(" > {} triples so far", model.size());

		/**
		 * JOBTYPE
		 */
		NodeList jobtypeNodes = doc.getElementsByTagName("JOBTYPE");
		log.debug(" > JOBTYPE: {}", jobtypeNodes.getLength());
		for (int ii = 0; ii < jobtypeNodes.getLength(); ii++) {
			Node jobtypeNode = jobtypeNodes.item(ii);
			Resource jobtypeResource = model.createResource(Unistats.getObservationURI(Unistats.jobTypes, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(jobtypeResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(jobtypeResource);
			model.add(jobtypeResource, RDF.type, Unistats.Observation);
			model.add(jobtypeResource, Unistats.dataSet, Unistats.jobTypes);
			model.add(jobtypeResource, Unistats.course, courseResource);

			single(jobtypeResource, jobtypeNode, "JOBPOP", Unistats.population, RangeCallback.xsdInt);
			single(jobtypeResource, jobtypeNode, "JOBAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(jobtypeResource, jobtypeNode, "JOBSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
			single(jobtypeResource, jobtypeNode, "PROFMAN", Unistats.professional, RangeCallback.xsdInt);
			single(jobtypeResource, jobtypeNode, "OTHERJOB", Unistats.nonProfessional, RangeCallback.xsdInt);
			single(jobtypeResource, jobtypeNode, "UNKWN", Unistats.unknown, RangeCallback.xsdInt);
		}
		log.debug(" > {} triples so far", model.size());

		/**
		 * NSS
		 */
		NodeList nssNodes = doc.getElementsByTagName("NSS");
		log.debug(" > NSS: {}", nssNodes.getLength());
		for (int ii = 0; ii < nssNodes.getLength(); ii++) {
			Node nssNode = nssNodes.item(ii);
			Resource nssResource = model.createResource(Unistats.getObservationURI(Unistats.nationalStudentSurveyResults, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(nssResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(nssResource);
			model.add(nssResource, RDF.type, Unistats.Observation);
			model.add(nssResource, Unistats.dataSet, Unistats.nationalStudentSurveyResults);
			model.add(nssResource, Unistats.course, courseResource);

			single(nssResource, nssNode, "NSSPOP", Unistats.population, RangeCallback.xsdInt);
			single(nssResource, nssNode, "NSSAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(nssResource, nssNode, "NSSSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

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
		log.debug(" > {} triples so far", model.size());

		// NHS
		single(doc, courseResource, "NHS", Unistats.nhsFundedStudents, new RangeCallback(){
			@Override
			public RDFNode get(String value) throws Exception {
				return RangeCallback.bool.get(value.equals("1") ? "True" : "False");
			}
		});
		
		/**
		 * NHS NSS
		 */
		NodeList nhsnssNodes = doc.getElementsByTagName("NHSNSS");
		log.debug(" > NHSNSS: {}", nhsnssNodes.getLength());
		for (int ii = 0; ii < nhsnssNodes.getLength(); ii++) {
			Node nhsnssNode = nhsnssNodes.item(ii);
			Resource nhsnssResource = model.createResource(Unistats.getObservationURI(Unistats.nationalStudentSurveyNHSResults, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(nhsnssResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(nhsnssResource);
			model.add(nhsnssResource, RDF.type, Unistats.Observation);
			model.add(nhsnssResource, Unistats.dataSet, Unistats.nationalStudentSurveyNHSResults);
			model.add(nhsnssResource, Unistats.course, courseResource);

			single(nhsnssResource, nhsnssNode, "NHSPOP", Unistats.population, RangeCallback.xsdInt);
			single(nhsnssResource, nhsnssNode, "NHSAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(nhsnssResource, nhsnssNode, "NHSSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

			NodeList nhsnssChildNodes = nhsnssNode.getChildNodes();
			// Extracting questions NSHQ1,NSHQ2... NSHQ6
			for (int q = 1; q < nhsnssChildNodes.getLength(); q++) {
				Node qn = nhsnssChildNodes.item(q);
				String nn = qn.getNodeName();
				if (nn.startsWith("NHSQ")) {
					try {
						Field f = Unistats.class.getField("nhsNssQuestion" + nn.substring(4));
						String value = qn.getTextContent();
						Property quri = (Property) f.get(null);
						model.add(nhsnssResource, quri, ResourceFactory.createTypedLiteral(new Integer(value)));
					} catch (Exception e) {
						log.warn("Cannot extract value of nss (nhs) question. Skipping. {}:{}", e.getClass(), e.getMessage());
					}
				}
			}
		}
		log.debug(" > {} triples so far", model.size());

		/**
		 * SALARY
		 */
		NodeList salaryNodes = doc.getElementsByTagName("SALARY");
		log.debug(" > SALARY: {}", salaryNodes.getLength());
		for (int ii = 0; ii < salaryNodes.getLength(); ii++) {
			Node salaryNode = salaryNodes.item(ii);
			Resource salaryResource = model.createResource(Unistats.getObservationURI(Unistats.salaries, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(salaryResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(salaryResource);
			model.add(salaryResource, RDF.type, Unistats.Observation);
			model.add(salaryResource, Unistats.dataSet, Unistats.salaries);
			model.add(salaryResource, Unistats.course, courseResource);

			single(salaryResource, salaryNode, "SALPOP", Unistats.population, RangeCallback.xsdInt);
			single(salaryResource, salaryNode, "SALAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(salaryResource, salaryNode, "SALSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());

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
		log.debug(" > {} triples so far", model.size());

		/**
		 * TARIFF
		 */
		NodeList tariffNodes = doc.getElementsByTagName("TARIFF");
		log.debug(" > TARIFF: {}", tariffNodes.getLength());
		for (int ii = 0; ii < tariffNodes.getLength(); ii++) {
			Node tariffNode = tariffNodes.item(ii);
			Resource tariffResource = model.createResource(Unistats.getObservationURI(Unistats.tariffs, ukprn, kisCourseId, Integer.toString(ii)));
			model.add(tariffResource, Unistats.institution, model.createResource(Unistats.getInstitutionURI(ukprn)));
			observationPreferredLabel(tariffResource);
			model.add(tariffResource, RDF.type, Unistats.Observation);
			model.add(tariffResource, Unistats.dataSet, Unistats.tariffs);
			model.add(tariffResource, Unistats.course, courseResource);

			single(tariffResource, tariffNode, "TARPOP", Unistats.population, RangeCallback.xsdInt);
			single(tariffResource, tariffNode, "TARAGG", Unistats.aggregation, RangeCallback.xsdString);
			single(tariffResource, tariffNode, "TARSBJ", Unistats.subject, Jacs.getRangeCallback_l2_l3());
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
		log.debug(" > {} triples so far", model.size());
	}

	private void observationPreferredLabel(Resource resource) {
		resource.addLiteral(SKOS.prefLabel, new StringBuilder("Observation #").append(resource.getLocalName()).toString());
	}

	public Model getModel() {
		if (model == null) {
			buildRDFModel();
		}
		return model;
	}
}
