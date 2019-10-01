# Evaluation Queries for MIE 2020 Submission

## Q1:  patients prescribed Fluoxetine (SNOMED CT code: 321949006)
SPARQL:
```SPARQL
PREFIX : <http://www.hdruk.ac.uk/sprint/graph/> 
PREFIX test: 	<http://www.hdruk.ac.uk/sprint/graph/ontology/test/> 
PREFIX onto: 	<http://www.hdruk.ac.uk/sprint/graph/ontology/>
PREFIX icd: <http://www.hdruk.ac.uk/sprint/graph/ontology/ICD/> 
PREFIX patient: <http://www.hdruk.ac.uk/sprint/graph/patient/> 
PREFIX RCTV2: <http://purl.bioontology.org/ontology/RCTV2/>
PREFIX bnf: <http://www.hdruk.ac.uk/sprint/graph/ontology/bnf/> 
PREFIX snomed: <http://www.hdruk.ac.uk/sprint/graph/ontology/snomed/> 

 SELECT DISTINCT * 
 WHERE { 
   ?x onto:hasPrescription ?p .
   ?p a snomed:321949006 .
 }
```
SQL:
```SQL
select CHI from graph.Prescription where BnfCode='0403030E0AAAAAA'
```
