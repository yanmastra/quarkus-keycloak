#!/bin/bash
DIR=$(pwd)
cd ../../../infra/docker
source .env
docker compose -f docker-compose.yml up minio -d

cd $DIR || exit
cd ../../../extensions/quarkus-base || exit
mvn clean install -DskipTests
echo "Building Base is complete"
sleep 1

cd ../../
cd extensions/quarkus-media-file-manager || exit
mvn clean install -DskipTests
echo "Building quarkus-media-file-manager is complete"
sleep 1

cd $DIR || exit

export DEBUG=15003
export QUARKUS_LOG_LEVEL=INFO
mvn clean quarkus:dev -Ddebug=$DEBUG