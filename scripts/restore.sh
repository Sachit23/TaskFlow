#!/usr/bin/env bash
# Restore both databases together. Requires an explicit operator confirmation.
set -euo pipefail
cd "$(dirname "$0")/.."
backup_dir="${1:?Usage: bash scripts/restore.sh BACKUP_DIRECTORY --confirm}"
[[ "${2:-}" == --confirm ]] || { echo 'Restoring replaces both databases. Add --confirm to proceed.' >&2; exit 1; }
compose() { docker compose --env-file "${TASKFLOW_ENV_FILE:-.env}" -f "${COMPOSE_FILE:-docker-compose.prod.yml}" "$@"; }
python3 - "$backup_dir" <<'PY'
import hashlib,json,sys
from pathlib import Path
root=Path(sys.argv[1]);manifest=json.loads((root/'manifest.json').read_text())
for name in ['users.dump','tasks.dump']:
    if hashlib.sha256((root/name).read_bytes()).hexdigest()!=manifest['files'][name]:
        raise SystemExit('Backup checksum mismatch: '+name)
PY
# Validate both dump formats before interrupting the application.
compose exec -T userdb pg_restore --list < "$backup_dir/users.dump" > /dev/null
compose exec -T taskdb pg_restore --list < "$backup_dir/tasks.dump" > /dev/null
running=()
while IFS= read -r service; do
  case "$service" in web|apigateway|taskservice|userservice) running+=("$service");; esac
done < <(compose ps --status running --services)
if ((${#running[@]})); then compose stop "${running[@]}"; fi
trap 'echo "Restore failed. Application services remain stopped; inspect both databases before restarting." >&2' ERR
compose exec -T userdb sh -c 'pg_restore --exit-on-error --clean --if-exists --no-owner --no-acl -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < "$backup_dir/users.dump"
compose exec -T taskdb sh -c 'pg_restore --exit-on-error --clean --if-exists --no-owner --no-acl -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < "$backup_dir/tasks.dump"
if ((${#running[@]})); then compose start "${running[@]}"; fi
printf 'Both databases restored. Wait for health checks, then run scripts/smoke.py against the public URL.\n'
