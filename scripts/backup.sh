#!/usr/bin/env bash
# Briefly pause application writes so the two database dumps form one checkpoint.
set -euo pipefail
cd "$(dirname "$0")/.."
umask 077
backup_base="${TASKFLOW_BACKUP_DIR:-backups}"
if [[ -z "${TASKFLOW_BACKUP_DIR:-}" && -L .env ]]; then backup_base="$(dirname "$(readlink .env)")/backups"; fi
backup_dir="${1:-$backup_base/$(date -u +%Y%m%dT%H%M%SZ)}"
compose() { docker compose --env-file "${TASKFLOW_ENV_FILE:-.env}" -f "${COMPOSE_FILE:-docker-compose.prod.yml}" "$@"; }
mkdir -p "$(dirname "$backup_dir")"
mkdir "$backup_dir" # Refuse to overwrite an existing backup.
running=()
while IFS= read -r service; do
  case "$service" in web|apigateway|taskservice|userservice) running+=("$service");; esac
done < <(compose ps --status running --services)
resume() { if ((${#running[@]})); then compose start "${running[@]}"; fi; }
trap resume EXIT
if ((${#running[@]})); then compose stop "${running[@]}"; fi
compose exec -T userdb sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$backup_dir/users.dump.partial"
compose exec -T taskdb sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$backup_dir/tasks.dump.partial"
mv "$backup_dir/users.dump.partial" "$backup_dir/users.dump"
mv "$backup_dir/tasks.dump.partial" "$backup_dir/tasks.dump"
python3 - "$backup_dir" <<'PY'
import datetime,hashlib,json,sys
from pathlib import Path
root=Path(sys.argv[1])
manifest={'created_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'files':{f:hashlib.sha256((root/f).read_bytes()).hexdigest() for f in ['users.dump','tasks.dump']}}
(root/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
PY
printf 'Backup saved to %s. Keep an encrypted copy off the server.\n' "$backup_dir"
