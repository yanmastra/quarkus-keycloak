#!/bin/bash

DIR=$(pwd)
cd deployed-dependencies || exit
DIR_DEPLOYED=$(pwd)
cd $DIR

cd dependencies || exit
DIRDPC=$(pwd)
echo "$DIRDPC"

DEP_LIST="quarkus-authentication quarkus-authorization quarkus-microservices-common"
DEP_CATEGORY="deployment runtime"
for dep in $DEP_LIST ; do
    echo "deploying : $dep"
    cd $dep
    mvn clean install -DskipTests
    mkdir -p "$DIR_DEPLOYED/$dep"
    cp ./pom.xml "$DIR_DEPLOYED/$dep/pom.xml"
    version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit

    cp "./deployment/target/$dep-deployment-$version.jar" "$DIR_DEPLOYED/$dep/deployment.jar"
    cp "./runtime/target/$dep-$version.jar" "$DIR_DEPLOYED/$dep/runtime.jar"

    cp "./deployment/pom.xml" "$DIR_DEPLOYED/$dep/deployment.xml"
    cp "./runtime/pom.xml" "$DIR_DEPLOYED/$dep/runtime.xml"
    cd $DIRDPC
done