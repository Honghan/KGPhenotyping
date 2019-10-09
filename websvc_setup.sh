#!/bin/sh

mvn dependency:copy-dependencies verify
mkdir websvc/app/lib
cp -r target/dependency/* websvc/app/lib
cp target/OntologyPhenotyping*jar websvc/app
mkdir -p websvc/app/src/main/resources
cp -r src/main/resources/lab websvc/app/src/main/resources
tar -zcf websvc.tar.gz websvc
