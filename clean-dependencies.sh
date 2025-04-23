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
    mvn clean
    cd $DIRDPC
done