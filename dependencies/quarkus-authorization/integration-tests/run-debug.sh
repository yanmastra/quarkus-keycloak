#!/bin/zsh
DIR=$(pwd)
cd ../../../docker || exit
export $(grep -v "^$" .env | grep -v "^#" | xargs)
cd ..
cd dependencies/quarkus-authentication || exit
mvn clean install -DskipTests
sleep 1
cd ../quarkus-authorization || exit
mvn clean install -DskipTests
sleep 1
cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO

mvn clean quarkus:dev -Ddebug=$DEBUG