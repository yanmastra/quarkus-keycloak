#!/bin/zsh
DIR=$(pwd)
cd ../../../docker/
export $(grep -v "^$" .env | grep -v "^#" | xargs)
docker compose -f docker-compose.yml up postgres -d

cd $DIR || exit
cd ../../../dependencies/quarkus-authentication || exit
mvn clean install -DskipTests
echo "Building authorization is complete"
sleep 1

cd ../../
cd dependencies/quarkus-microservices-common || exit
mvn clean install -DskipTests
echo "Building microservices-common is complete"
sleep 1

cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO

mvn clean quarkus:dev -Ddebug=$DEBUG