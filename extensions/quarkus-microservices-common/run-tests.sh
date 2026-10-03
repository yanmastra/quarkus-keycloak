#!/bin/bash
# Runs unit tests + integration tests for quarkus-microservices-common.
set -e

DIR=$(cd "$(dirname "$0")" && pwd)

cd "$DIR/../../infra/docker"
source .env
docker compose -f docker-compose.yml up postgres -d

echo "Waiting for postgres to be healthy..."
until [ "$(docker inspect -f '{{.State.Health.Status}}' "${PROJECT_NAME}-postgres" 2>/dev/null)" = "healthy" ]; do
  sleep 1
done

cd "$DIR/../quarkus-base"
mvn clean install -DskipTests
echo "quarkus-base installed"

cd "$DIR"
mvn clean install -DskipTests
echo "quarkus-microservices-common installed"

mvn test
echo "quarkus-microservices-common unit tests complete"

cd "$DIR/integration-tests"
export DATABASE_HOST
export DATABASE_USERNAME
export DATABASE_PASSWORD
export POSTGRES_EXTERNAL_PORT
mvn clean test
echo "quarkus-microservices-common integration tests complete"
