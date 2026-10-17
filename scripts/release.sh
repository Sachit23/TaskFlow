#!/usr/bin/env bash
# Install an immutable source release and roll back images if startup fails.
set -euo pipefail
archive="${1:?Usage: bash scripts/release.sh SOURCE_ARCHIVE [DEPLOY_ROOT]}"
deploy_root="${2:-/opt/taskflow}"
project="${TASKFLOW_PROJECT_NAME:-taskflow}"
release_dir="$(python3 - "$archive" "$deploy_root" <<'PY'
import hashlib,os,tarfile,tempfile,sys
from pathlib import Path,PurePosixPath
archive=Path(sys.argv[1]).resolve();root=Path(sys.argv[2]).resolve()
if root==Path('/'):raise SystemExit('Choose an application deployment directory.')
release_id=hashlib.sha256(archive.read_bytes()).hexdigest()[:16]
releases=root/'releases';releases.mkdir(parents=True,exist_ok=True)
release=releases/release_id
if not release.exists():
    with tempfile.TemporaryDirectory(prefix='.pending-',dir=releases) as staging:
        staging=Path(staging)
        with tarfile.open(archive,'r:gz') as source:
            members=[]
            for item in source.getmembers():
                path=PurePosixPath(item.name)
                if path.is_absolute() or '..' in path.parts or not(item.isfile() or item.isdir()):
                    raise SystemExit('Unsafe entry in release archive: '+item.name)
                if path.parts and (path.parts[0] in {'.env','.git','backups','.local-data','.local-logs'} or (path.parts[0].startswith('.env.') and path.parts[0]!='.env.example')):
                    raise SystemExit('Release archive contains local state: '+item.name)
                item.mode &= 0o777
                members.append(item)
            source.extractall(staging,members=members)
        if not(staging/'docker-compose.prod.yml').is_file():raise SystemExit('Release is missing docker-compose.prod.yml')
        os.replace(staging,release)
print(release)
PY
)"
deploy_root="$(dirname "$(dirname "$release_dir")")"
shared_env="$deploy_root/.env"
if [[ ! -f "$shared_env" ]]; then
  python3 "$release_dir/scripts/setup.py"
  mv "$release_dir/.env" "$shared_env"
fi
if [[ -n "${TASKFLOW_PUBLIC_ADDRESS:-}" ]]; then
  python3 - "$shared_env" "$TASKFLOW_PUBLIC_ADDRESS" <<'ENV'
from pathlib import Path
import re,sys
if not re.fullmatch(r'[A-Za-z0-9](?:[A-Za-z0-9.-]{0,251}[A-Za-z0-9])?',sys.argv[2]):
    raise SystemExit('PUBLIC_ADDRESS must be a DNS hostname without a protocol or path.')
p=Path(sys.argv[1]);lines=p.read_text().splitlines()
lines=[x for x in lines if not x.startswith('PUBLIC_ADDRESS=')]+['PUBLIC_ADDRESS='+sys.argv[2]]
p.write_text('\n'.join(lines)+'\n')
ENV
fi
if [[ ! -e "$release_dir/.env" ]]; then ln -s "$shared_env" "$release_dir/.env"; fi
previous=""
if [[ -L "$deploy_root/current" ]]; then previous="$(readlink "$deploy_root/current")"; fi
compose_release() {
  local source="$1"; shift
  IMAGE_TAG="$(basename "$source")" docker compose -p "$project" --env-file "$shared_env" -f "$source/docker-compose.prod.yml" "$@"
}
if ! compose_release "$release_dir" up -d --build --wait --wait-timeout 300; then
  if [[ -n "$previous" && -f "$previous/docker-compose.prod.yml" ]]; then
    echo 'New containers failed health checks; restoring the previous images.' >&2
    compose_release "$previous" up -d --no-build --wait --wait-timeout 300
  fi
  exit 1
fi
python3 - "$deploy_root" "$release_dir" "$previous" <<'PY'
import os,sys
from pathlib import Path
root=Path(sys.argv[1])
for name,target in [('current',sys.argv[2]),('previous',sys.argv[3])]:
    if target:
        tmp=root/('.'+name+'-next')
        tmp.unlink(missing_ok=True);tmp.symlink_to(target);os.replace(tmp,root/name)
PY
printf 'Healthy release: %s\n' "$(basename "$release_dir")"
