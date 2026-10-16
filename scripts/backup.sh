#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
umask 077
backup_dir="${1:-backups/$(date -u +%Y%m%dT%H%M%SZ)}"
mkdir -p "$backup_dir"
compose_file="${COMPOSE_FILE:-docker-compose.prod.yml}"
docker compose -f "$compose_file" exec -T userdb sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$backup_dir/users.dump"
docker compose -f "$compose_file" exec -T taskdb sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$backup_dir/tasks.dump"
printf 'Backups saved to %s. Copy them to encrypted off-server storage.\n' "$backup_dir"
