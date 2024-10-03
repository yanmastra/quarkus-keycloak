#!/bin/zsh
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)

keytool -import -alias "${KEYCLOAK_HOST}" -keystore  "${JAVA_HOME}/lib/security/cacerts" -file ./nginx/certs/self-signed.crt
