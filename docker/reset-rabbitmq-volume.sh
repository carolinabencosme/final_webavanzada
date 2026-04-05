#!/usr/bin/env sh
# Limpia el volumen antiguo rabbitmq_data. Uso: desde la raíz del repo: sh docker/reset-rabbitmq-volume.sh
set -e
cd "$(dirname "$0")/.."
echo "Deteniendo stack..."
docker compose down 2>/dev/null || true

PROJECT="$(basename "$(pwd)" | tr -cd '[:alnum:]')"
OLD="${PROJECT}_rabbitmq_data"
echo "Intentando eliminar: $OLD"
if docker volume rm "$OLD" 2>/dev/null; then
  echo "OK: volumen eliminado."
else
  echo "Info: no existe o ya fue borrado. Volúmenes con 'rabbit':"
  docker volume ls | grep -i rabbit || true
fi

echo "Siguiente: docker compose up -d (volumen nuevo: hospedaje_rabbitmq_data)"
