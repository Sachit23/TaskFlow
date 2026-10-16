#!/usr/bin/env bash
# Requires an Azure subscription, az login, Docker-capable VM quota and an SSH key.
set -euo pipefail
cd "$(dirname "$0")/.."
resource_group="${1:-taskflow-production}"
region="${2:-centralindia}"
public_key="${3:-$HOME/.ssh/id_ed25519.pub}"
ssh_cidr="${4:?Usage: scripts/deploy-azure.sh RESOURCE_GROUP REGION PUBLIC_KEY YOUR_PUBLIC_IP/32}"
command -v az >/dev/null || { echo 'Install Azure CLI and run az login first.' >&2; exit 1; }
test -f "$public_key" || { echo 'Public SSH key not found.' >&2; exit 1; }
private_key="${public_key%.pub}"
test -f "$private_key" || { echo 'Matching private key not found.' >&2; exit 1; }
az account show --output none
printf 'Creating/updating paid Azure resources in %s (%s).\n' "$resource_group" "$region"
az group create --name "$resource_group" --location "$region" --output none
az deployment group create --resource-group "$resource_group" --template-file deployment/azure/main.json --parameters sshPublicKey="$(cat "$public_key")" sshAllowedCidr="$ssh_cidr" --output none
hostname="$(az deployment group show --resource-group "$resource_group" --name main --query properties.outputs.hostname.value -o tsv)"
printf 'VM provisioned: %s. Verify its SSH fingerprint in Azure before accepting the first connection.\n' "$hostname"
# StrictHostKeyChecking=ask requires explicit verification on the first connection.
ssh -i "$private_key" -o StrictHostKeyChecking=ask "taskflow@$hostname" 'sudo cloud-init status --wait'
archive="$(mktemp -t taskflow-source.XXXXXX)"
trap 'rm -f "$archive"' EXIT
tar --exclude='./.git' --exclude='./.env' --exclude='./.env.*' --exclude='./frontend/node_modules' --exclude='./frontend/dist' --exclude='./frontend/test-results' --exclude='./frontend/playwright-report' --exclude='./.local-data' --exclude='./.local-logs' --exclude='./backups' --exclude='*/target' --exclude='*.DS_Store' -czf "$archive" .
scp -i "$private_key" "$archive" "taskflow@$hostname:/opt/taskflow/source.tar.gz"
ssh -i "$private_key" "taskflow@$hostname" bash -s -- "$hostname" <<'REMOTE'
set -euo pipefail
cd /opt/taskflow
tar -xzf source.tar.gz
python3 scripts/setup.py
python3 - "$1" <<'ENV'
from pathlib import Path
import sys
p=Path('.env');s=p.read_text();s='\n'.join('PUBLIC_ADDRESS='+sys.argv[1] if l.startswith('PUBLIC_ADDRESS=')else l for l in s.splitlines())+'\n';p.write_text(s)
ENV
sudo env COMPOSE_PARALLEL_LIMIT=1 docker compose -f docker-compose.prod.yml up -d --build --wait --wait-timeout 300
REMOTE
python3 scripts/smoke.py "https://$hostname"
printf '\nTaskFlow is live at https://%s\n' "$hostname"
