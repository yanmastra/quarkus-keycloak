#!/bin/zsh
DIR=$(pwd)
cd ../../../infra/docker || exit
export $(grep -v "^$" .env | grep -v "^#" | xargs)
docker compose up postgres keycloak nginx-proxy -d
cd ..
cd extensions/quarkus-authentication || exit
mvn clean install -DskipTests
sleep 1
cd ../quarkus-authorization || exit
mvn clean install -DskipTests
sleep 1
cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO

mvn clean quarkus:dev -Ddebug=$DEBUG