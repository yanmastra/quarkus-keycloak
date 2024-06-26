#!/bin/zsh

export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)

echo "keytool -genkeypair -storepass \"${KEYCLOAK_KEYSTORE_PASSWORD}\" -storetype PKCS12 -keyalg RSA -keysize 2048 -dname \"CN=${KEYCLOAK_HOST}\" -alias \"${KEYCLOAK_HOST}\" -ext \"SAN:c=DNS:${KEYCLOAK_HOST},IP:10.123.123.123\" -keystore ./docker/keycloak/server.keystore"
keytool -genkeypair -storepass "${KEYCLOAK_KEYSTORE_PASSWORD}" -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=${KEYCLOAK_HOST}" -alias "${KEYCLOAK_HOST}" -ext "SAN:c=DNS:${KEYCLOAK_HOST},IP:10.123.123.123" -keystore ./docker/keycloak/server.keystore

mkdir ./nginx/certs
echo "openssl req -x509 -newkey rsa:4096 -keyout ./nginx/certs/self-signed.key -out ./nginx/certs/self-signed.crt -sha256 -days 365 -nodes -subj \"/C=ID/ST=Bali/L=Denpasar/O=IndonesianTechnologyCompany/OU=ITService/CN=${KEYCLOAK_HOST}\""
openssl req -x509 -newkey rsa:4096 -keyout ./nginx/certs/self-signed.key -out ./nginx/certs/self-signed.crt -sha256 -days 365 -nodes -subj "/C=ID/ST=Bali/L=Denpasar/O=IndonesianTechnologyCompany/OU=ITService/CN=${KEYCLOAK_HOST}"