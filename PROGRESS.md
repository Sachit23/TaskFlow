# TaskFlow status

Local implementation and deployment verification are complete as of 17 October 2026. **The app is not yet publicly deployed.** No Azure subscription, service connection, or live host has been supplied. The user has an Azure DevOps account only.

## Working application

- Secure accounts, expiring/revocable sessions, profile/password changes, and private workspaces.
- Projects, dashboard, Kanban/list views, task priorities/status/dates, labels, subtasks, comments, activity history, search/filter/sort, CSV export, dark mode, and responsive layouts.
- Self-hosted fonts, accessible named dialogs, and optional editable sample data.
- Three Java services, two PostgreSQL databases, and Caddy web/HTTPS ingress.
- Versioned Flyway schemas, non-root Java containers, private service networks, persistent volumes, health checks, and bounded container logs.
- Versioned releases with startup image rollback, consistent checksummed backups, restoration tooling, and a canonical Azure DevOps pipeline that tests production containers.

## Verification evidence

- **24 backend tests passed:** 10 identity, 10 workspace/database, 4 actual gateway/proxy/rate-limit checks.
- **3 frontend unit tests passed** and TypeScript/production build passed.
- **9 deployment/infrastructure tests passed:** release promotion, rollback on failed startup, deleted-source isolation, private first-deploy secrets, hostname updates, unsafe archive rejection, and offline Azure configuration properties.
- **4 full browser scenarios passed against the Docker/PostgreSQL build:** desktop account/project/task/comment/profile/login lifecycle; sample project/search/drag-and-drop/export/deletion; mobile dark-mode task creation; two-account isolation. Last full run: `/tmp/taskflow-browser-docker.log` (4 passed in 28.9 seconds).
- All six test containers healthy. API smoke checks passed through Caddy after deployment and after database recovery.
- Complete stack stop/start preserved accounts, sessions, projects, tasks, labels, subtasks, and comments.
- Both databases were backed up, records changed, then restored; original values and the session were verified afterward. Only isolated `taskflow-qa` test volumes were used.
- Production frontend dependency audit: **0 reported advisories** at verification time. This is not a blanket security certification of all application/container dependencies.
- Caddy HTTP and HTTPS-hostname configuration validation passed; actual public certificate issuance still requires a public deployment.
- Both Compose files parse, shell scripts pass `bash -n`, Python scripts compile, and Kubernetes manifests render through Kustomize.

Earlier browser runs found selector issues and intermittent slow requests while image builds were active. The selectors were corrected; the final full suite passed against the warm, stable deployment without increasing assertion timeouts. Test process cleanup now stops child services correctly.

## Current step: review the Azure Repos pull request

The user refreshed Git authentication on 18 October 2026. Azure Repos `origin` points to `CaseStudy2026/TaskFlow`; its default branch is `develop`. The completed feature branch was pushed without force and its remote commit was verified as `dd1bd6d06d2bec0647af80a87af508a338345139`. Local and remote tracking were configured and showed zero commits ahead/behind. The shared `develop` and `main` branches remain unchanged; a pull request into `develop` is a separate next step.

The canonical pipeline now includes feature-branch CI triggers, and its stage indentation was corrected and parsed with Ruby/Psych. All nine deployment/infrastructure checks passed again. Azure pipeline **8 (`TaskFlow-Git-CI`)** was updated to `azure-pipelines.yml`, with its default build branch set to the current feature branch. Existing secret variables were preserved from the previous definition revision. Obsolete pipelines 5, 6, and 7 were disabled, retaining their history; pipelines 1–4 were already disabled.

