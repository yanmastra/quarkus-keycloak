#!/bin/zsh
DIR=$(pwd)
cd ../../
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
#docker compose -f docker-compose.yml up postgres -d
#docker compose -f docker-compose.yml up zookeeper kafka -d

cd dependencies/authorization || exit
mvn clean install -DskipTests
echo "Building authorization is complete"
sleep 1

cd ../../
cd dependencies/quarkus-microservices-common || exit
mvn clean install -DskipTests
echo "Building quarkus-microservices-common is complete"
sleep 1

cd $DIR || exit

export DEBUG=15005
export QUARKUS_LOG_LEVEL=INFO

PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit
echo "project version: $PROJECT_VERSION"
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout) || exit
echo "project artifactId: $ARTIFACT_ID"
GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout) || exit
echo "project groupId: $GROUP_ID"

mvn clean package -Pnative -DskipTests \
                  -Dquarkus.native.additional-build-args="--verbose, --report-unsupported-elements-at-runtime, -H:+UnlockExperimentalVMOptions" \
                  -Dquarkus.native.container-build=true \
                  -Dquarkus.container-image.build=true \
                  -Dquarkus.container-image.group=$GROUP_ID \
                  -Dquarkus.container-image.name=$ARTIFACT_ID \
                  -Dquarkus.container-image.tag=$PROJECT_VERSION \
                  -Dquarkus.native.debug.enabled=true

docker tag $GROUP_ID/$ARTIFACT_ID:$PROJECT_VERSION $GROUP_ID/$ARTIFACT_ID:latest