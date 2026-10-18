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
# Deploy committed source only; ignored local secrets never enter the archive.
git archive --format=tar.gz --output="$archive" HEAD
scp -i "$private_key" "$archive" "taskflow@$hostname:/opt/taskflow/source.tar.gz"
ssh -i "$private_key" "taskflow@$hostname" bash -s -- "$hostname" <<'REMOTE'
set -euo pipefail
cd /opt/taskflow
bootstrap_dir="$(mktemp -d)"
trap 'rm -rf "$bootstrap_dir"' EXIT
tar -xzf source.tar.gz -C "$bootstrap_dir" scripts/release.sh
sudo env TASKFLOW_PUBLIC_ADDRESS="$1" bash "$bootstrap_dir/scripts/release.sh" /opt/taskflow/source.tar.gz /opt/taskflow
REMOTE
python3 scripts/smoke.py "https://$hostname" --wait-seconds 180
printf '\nTaskFlow is live at https://%s\n' "$hostname"
