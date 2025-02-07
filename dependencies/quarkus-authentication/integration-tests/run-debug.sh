#!/bin/bash
DIR=$(pwd)
echo "cd ../../../docker"
cd ../../../docker || exit
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)

cd $DIR || exit
cd ..
mvn clean install -DskipTests
sleep 1
cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO

mvn clean
mvn quarkus:dev -Ddebug=$DEBUG