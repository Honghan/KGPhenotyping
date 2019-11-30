# deployment a knowledge driven phenotyping study
## prerequisite
- a working directory - to be referenced as `YOUR_WORKING_DIR` in this document
- install `docker` and `docker-compose` by following instructions from [docker](https://docs.docker.com/v17.09/engine/installation/) and [docker compose](https://docs.docker.com/compose/install/)

## 1. go to a working directory and download the release files of KGPhenotyping
```
cd YOUR_WORKING_DIR
curl https://github.com/Honghan/KGPhenotyping/releases/download/v0.1/websvc.0.1.tar.gz --output websvc.0.1.tar.gz
curl https://github.com/Honghan/KGPhenotyping/releases/download/v0.1/svc_compose.yml --output svc_compose.yml
tar xzvf websvc.0.1.tar.gz
```

## 2. edit the compose file and start the service
- edit `svc_compose.yml` to replace strings of `YOUR_WEBSVC_FOLDER` with your `YOUR_WORKING_DIR`
- start the service
```
docker-compose -f svc_compose.yml up -d
```
- check the service is up running
```
docker ps
```

## 3. edit the ontology file (optional) and generate the OBDA mapping file

This system uses a minimised ontology file (OWL2 QL) as the unified/ontological view of underlying data sources. You can edit or use your own ontology. Or, you can use the ontology provided by our system. 
Once the ontology is determined. You will need to create a mapping from your databases to the ontology. This can either be done manually or using a semi-automated mapping generation system - StarLinker.  
A [sample mapping file](https://github.com/Honghan/KGPhenotyping/blob/master/src/main/resources/lab/rule-mappings/base-mapping.obda) is also provided in this system.

## 4. copy the predefined study folder
A KGPhenotyping study can be easily resued in a new setting by simply copying the folder. The only file created by a study is a SWRL rule file in json format (e.g. [this sample](https://github.com/Honghan/KGPhenotyping/blob/master/src/main/resources/lab/studies/t2dm/rules.json)).
To reuse/reproduce an existing study, just copy the rule file to a new study folder (`YOUR_STUDY_FOLDER`) as follows.
```
YOUR_WORKING_DIR/app/src/main/resources/lab/studies/YOUR_STUDY_FOLDER
```
