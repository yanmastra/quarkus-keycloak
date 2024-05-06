#!/bin/zsh
DIR=$(pwd)
cd ../../
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose -f docker-compose.yml up postgres -d
docker compose -f docker-compose.yml up zookeeper kafka -d

cd dependencies/authorization || exit
mvn clean install -DskipTests
echo "Building authorization is complete"
sleep 1

cd ../../
cd dependencies/common-class || exit
mvn clean install -DskipTests
echo "Building common-class is complete"
sleep 1

cd ../../
cd dependencies/microservices-common || exit
mvn clean install -DskipTests
echo "Building microservices-common is complete"
sleep 1

cd $DIR || exit

export DEBUG=15005
export QUARKUS_LOG_LEVEL=INFO

open http://localhost:4001/q/swagger-ui
mvn clean quarkus:dev -Ddebug=$DEBUG -Djdk.virtualThreadScheduler.parallelism=16 -Djdk.virtualThreadScheduler.maxPoolSize=64