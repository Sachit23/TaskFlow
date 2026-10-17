# Deploying TaskFlow

The Docker/PostgreSQL stack, all browser workflows, restart persistence, and backup/restoration have been tested locally. Azure resource creation and the Azure DevOps pipeline still need their first run in your account. [Verification details](../PROGRESS.md).

## Any Linux server

Use a Linux server with Docker Engine, Compose v2, Python 3, at least 4 GB RAM, persistent disk, and a public hostname pointing to its IP. Permit inbound TCP 80/443 and restrict SSH to your own public IP. Databases and Java service ports stay inside the Docker network.

Copy the committed source to the server, run `python3 scripts/setup.py`, and change `PUBLIC_ADDRESS` in `.env` to your public DNS hostname, without `https://`. Keep the generated database passwords stable.

```sh
COMPOSE_PARALLEL_LIMIT=1 docker compose -f docker-compose.prod.yml up -d --build --wait --wait-timeout 300
python3 scripts/smoke.py https://your-public-hostname --wait-seconds 180
```

Caddy obtains and renews HTTPS certificates and redirects HTTP to HTTPS. Its persistent `/data` volume retains certificate state. DNS and public reachability must work before certificate issuance can complete. Configuration validation passed locally; a real certificate can only be verified after public deployment.

Inspect health and logs with:

```sh
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs --tail=100
```

## Azure from your current setup

An Azure DevOps account provides pipelines; an **Azure subscription** provides hosting resources. First create or connect a subscription, install Azure CLI, run `az login`, and select the intended subscription. You also need an SSH key pair.

The template provisions an Ubuntu 24.04 VM, a persistent 64 GB OS disk, a static public IP with a DNS hostname, a virtual network, and a security group. Its default VM size is `Standard_B2s`. Check the selected subscription's quota and Azure's current pricing before running the provisioning command.

```sh
bash scripts/deploy-azure.sh taskflow-production centralindia "$HOME/.ssh/id_ed25519.pub" YOUR_PUBLIC_IP/32
```

Use your actual public IP/CIDR and a region with available quota. Verify the VM's SSH fingerprint through Azure on the first connection. The script waits for Docker installation, packages **committed Git source only**, uploads it, creates secrets on the server, sets the Azure hostname, deploys the stack, and performs a public smoke check. Commit your intended changes before invoking it.

The template is at `deployment/azure/main.json`; its embedded cloud-init configuration is checked against the reviewable `cloud-init.yaml` by automated tests. Offline checks verify the SSH restriction and disable password authentication. Azure API validation, resource quota, certificate issuance, and public reachability remain account-specific checks for the first deployment.

No Azure resources have been created by this work.

## Versioned releases and rollback

For subsequent releases, upload the pipeline's `taskflow.tar.gz` and run:

```sh
sudo bash /path/to/release.sh /path/to/taskflow.tar.gz /opt/taskflow
```

`scripts/release.sh` extracts each artifact into its own checksum-named directory under `/opt/taskflow/releases/`. It rejects traversal, links, and local secret/state files. Releases share `/opt/taskflow/.env`; source updates cannot overwrite it. Separate directories also ensure deleted source files do not linger after an update.

Images receive the release's unique tag, which is saved in the shared `.env` after successful startup so maintenance commands select the same images. If startup health checks fail, the installer starts the previous images and leaves `current` pointing to the last successful release. After success, `current` and `previous` identify the two most recent releases. Keep previous images and directories until you no longer need them for recovery.

This rolls back application images. It does not reverse database migrations. Back up before schema changes and design migrations to remain compatible with the previous application version. The deployment uses one VM and can have a brief interruption during updates.

## Azure DevOps pipeline

Create one pipeline using **`azure-pipelines.yml`**. The old experimental pipeline files have been retired; repoint any existing definitions to this file.

For the current development branch, select **Azure Repos Git → TaskFlow → Existing Azure Pipelines YAML file**, choose `feature/kubernetes-integration`, and set the path to `/azure-pipelines.yml`. Save and run the pipeline on that branch. If a TaskFlow pipeline already exists, update its YAML path instead of creating a duplicate. CI runs on `main`, `master`, `develop`, and `feature/*`; updates arriving during a run are batched. Azure Repos PR validation requires a build-validation policy on the target branch, rather than a YAML `pr` trigger.

