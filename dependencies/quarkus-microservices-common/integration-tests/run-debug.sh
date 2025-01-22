#!/bin/zsh
DIR=$(pwd)
cd ../../../docker/
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
export DB_LIST=$(cat databases-list.txt)
export KEYCLOAK_DB_NAME=db_keycloak
docker compose -f docker-compose.yml up postgres -d

cd ..
cd dependencies/quarkus-authentication || exit
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