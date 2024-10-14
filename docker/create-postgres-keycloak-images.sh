export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
export DB_LIST="$(cat databases-list.txt)"
export KEYCLOAK_DB_NAME=db_authentication
envsubst  < keycloak/Dockerfile.template > keycloak/Dockerfile

docker build -f keycloak/Dockerfile -t ${SERVER_HOST}/keycloak:latest .
docker build -f postgres/Dockerfile -t ${SERVER_HOST}/postgres:latest .