The repository's current default branch is `develop`. Push and verify the feature branch first, then open a pull request into `develop`; merging remains a separate review step. This verification pipeline does not require an Azure subscription or provision a host. Microsoft-hosted agent availability depends on your Azure DevOps organization's parallel-job entitlement; if a run reports no hosted parallelism, resolve that entitlement before configuring deployment.

The pipeline:

1. Runs backend and deployment tests and publishes coverage reports.
2. Builds the frontend, runs unit tests, and starts the production containers with PostgreSQL.
3. Runs API smoke checks and the full browser suite against those containers.
4. Packages committed source as a release artifact, excluding ignored local secrets and build state.

For automated production delivery after provisioning the VM:

- Create an SSH service connection named `TaskFlow-SSH` for the VM's `taskflow` user.
- Create the `taskflow-production` environment, enable its exclusive lock, and configure your release approvals.
- Define `TASKFLOW_PUBLIC_URL` as the HTTPS URL.
- Append the stage in `deployment/azure-release-stage.yml` to the canonical pipeline. It deploys the versioned artifact and runs the public smoke check.
- Run the deployment agent from a stable IP allowed by the VM's SSH restriction. Microsoft-hosted agent addresses change, so they need a deliberate network-access arrangement.

The optional stage deploys from `main`; adjust that branch condition if your release branch differs. For Azure Repos, configure PR build validation through branch policies. Pipeline configuration and deployment scripts are committed; the account's service connection and first remote pipeline run are still to be set up.

## Back up and restore

From the deployed `current` directory:

```sh
sudo bash scripts/backup.sh
```

Backup briefly stops running application services, keeps the databases online, and creates both dumps while writes are paused. It resumes the services afterward. Each backup includes SHA-256 checksums; an existing backup directory is never overwritten. For versioned releases, the default backup location is the shared deployment root's `backups/` directory. `TASKFLOW_BACKUP_DIR` or a positional directory can override it.

Keep encrypted copies off the VM and schedule regular backups. To restore a selected checkpoint:

```sh
sudo bash scripts/restore.sh /opt/taskflow/backups/TIMESTAMP --confirm
sudo docker compose -f docker-compose.prod.yml up -d --no-build --wait --wait-timeout 300
python3 scripts/smoke.py https://your-public-hostname --wait-seconds 180
```

Restore validates both checksums and archive formats before stopping application services. It restores both databases and resumes the services. On restoration failure it leaves the application stopped for investigation. A real isolated-stack restore has been tested, including session and workspace recovery.

Use the default production project name `taskflow`, or pass the matching `COMPOSE_PROJECT_NAME` for other deployments. The backup tools accept `COMPOSE_FILE` and `TASKFLOW_ENV_FILE` when operating a separate local/test stack.

## Existing legacy databases

The original project used Hibernate-managed tables and PostgreSQL 15. The new deployment initializes fresh PostgreSQL 17 databases with Flyway migrations. Do not attach older database volumes to the new PostgreSQL major version or bypass Flyway's nonempty-schema check.

If you need historical records, back up both old databases using the old deployment first. Restore into a separate environment and prepare a reviewed schema/data migration that preserves user IDs, task ownership, and password hashes. No existing project volumes were modified during this work.

## Optional Kubernetes deployment

`k8s/` contains private services, resource settings, startup/readiness probes, PostgreSQL StatefulSets with persistent volumes, and ingress through the web service. `kubectl kustomize k8s` renders successfully; no live cluster has been tested.

Before applying it, create the `taskflow` namespace and `taskflow-db` Secret with the four database username/password keys, publish all four images and set their immutable tags through Kustomize, and configure an ingress controller, real hostname, and the `taskflow-tls` certificate. Use a server-side dry run against the intended cluster before deployment. Configure trusted proxy/client-IP handling for your ingress so login rate limiting sees individual clients.

The Azure VM/Compose workflow is the primary deployment route for this release.
