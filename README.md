# Kis-Kit
This tool converts Unistats XML data to Linked Data.
## The Ontology
The schema is mostly based on the W3C RDF Data Cube Vocabulary http://www.w3.org/TR/vocab-data-cube/

Namespace of the schema is http://data.linkedu.eu/kis/ontology/

The Turtle file is embedded in the code, it can be accessed at this link: https://raw.githubusercontent.com/the-open-university/kiskit/master/src/uk/ac/open/data/kiskit/v002/vocab/vocabulary.ttl

## The Data
The data translation takes into account not only the XML included in the downloadable package, but also reference data like the JACS codes published by the University of Southampton (http://data.southampton.ac.uk/dumps/jacs/2014-02-21/jacs.ttl). In addition, the process includes the generation of links to known entities, like the URIs of UKPRN institutions published at http://learning-providers.data.ac.uk.

Data URIs are build using the namespace http://data.linkedu.eu/kis/

The resulting RDF includes description of data sets following the RDF Data Cube Vocabulary, but also embed well-formed documentation: 

 - all resources have at least one langed rdfs:label; 
 - all resources have a single rdfs:comment; 
 - all resources have a single skos:prefLabel; 
 - schema elements have rdfs:isDefinedBy pointing to the Unistats vocabulary specification (the ontology above, not yet online - TODO); 
 - resources include link to HESA documentation using rdfs:seeAlso.

The Unistats Linked Data contains almost 7.140.000 triples with approximately 325.000 qb:Observations in 11 qb:DataSets.

The following SPARQL query lists the data sets with the number of observations:
```   
prefix qb: <http://purl.org/linked-data/cube#> 
select ?URI ?Name (COUNT(DISTINCT ?o) as ?Observation)
where {
?URI a qb:DataSet ;
         skos:prefLabel ?Name .
?o qb:dataSet ?URI .
} 
GROUP BY ?URI ?Name 
ORDER BY DESC(?Observation)	
```
## Build
This is a Java program.
To build the project you need also http://ant.apache.org/. 

Having ant in place, run the following command from the project main directory:
```
ant build jar
```
## Usage
### How to obtain the Unistats data
To download the latest unistats archive go to this page: 

http://www.hesa.ac.uk/index.php?option=com_content&task=view&id=2609

and click the button "Accept".

Unpack the archive. 

### Run the extraction
To extract the data in full:
```
java -jar kiskit-<version>.jar <inputDir> <outputFile>
```
To only extract data about a single institution
```
java -jar kiskit-<version>.jar <inputDir> <outputFile> <ukprn>
```
The program generates an RDF file in N-Triples format.

## Run the Old Version (2012)
By default the tool expects an XML respecting the HESA Unistats spec C13061 published on Sept 2013.
However, the code includes also packages to translate the previous version, spec C12061 with data about year 2012.

The code is split in different sub-packages:
 * v002 is targeted to C13061, main class is UnistatsToNTriple002
 * v001 is targeted to C12061, main class is UnistatsToNTriple001

Use different main classes to extract different dumps, as follow.





