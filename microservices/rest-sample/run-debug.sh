#!/bin/zsh
DIR=$(pwd)
cd ../../docker || exit
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose -f docker-compose.yml up postgres -d
docker compose -f docker-compose.yml up keycloak -d
docker compose -f docker-compose.yml up nginx -d

cd ..
cd dependencies/authorization || exit
mvn clean install -DskipTests
echo "Building authorization is complete"
sleep 1
cd ../quarkus-microservices-common || exit
mvn clean install -DskipTests
echo "Building quarkus-microservices-common is complete"
sleep 1
#cp ../../docker/keycloak/imports/realm-sample.json $DIR/src/main/resources/
cd $DIR || exit

export DEBUG=15005
export QUARKUS_LOG_LEVEL=INFO

open http://localhost:4001/q/swagger-ui
mvn clean quarkus:dev -Ddebug=$DEBUG -Djdk.virtualThreadScheduler.parallelism=16 -Djdk.virtualThreadScheduler.maxPoolSize=64