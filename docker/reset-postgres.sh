export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker container stop '${SERVER_HOST}-postgres'
docker container rm '${SERVER_HOST}-postgres'

rm -rf .postgresql
export DB_LIST="$(cat databases-list.txt)"
docker image rm $(docker images -q ${SERVER_HOST}/keycloak:latest)
sleep 2
./create-postgres-keycloak-images.sh
docker compose -f ./docker-compose.yml up postgres -d