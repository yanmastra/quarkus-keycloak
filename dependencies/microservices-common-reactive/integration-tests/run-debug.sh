#!/bin/zsh
DIR=$(pwd)
cd ../../../
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose -f docker-compose.yml up postgres -d
docker compose -f docker-compose.yml up zookeeper kafka -d

cd dependencies/authorization || exit
mvn clean install -DskipTests
sleep 1
cd ../../
cd dependencies/common-class || exit
mvn clean install -DskipTests
sleep 1
cd ../../
cd dependencies/microservices-common-reactive || exit
mvn clean install -DskipTests
sleep 1

cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO

mvn clean
mvn quarkus:dev -Ddebug=$DEBUG