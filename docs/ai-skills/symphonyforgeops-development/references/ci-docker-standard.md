# CI And Docker Standard

## Goals

- Every service can be tested and packaged independently.
- Docker images do not contain local Windows paths, tokens, API keys, workspaces, managed repositories, or database data.
- Compose config can be validated before service startup.

## Local Environment

Preferred local defaults:

- Java 21 LTS.
- Maven 3.9.x with `E:/repository`.
- Node 20 or 22 for predictable Vite builds.
- MySQL 8 on localhost.
- This machine may reuse WikiForge's MySQL container on port 3306 with a separate `forgeops` schema.

## Commands

Backend:

```powershell
cd backend
mvn -B -pl forgeops-api -am test
```

Frontend:

```powershell
cd frontend
npm install
npm run build
```

Docker:

```powershell
docker compose -f deploy/docker-compose.dev.yml config
```

## Secrets

- `.env` is local only.
- `.env.example` may contain development defaults, never real tokens.
- GitHub token and model API keys come from environment variables or host secret storage.
- Logs must mask token-like values.

## Migration Checks

Flyway migration must be tested against an empty MySQL schema before a stage is complete. If using the shared WikiForge MySQL instance, use schema `forgeops` and keep WikiForge tables untouched.
