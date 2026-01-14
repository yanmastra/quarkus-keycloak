#!/bin/zsh
DIR=$(pwd)
cd ../../../docker
source .env
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
export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://127.0.0.1:${POSTGRES_EXTERNAL_PORT}/db_integration_test?serverTimezone=UTC&timezone=UTC"

export DATABASE_USERNAME=developer
export DATABASE_PASSWORD=password

mvn clean quarkus:dev -Ddebug=$DEBUG