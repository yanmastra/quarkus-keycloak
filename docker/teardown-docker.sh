#!/bin/zsh
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose down