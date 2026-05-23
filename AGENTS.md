# SymphonyForgeOps Agent Rules

## Role

You are working on a personal AI development control console. Keep this project independent from any managed product repository.

## Project Boundary

- Do not move SymphonyForgeOps code into WikiForge or any managed project.
- Treat managed repositories as external targets.
- Store project metadata, work orders, runs, review results, and command templates in SymphonyForgeOps.
- Keep generated runtime logs and local data out of Git.

## Engineering Defaults

- Contract first for API, DTO, database migration, and command execution behavior.
- Local first for MVP.
- Prefer simple Spring Boot services before adding workflow engines.
- Prefer explicit command templates over hidden automation.
- Human approval is required before merging or running destructive commands.

## Verification

Backend:

```text
mvn -B test
```

Frontend:

```text
npm run build
```

Deployment:

```text
docker compose -f deploy/docker-compose.dev.yml config
```

