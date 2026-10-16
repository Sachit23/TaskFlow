# Deployment guide — verification pending

The deployment files are implemented but have not yet been tested end to end with Docker/PostgreSQL or deployed to Azure. See [the checkpoint](../PROGRESS.md) before treating this as a release.

## Any Linux server

Use a server with Docker Engine, Compose v2, at least 4 GB RAM, persistent disk, and a public hostname pointing to its IP. Open inbound TCP 80 and 443; restrict SSH to your own public IP. Do not expose the databases or Java service ports.

Copy the checkout to the server, run `python3 scripts/setup.py`, and set `PUBLIC_ADDRESS` in the generated `.env` to the public DNS hostname (without `https://`). Keep both generated database passwords private and stable.

```sh
COMPOSE_PARALLEL_LIMIT=1 docker compose -f docker-compose.prod.yml up -d --build --wait --wait-timeout 300
python3 scripts/smoke.py https://your-public-hostname
```

Caddy provisions and renews HTTPS certificates. Its `/data` volume retains certificate state. DNS and public reachability on port 80/443 must be working first. The default deployment builds its own images; `IMAGE_PREFIX` and `IMAGE_TAG` can select images published to a registry.

Check service health with `docker compose -f docker-compose.prod.yml ps` and inspect logs with `docker compose -f docker-compose.prod.yml logs --tail=100`. Normal updates preserve database volumes. Back up before deploying schema changes; never delete volumes as a deployment workaround.

## Azure from an empty account

You need an **Azure subscription**, not just Azure DevOps. Sign into Azure CLI with `az login`, select the intended subscription, and create an SSH key if you do not already have one. The template creates paid resources: an Ubuntu VM, public IP/DNS name, network, security group, and persistent OS disk. Confirm Azure's current pricing and your regional VM quota before provisioning.

```sh
bash scripts/deploy-azure.sh taskflow-production centralindia "$HOME/.ssh/id_ed25519.pub" YOUR_PUBLIC_IP/32
```

The script deploys `deployment/azure/main.json`, waits for Docker installation through cloud-init, uploads source without local secrets/build outputs, creates secrets on the VM, starts the production stack, and runs a smoke check. Verify the server SSH fingerprint through Azure on the first connection. The matching private key must remain on your computer.

The resulting hostname comes from the Azure public-IP resource; no domain purchase is necessary. Public deployment remains unverified until the app is healthy and smoke checks pass against that hostname. A single VM is a practical initial deployment, with an outage during some updates; it is not a high-availability configuration.

## Azure DevOps

Create one pipeline pointing to `azure-pipelines.yml`. It verifies the backend and frontend, runs browser checks, and packages a source release. The old pipeline filenames are compatibility entry points; disable duplicate pipelines.

For automated production delivery after the VM is provisioned:

1. Create the `TaskFlow-SSH` SSH service connection for the VM's `taskflow` user.
2. Create the `taskflow-production` environment and set approval/check policies as appropriate.
3. Configure `TASKFLOW_PUBLIC_URL` with the HTTPS URL.
4. Adapt and append the stage in `deployment/azure-release-stage.yml`.
5. Ensure the deployment agent can reach SSH. The VM restricts port 22 to the CIDR supplied at provisioning; hosted-agent addresses are dynamic, so use an agent with a stable allowed source or deliberately update the restriction.

This stage must be reviewed and tested before enabling automatic production releases. For Azure Repos, configure PR build validation through branch policies.

## Backups and restore

```sh
bash scripts/backup.sh
```

The script writes custom-format PostgreSQL dumps of both databases under `backups/`. Store copies encrypted off-server and establish a backup schedule. To restore, stop `web`, `apigateway`, `taskservice`, and `userservice`, then restore each dump into the corresponding database with `pg_restore` as its configured database user. Test restoration on a disposable stack before relying on it. Restore both databases from a consistent backup window; cross-database atomic snapshots are not provided.

## Kubernetes alternative

`k8s/` includes StatefulSets with persistent volumes for PostgreSQL, private services, probes/resource settings, and ingress routed through the web entry point. Before deploying:

- Create the `taskflow` namespace and `taskflow-db` Secret with `USER_DB_USERNAME`, `USER_DB_PASSWORD`, `TASK_DB_USERNAME`, and `TASK_DB_PASSWORD`; do not commit secrets.
- Build/push all four images and update their names/tags with Kustomize overlays.
- Configure a public ingress controller, your actual hostname, and the `taskflow-tls` TLS secret/certificate.
- Validate with `kubectl kustomize k8s` and a server-side dry run before `kubectl apply -k k8s`.

Kubernetes is optional and not yet cluster-tested. The Azure VM/Compose route is the primary deployment path for this project.
