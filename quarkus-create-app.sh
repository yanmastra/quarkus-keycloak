#!/bin/bash

export DIR=$(pwd)
export DIR_NAME="${PWD##*/}"
cd ..
export PROJECT_DIR=$(pwd)
cd $DIR || exit

if [[ -f .env ]]; then
  echo ".env already exists"
  source .env
else
  echo "export PROJECT_DIR=${PROJECT_DIR}" >> .env
  echo "export RESOURCE_DIR=${DIR}" >> .env
  echo "export QUARKUS_VERSION=3.15.1" >> .env
fi

export GROUP_ID=${1:-org.acme}
export ARTIFACT_ID=${2:-getting-started}

cd $PROJECT_DIR || exit

mvn io.quarkus.platform:quarkus-maven-plugin:${QUARKUS_VERSION:-3.15.1}:create \
    -DprojectGroupId=$GROUP_ID \
    -DprojectArtifactId=$ARTIFACT_ID
#mkdir $ARTIFACT_ID

cd $ARTIFACT_ID || exit
export VAR_PROJECT_DIR="\${PROJECT_DIR}"
export VAR_SOURCE="\$(grep -v \"^$\" docker_env.env | grep -v \"^#\" | xargs)"
export VAR_PWD="\$(pwd)"
export VAR_DIR="\$DIR"
export VAR_DIR_NAME="\$DIR_NAME"
export VAR_RESOURCE_DIR="\$RESOURCE_DIR"

envsubst  < ${DIR}/docker/run-debug.sh.template > run-debug.sh
chmod +x run-debug.sh
cp ${DIR}/docker/docker_env.env .
envsubst < ${DIR}/docker/pom.xml.template > pom.xml
cat ${DIR}/docker/application.properties.template > src/main/resources/application.properties
echo "quarkus.smallrye-openapi.info-title=$ARTIFACT_ID" >> src/main/resources/application.properties
mvn clean install -DskipTests
cd $DIR || exit