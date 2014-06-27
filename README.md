# Kis-Kit
This tool converts Unistats XML data to Linked Data.

## Usage
### How to obtain the Unistats data
To download the latest unistats archive go to this page: 

http://www.hesa.ac.uk/index.php?option=com_content&task=view&id=2609

and click the button "Accept".

Extract data from the archive. The file we need is called kis<timestamp>.xml

### Run the extraction
To extract the data in full:
	java -jar kiskit-<version>.jar <inputFile> <outputFile>
To only extract data about a single institution
	java -jar kiskit-<version>.jar <inputFile> <outputFile> <ukprn>

### Resources
See also: http://www.hesa.ac.uk/index.php?option=com_studrec&Itemid=232&mnl=13061

1) JACS codes:
Southampton JACS codes (rdf file embedded)
uk.ac.open.data.kiskit.vXXX.data/UNISTATS_subject_aggregation_lookup.xls
From http://www.hesa.ac.uk/index.php?option=com_content&task=view&id=2609 
  
2) Accreditation bodies
uk.ac.open.data.kiskit.vXXX.data/ACCBODYIDs.csv
See http://www.hesa.ac.uk/index.php?option=com_studrec&task=show_file&Itemid=233&mnl=12061&href=A^_^ACCBODYID.html

3) SOC Job Categories: 
SOC_JOB_CATEGORIES.csv (embedded)
Job categories

4) Institutions descriptions
UNISTATS_UKPRN_lookup.csv (embedded)


-----------------------------------
Vocabulary
http://data.linkedu.eu/kis/ontology/
http://data.linkedu.eu/kis/


002 is targeted to C13061
001 is targeted to C12061

# Run Old Versions
Use different main classes to extract different dumps, as follow:
- unistats_2013_05_07_06_03_44 -> UnistatsToNTriple001





