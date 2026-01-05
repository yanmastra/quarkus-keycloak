#export $(grep -v "^$" .env | grep -v "^#" | xargs)
source .env

DB=$1
echo "adding new database: $DB"
docker exec -it ${SERVER_HOST}-postgres bash -c \
"psql -U ${DATABASE_USERNAME} -c 'CREATE USER $DB; CREATE DATABASE $DB; GRANT ALL PRIVILEGES ON DATABASE $DB TO $DB;'"