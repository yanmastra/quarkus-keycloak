#!/bin/bash

DIR=$(pwd)

function install_jar_to_maven() {
  file=$1
  pomFile=$2
mvn install:install-file \
   -Dfile=$file \
   -DpomFile=$pomFile
}

DPC_LIST="quarkus-base quarkus-authentication quarkus-microservices-common"
for dpc in $DPC_LIST ; do
    cd "dependencies/$dpc" || exit
    echo ""
    echo "mvn clean install -N"
    mvn clean install -N
    cd ..
    install_jar_to_maven $dpc/runtime.jar $dpc/runtime.xml
    install_jar_to_maven $dpc/deployment.jar $dpc/deployment.xml
    cd $DIR
done