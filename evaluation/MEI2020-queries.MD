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

## Q2: patients diagnosed as Type II Diabetes (ICD10 E11 and its sub-codes)
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
   ?x a icd:E11 .
 }
```

```SQL
select CHI from graph.Admissions where maincondition like 'E11%' or maincondition in 
('-25000', '-25002', '-25010', '-25012', '-25020', '-25022', '-25030', '-25032', '-25040', '-25042', '-25050', '-25052', '-25060', '-25062', '-25070', '-25072', '-25080', '-25082', '-25090', '-25092')
```
