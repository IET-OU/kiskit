# Kis-Kit
This tool converts Unistats XML data to Linked Data.
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

Extract data from the archive. The file we need is called kis<timestamp>.xml

### Run the extraction
To extract the data in full:
```
java -jar kiskit-<version>.jar <inputFile> <outputFile>
```
To only extract data about a single institution
```
java -jar kiskit-<version>.jar <inputFile> <outputFile> <ukprn>
```
## The Data
The schema is mostly based on the W3C RDF Data Cube Vocabulary http://www.w3.org/TR/vocab-data-cube/

Namespace of the schema is http://data.linkedu.eu/kis/ontology/

Data URIs are build from http://data.linkedu.eu/kis/

## Run the Old Version (2012)
By default the tool expects an XML respecting the HESA Unistats spec C13061 published on Sept 2013.
However, the code includes also packages to translate the previous version, spec C12061 with data about year 2012.

The code is split in different sub-packages:
* v002 is targeted to C13061, main class is UnistatsToNTriple002
* v001 is targeted to C12061, main class is UnistatsToNTriple001

Use different main classes to extract different dumps, as follow.





