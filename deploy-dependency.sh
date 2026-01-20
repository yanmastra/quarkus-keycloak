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
    cd $dep || exit 0

    echo "getting version: mvn -q -Dexec.executable=echo -Dexec.args='\${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:3.1.0:exec"
    VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:3.1.0:exec)
    echo "got version: $VERSION"

    mvn clean install -DskipTests
    mkdir -p "$DIR_DEPLOYED/$VERSION/$dep"

    version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit

    if [ -d "runtime" ]; then
      cp ./pom.xml "$DIR_DEPLOYED/$VERSION/$dep/pom.xml"

      cp "./deployment/target/$dep-deployment-$version.jar" "$DIR_DEPLOYED/$VERSION/$dep/deployment.jar"
      cp "./runtime/target/$dep-$version.jar" "$DIR_DEPLOYED/$VERSION/$dep/runtime.jar"
      cp "./runtime/target/$dep-$version-javadoc.jar" "$DIR_DEPLOYED/$VERSION/$dep/runtime-javadoc.jar"

      cp "./deployment/pom.xml" "$DIR_DEPLOYED/$VERSION/$dep/deployment.xml"
      cp "./runtime/pom.xml" "$DIR_DEPLOYED/$VERSION/$dep/runtime.xml"
    else
      cp ./pom.xml "$DIR_DEPLOYED/$VERSION/$dep/runtime.xml"
      cp "./target/$dep-$version.jar" "$DIR_DEPLOYED/$VERSION/$dep/runtime.jar"
      cp "./target/$dep-$version-javadoc.jar" "$DIR_DEPLOYED/$VERSION/$dep/runtime-javadoc.jar"
    fi

    cd $DIRDPC
done