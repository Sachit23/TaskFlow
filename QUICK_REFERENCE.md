# TaskFlow quick reference

- Start normally: `python3 scripts/setup.py`, then `docker compose up -d --build --wait --wait-timeout 300`
- Local app: http://localhost:8088
- Current isolated verification preview: http://127.0.0.1:18088
- Development without Docker: `python3 scripts/dev.py` → http://127.0.0.1:5173
- Public hosting and Azure setup: [deployment guide](deployment/README.md)
- Completed checks and remaining account setup: [PROGRESS.md](PROGRESS.md)
- Backend verification: `mvn -f backend/pom.xml clean verify` with Java 17
- Frontend verification: `cd frontend`, then `npm test` and `npm run build` with Node 22
- Deployment checks: `python3 -m unittest discover -s scripts/tests -v`
- Database backup: `bash scripts/backup.sh`
- Restore: `bash scripts/restore.sh BACKUP_DIRECTORY --confirm`
- Production exposes web ports 80/443 only; Java services and databases are private.
- No public deployment or remote Git push has been performed yet.
