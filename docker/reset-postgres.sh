export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker container stop q-learning-postgres
docker container rm q-learning-postgres

rm -rf .postgresql
export DB_LIST="$(cat databases-list.txt)"
docker image rm $(docker images -q docker-postgres)
sleep 2
docker compose -f ./docker-compose.yml up postgres -d