#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$DIR"
cd deployed-dependencies || exit
DIR_DEPLOYED=$(pwd)
cd $DIR

cd extensions || exit
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