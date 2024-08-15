#!/bin/zsh
DIR=$(pwd)
cd ../../../
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose -f docker-compose.yml up postgres -d

cd dependencies/authorization || exit
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

mvn clean
mvn quarkus:dev -Ddebug=$DEBUG