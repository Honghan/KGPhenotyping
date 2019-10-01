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
   ?x a icd:E11 .
 }
```
SQL:
```SQL
select CHI from graph.HospitalAdmissions where maincondition like 'E11%' or maincondition in 
('-25000', '-25002', '-25010', '-25012', '-25020', '-25022', '-25030', '-25032', '-25040', '-25042', '-25050', '-25052', '-25060', '-25062', '-25070', '-25072', '-25080', '-25082', '-25090', '-25092')
```

##Q3: patients have potential Diabetes coding errors
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
   ?x a onto:DiabetesConflictPatient .
 }
```
SQL:
```SQL
select h1.CHI from graph.HospitalAdmissions h1,  graph.HospitalAdmissions h2 where (h1.maincondition like 'E11%' or h1.maincondition in 
('-25000', '-25002', '-25010', '-25012', '-25020', '-25022', '-25030', '-25032', '-25040', '-25042', '-25050', '-25052', '-25060', '-25062', '-25070', '-25072', '-25080', '-25082', '-25090', '-25092'))
and 
(h2.maincondition like 'E10%' or h1.maincondition in 
('-25001', '-25003', '-25011', '-25013', '-25021', '-25023', '-25031', '-25033', '-25041', '-25043', '-25051', '-25053', '-25061', '-25063', '-25071', '-25073', '-25081', '-25083', '-25091', '-25093'))
and h1.CHI = h2.CHI
```
