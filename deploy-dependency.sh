#!/bin/bash

DIR=$(pwd)
cd deployed-dependencies || exit
DIR_DEPLOYED=$(pwd)
cd $DIR

cd dependencies || exit
DIRDPC=$(pwd)
echo "$DIRDPC"

DEP_LIST="quarkus-base quarkus-authentication quarkus-authorization quarkus-microservices-common"
DEP_CATEGORY="deployment runtime"
for dep in $DEP_LIST ; do
    echo "deploying : $dep"
    cd $dep
    mvn clean install -DskipTests
    mkdir -p "$DIR_DEPLOYED/$dep"

    version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit

    if [ -d "runtime" ]; then
      cp ./pom.xml "$DIR_DEPLOYED/$dep/pom.xml"

      cp "./deployment/target/$dep-deployment-$version.jar" "$DIR_DEPLOYED/$dep/deployment.jar"
      cp "./runtime/target/$dep-$version.jar" "$DIR_DEPLOYED/$dep/runtime.jar"

      cp "./deployment/pom.xml" "$DIR_DEPLOYED/$dep/deployment.xml"
      cp "./runtime/pom.xml" "$DIR_DEPLOYED/$dep/runtime.xml"
    else
      cp ./pom.xml "$DIR_DEPLOYED/$dep/runtime.xml"
      cp "./target/$dep-$version.jar" "$DIR_DEPLOYED/$dep/runtime.jar"
    fi

    cd $DIRDPC
done