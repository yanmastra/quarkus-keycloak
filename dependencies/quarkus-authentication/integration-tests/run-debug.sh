#!/bin/zsh
DIR=$(pwd)
cd ../../../docker
source .env

cd $DIR || exit
cd ..
mvn clean install -DskipTests
sleep 1
cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO

mvn clean
mvn quarkus:dev -Ddebug=$DEBUG