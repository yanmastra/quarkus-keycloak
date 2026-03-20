#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
export DIR_NAME="${DIR##*/}"
cd "$DIR"
cd extensions || exit
export DEPENDENCIES_DIR=$(pwd)
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

cd ${DEPENDENCIES_DIR} || exit

mvn io.quarkus.platform:quarkus-maven-plugin:${QUARKUS_VERSION:-2.16.3}:create-extension -N -DgroupId=${GROUP_ID:-io.yanmastra} -DextensionId=${ARTIFACT_ID:-greeting-extension}
echo "Extension create in ${PROJECT_DIR}/${ARTIFACT_ID}"
cd $DIR
