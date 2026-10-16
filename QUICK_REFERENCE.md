# TaskFlow quick reference

- Implementation status and next steps: [PROGRESS.md](PROGRESS.md)
- Local startup and checks: [README.md](README.md)
- Public deployment and Azure setup: [deployment/README.md](deployment/README.md)
- Local frontend: http://127.0.0.1:5173 via `python3 scripts/dev.py`
- Local Docker frontend: http://localhost:8088 via `docker compose up -d --build --wait`
- Internal ports: users 8080, tasks 8081, gateway 8090. Production exposes web ports 80/443 only.
- No public deployment has been created yet.
