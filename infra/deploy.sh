#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f "infra/.env" ]; then
  echo "请先复制 infra/.env.production.example 为 infra/.env 并填写真实密码和公网地址。"
  exit 1
fi

cd backend
mvn -DskipTests package

cd ../infra
docker compose --env-file .env -f docker-compose.prod.yml up -d --build
docker compose --env-file .env -f docker-compose.prod.yml ps
