## Knowledge Graph based Phenotyping on Heterogenous Data Sources
- towards interpretable, reusable, reproducible phenotype computation in clinical data science.
- a project funded by [Health Data Research UK: Graph-Based Data Federation for Healthcare Data Science](https://www.hdruk.ac.uk/projects/graph-based-data-federation-for-healthcare-data-science/)

### Why & What
Extracting patient phenotypes from routinely collected health data (such as Electronic Health Records) requires translating clinically-sound phenotype definitions into queries/computations executable on the underlying data sources by clinical researchers. This requires significant knowledge and skills to deal with heterogeneous and   often *imperfect* data. Translations are time-consuming,  error-prone and, most importantly, hard to share and reproduce across different settings. This project implements a **knowledge driven phenotyping framework** that  
1. decouples the specification of phenotype semantics from underlying data sources; 
2. can automatically populate and conduct phenotype computations on heterogeneous data spaces. 

### Architecture & Deployment
This framework has been deployed on five Scottish health datasets.
![alt text](https://raw.githubusercontent.com/Honghan/KGPhenotyping/master/assets/kg-phenotyping-arch.png "Architecture of Knowledge Driven Phenotyping")
