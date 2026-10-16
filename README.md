# TaskFlow

A focused project and task workspace built with React, TypeScript, Spring Boot, PostgreSQL, and an API gateway. Includes private accounts, projects, Kanban and list views, priorities, due dates, subtasks, comments, activity history, dark mode, and CSV exports.

**Implementation is checkpointed, not yet fully release-verified.** See [PROGRESS.md](PROGRESS.md) for completed checks and the exact remaining work.

## Run locally

Install Java 17, Maven, Python 3, and Node 22. Then:

```sh
cd frontend
npm ci
cd ..
python3 scripts/dev.py
```

Open http://127.0.0.1:5173 and create an account. Local mode uses persistent H2 databases under `.local-data/`. Passwords and session tokens are never preconfigured; there are no shared demo credentials. Sample projects are added only when requested inside the app.

## Docker

With Docker Desktop or Docker Engine running:

```sh
python3 scripts/setup.py
docker compose up -d --build --wait
python3 scripts/smoke.py http://localhost:8088
```

Open http://localhost:8088. Database volumes survive normal container restarts and `docker compose down`. Do not use `down -v` unless you intend to remove the databases.

## Public deployment

See [deployment/README.md](deployment/README.md). The production Compose file serves the app through Caddy on ports 80/443 and obtains HTTPS certificates for the configured public hostname. Java services and databases are private to the Docker network.

Azure DevOps runs CI/CD; an Azure subscription or another server is still required to host the application. Infrastructure and release configuration are included but have not yet been exercised against a live Azure account.

## Checks

```sh
mvn -f backend/pom.xml clean verify
cd frontend
npm ci
npm run build
npm test
npx playwright install chromium
npm run test:e2e
```

Select Java 17 explicitly if Maven uses a different JDK. Playwright starts the real local stack and requires Node 20.19 or newer. The first database-backed backend suite and frontend checks pass; browser, container, and Azure verification remain pending at this checkpoint.

## Architecture

```text
Browser → Caddy (web + HTTPS) → API gateway
                               ├─ User service → identity PostgreSQL
                               └─ Task service → workspace PostgreSQL
                                      └─ session validation → User service
```

Sessions use random bearer tokens stored in the browser's session storage, with hashed tokens and a seven-day expiry in the identity database. The task service validates sessions and enforces ownership for every workspace operation. Password changes revoke other sessions. Every account has its own personal workspace; shared team membership, file attachments, email verification, and password recovery are not implemented.

Flyway owns schema changes; production uses Hibernate validation. Existing databases from the earlier version require a reviewed migration before adoption. See the checkpoint notes before connecting an old database.
