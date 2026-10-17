#!/bin/bash
# Lance le serveur en lisant les secrets dans le fichier .env (jamais envoyé sur GitHub)
cd "$(dirname "$0")" || exit 1
set -a
. ./.env
set +a
exec ./mvnw spring-boot:run
