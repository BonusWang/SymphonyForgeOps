# SymphonyForgeOps

SymphonyForgeOps is a personal AI development control console for managing independent projects, work orders, agent runs, tests, and code review gates.

It is intentionally separate from WikiForge. WikiForge remains the knowledge product; SymphonyForgeOps is the development operating layer that can manage WikiForge, team-workload, and future independent repositories.

## Positioning

```text
GitHub repos
  -> Project Registry
  -> Work Orders
  -> Implementation Runs
  -> Test Runs
  -> Review Items
  -> Human Approval
```

## Technology Stack

- Backend: Java 21, Spring Boot 3.x, Maven
- Frontend: Vue 3, Vite, TypeScript, Element Plus, Pinia, Axios
- Database: MySQL 8
- Deployment: local first, Docker Compose ready

## Local Paths

Recommended local source path:

```text
D:\Projects\SymphonyForgeOps
```

Maven local repository:

```text
E:\repository
```

The backend Maven wrapper config is stored in `backend/.mvn/maven.config`, so Maven commands run from `backend` will use that repository automatically.

## MVP Scope

- Register independent repositories.
- Track work orders and implementation runs.
- Store commands for build, test, review, and local startup.
- Show a dashboard for project health and review status.
- Prepare the domain model for future Codex, Claude Code, OpenHands, PR-Agent, and GitHub integration.

## Suggested First Projects

- WikiForge: knowledge product and local-first knowledge management app.
- team-workload: workload/project management app.
- SymphonyForgeOps: the control console itself.

## Local Development

Backend:

```bash
cd backend
mvn -B test
mvn -pl forgeops-api spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Docker:

```bash
docker compose -f deploy/docker-compose.dev.yml config
```

## MySQL 8

Default local Docker settings:

```text
Database: forgeops
User: forgeops
Password: forgeops_dev_password
Port: 3308
```

Runtime environment variables:

```text
FORGEOPS_DATASOURCE_URL=jdbc:mysql://localhost:3308/forgeops?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
FORGEOPS_DATASOURCE_USERNAME=forgeops
FORGEOPS_DATASOURCE_PASSWORD=forgeops_dev_password
```

When deploying to another computer, keep the same variable names and change only host, port, username, and password.
