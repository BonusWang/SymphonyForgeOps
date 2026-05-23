# SymphonyForgeOps

脚手架已升级为个人 AI 研发控制台工程。

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

- Backend: Java 17, Spring Boot 3.x, Maven
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

## v1.0 Scope

- Register independent repositories and work orders in MySQL.
- Reload `WORKFLOW.md` into typed workflow contracts.
- Store commands, command runs, isolated workspaces, review gates, GitHub links, and human decisions.
- Dispatch a ready work order through the orchestrator to a registered local SSH-style worker target.
- Show dashboard sections for projects, work orders, agent runs, workers, GitHub links, review findings, and human decisions.
- Keep Codex, Claude Code, and OpenHands as adapter configs until their external smoke checks are explicitly enabled.

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

Preferred local development on this machine reuses the WikiForge MySQL container and creates a separate schema:

```text
Host: localhost
Port: 3306
Database: forgeops
User: forgeops
Password: forgeops_dev_password
```

Runtime environment variables:

```text
FORGEOPS_DATASOURCE_URL=jdbc:mysql://localhost:3306/forgeops?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
FORGEOPS_DATASOURCE_USERNAME=forgeops
FORGEOPS_DATASOURCE_PASSWORD=forgeops_dev_password
```

Docker Compose can still start an isolated ForgeOps MySQL service when a clean database is preferred:

```text
Database: forgeops
User: forgeops
Password: forgeops_dev_password
Port: 3308
```

Isolated Docker database runtime variables:

```text
FORGEOPS_DATASOURCE_URL=jdbc:mysql://localhost:3308/forgeops?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
FORGEOPS_DATASOURCE_USERNAME=forgeops
FORGEOPS_DATASOURCE_PASSWORD=forgeops_dev_password
```

When deploying to another computer, keep the same variable names and change only host, port, username, and password.