Remote verification [build 64](https://dev.azure.com/CaseStudy2026/TaskFlow/_build/results?buildId=64) **completed successfully** for `dd1bd6d` on Microsoft-hosted agents. The Browser, Java, and Bundle jobs all passed, including production-container browser checks, backend/deployment tests, and release packaging. See [deployment/README.md](deployment/README.md#azure-devops-pipeline) for Azure Repos build-validation policies. Subsequent status-only checkpoints use `[skip ci]` to avoid queuing a duplicate full build.

### Resumed: release artifact and merge review

The user resumed work on 18 October 2026. Git fetch confirmed that the feature branch remains synchronized and that `develop` has no new commits outside the feature branch's ancestry. Build 64 was confirmed successful, with `backend-coverage` and `release` artifacts published. The release archive was downloaded from Microsoft's artifact service and inspected without extracting it: all 130 files match `git archive dd1bd6d` by path and SHA-256 content hash; no links, traversal paths, `.git`, or local `.env` entries were present. Only documentation changed after the tested code commit.

[Pull request 7](https://dev.azure.com/CaseStudy2026/TaskFlow/_git/TaskFlow/pullrequest/7) is open from `feature/kubernetes-integration` into `develop`, with the implementation, verification evidence, and deployment boundaries documented. No active PR existed before creation, and no enabled branch policies were returned by the account. The PR has not been merged; no shared target branch was changed. Next, review/complete this PR, switch the pipeline's default build branch to `develop` after the merge, and verify the resulting branch build before starting hosting work. Oracle provisioning and Kubernetes changes remain deferred.

CI reported nonblocking warnings: `NodeTool@0` is deprecated in favor of `UseNode@1`, and `ubuntu-latest` is scheduled to move to Ubuntu 26. Consider pinning a reviewed agent image on the next pipeline maintenance pass. The unused optional Azure deployment-stage fragment also has indentation issues to correct and validate before enabling it; it is not part of the running canonical verification pipeline.

The user has deferred hosting until repository/pipeline synchronization is complete. The intended free Kubernetes route is now Oracle Always Free plus self-managed K3s, subject to account eligibility and capacity. No Oracle account or VM has been configured; the existing Kubernetes manifests still need a K3s/Traefik configuration, published ARM images, and actual cluster verification. The Azure VM workflow below remains an optional paid hosting route.

## Optional Azure public hosting

1. Create/connect an Azure subscription and sign into Azure CLI. Azure DevOps itself is not the app host.
2. Use the documented provisioning command in [deployment/README.md](deployment/README.md), choosing a region/quota and restricted SSH CIDR.
3. Verify the public hostname, HTTPS, and smoke checks on that actual Azure deployment.
4. Configure the Azure DevOps deployment service connection/environment and run the optional deployment stage. The verification pipeline has been queued remotely; production delivery has not been configured or run.

No public URL or paid cloud resources have been created. The user has authorized Azure Repos synchronization; follow the deployment guide before provisioning hosting resources in an account.

## Optional scope / known boundaries

- This release provides personal workspaces. Team membership, file uploads, email verification, and password recovery are not implemented.
- Single-VM deployment has update interruptions; image rollback does not undo database migrations.
- Azure template checks are offline; Azure API/quota validation awaits an account. Kubernetes is rendered but not cluster-tested.
- Old Hibernate-managed/PostgreSQL 15 databases require a reviewed migration, not automatic reset/baselining. Existing user databases were not touched.

## Local preview and reproduction

The isolated verification stack is available at **http://127.0.0.1:18088** while Docker Desktop is running. It contains generated test accounts/data; create an account to explore it.

```sh
IMAGE_PREFIX=taskflow-qa WEB_PORT=18088 docker compose --env-file .env.example -p taskflow-qa up -d --no-build --wait
```

A normal local deployment uses `python3 scripts/setup.py` and `docker compose up -d --build --wait --wait-timeout 300`, then http://localhost:8088. The original `.env` was left unchanged.

## Repository / environment

- Repository: `/Users/kartik/Desktop/Task Management/TaskFlow`; branch `feature/kubernetes-integration`.
- Java 17: `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`. Maven otherwise defaults to Java 25 on this Mac.
- System Node is 18.15. Temporary Node 22 for verification: `/tmp/taskflow-tools/node_modules/node/bin/node`. Docker and CI use Node 22.
- Local Chrome testing uses `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH='/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'`. CI installs its matching Playwright Chromium.
- Local H2 data/logs are ignored under `.local-data/` and `.local-logs/`. Browser traces/results and QA backup artifacts are also local and untracked.
- User-owned tracked `.DS_Store` changes remain unstaged. Existing production image configuration was preserved in commit `459fe2f` before implementation.
- Git identity was not configured; commits use per-command `Codex <codex@openai.com>` without modifying repository/global identity.
- Verification logs: `/tmp/taskflow-tests.log`, `/tmp/taskflow-browser-docker.log`, `/tmp/taskflow-storage.log`, `/tmp/taskflow-audit.json`. Storage verification helper: `/tmp/taskflow-verify-storage.py`; its synthetic records were removed after the test.
