## Knowledge Graph based Phenotyping on Heterogenous/Distributed Data Sources
- towards interpretable, reusable, reproducible phenotype computation in clinical data science.
- a project funded by Health Data Research UK

### OBDA Web Service
To build the web service run the script websvc_setup.sh. This creates a tarball, websvc.tar.gz, with everything needed for deployment.
Example deployment using docker:
```
docker build -t obdasvc .
docker run -d --name obdasvc1 -p 8050:80 obdasvc
```

#### Configuration
Main configuration file is at `src/main/resources/onto-phenotyping.properties`
Database configuration file is at `src/main/resources/lab/rule-mappings/db-property.properties`

After modifying config files, re-run websvc_setup.sh and redeploy.

