# SymphonyForgeOps 开发任务计划 Development Task Plan v0.1

## 版本索引 Version Index

- 最新版本：v0.1
- 日期：2026-05-23
- 目标：让下一台配置好环境的机器可以直接继续执行。
- 基线提交：
  - `23e2e58 Bootstrap SymphonyForgeOps mission control`
  - `8e7b7af Align ForgeOps with Symphony workflow model`

## 1. 当前状态

已完成：

- 独立项目位于 `D:\Projects\SymphonyForgeOps`。
- 已初始化 Git，分支为 `main`。
- 后端 Spring Boot 单服务 `forgeops-api` 已创建。
- 前端 Vue 3 控制台已创建。
- MySQL 8 Docker Compose 配置已创建。
- Maven 本地仓库配置为 `E:/repository`。
- 已新增 `WORKFLOW.md`。
- 已新增基础表和 Symphony runtime 预留表。
- Dashboard 当前使用内存数据展示。

尚未验证：

- 当前环境没有可用 Java 21 / Maven / Node / Docker，因此未跑完整构建。
- 数据库 migration 未在真实 MySQL 8 上执行验证。
- 前端 TypeScript 构建未执行。
- 后端测试未执行。

## 2. 下一环境第一步

进入项目：

```powershell
cd D:\Projects\SymphonyForgeOps
git status --short
git log --oneline -5
```

确认环境：

```powershell
java -version
mvn --version
node --version
npm --version
docker --version
docker compose version
```

期望：

- Java 21。
- Maven 3.9.x。
- Node 20 或 22。
- Docker Desktop 可用。
- MySQL 8 可由 Docker Compose 启动。

## 3. 串行任务总览

推荐先串行完成契约和持久化，再并行 UI / API / DevOps。

```text
ENV-001 环境验证
  -> DB-001 Flyway 验证
  -> API-001 Dashboard API 真实启动
  -> FE-001 前端构建修复
  -> DOC-001 文档归档确认
  -> PERSIST-001 Project Registry 持久化
  -> PERSIST-002 Work Order 持久化
  -> WF-001 WORKFLOW.md parser
  -> CMD-001 Command Template
  -> WS-001 Workspace Manager
  -> ORCH-001 Orchestrator skeleton
```

## 4. 任务明细

### ENV-001 环境验证

目标服务：全项目。

允许修改：

- 不修改代码，除非文档中的环境说明明显错误。

验证命令：

```powershell
java -version
mvn --version
node --version
npm --version
docker --version
docker compose version
```

验收标准：

- 所有命令可用。
- Java 主版本为 21。
- Maven 可以使用 `E:\repository`。

### DB-001 MySQL 与 Flyway 验证

目标服务：`forgeops-api`、`deploy`。

允许修改：

- `deploy/docker-compose.dev.yml`
- `.env.example`
- `backend/forgeops-api/src/main/resources/application.yml`
- `backend/forgeops-api/src/main/resources/db/migration/*.sql`

禁止修改：

- 前端 UI。
- 业务 API 结构。

执行：

```powershell
docker compose -f deploy\docker-compose.dev.yml config
docker compose -f deploy\docker-compose.dev.yml up -d forgeops-mysql
```

后端验证：

```powershell
cd backend
mvn -B -pl forgeops-api -am test
```

验收标准：

- Compose config 成功。
- MySQL 8 启动成功。
- Flyway migration 在空库执行成功。
- 后端测试通过或失败原因明确记录。

### API-001 后端启动与健康检查

目标服务：`forgeops-api`。

允许修改：

- `backend/forgeops-api/src/main/java/**`
- `backend/forgeops-api/src/test/java/**`

执行：

```powershell
cd backend
mvn -pl forgeops-api spring-boot:run
```

检查：

```powershell
curl http://localhost:8090/api/v1/health
curl http://localhost:8090/api/v1/dashboard
curl http://localhost:8090/actuator/health
```

验收标准：

- 三个接口正常返回。
- `/api/v1/*` 返回 `ApiResponse<T>`。
- 如果数据库不可用，错误必须可读，不应静默挂死。

### FE-001 前端构建与 Dashboard 修复

目标服务：`frontend`。

允许修改：

- `frontend/src/**`
- `frontend/package.json`
- `frontend/vite.config.ts`

禁止修改：

- 后端 API 结构，除非先更新契约文档。

执行：

```powershell
cd frontend
npm install
npm run build
npm run dev
```

验收标准：

- TypeScript 编译通过。
- Dashboard 可访问。
- 前端能通过 Vite proxy 调用 `/api/v1/dashboard`。
- 页面没有明显文字溢出和布局重叠。

### DOC-001 文档归档确认

目标文档：

- `docs/2026-05-23-SymphonyForgeOps-全量需求文档-full-prd-v0.1.md`
- `docs/2026-05-23-SymphonyForgeOps-开发任务计划-development-task-plan-v0.1.md`
- `docs/2026-05-23-SymphonyForgeOps-检查验收计划-check-plan-v0.1.md`
- `docs/2026-05-23-SymphonyForgeOps-环境交接说明-handoff-v0.1.md`

执行：

```powershell
git status --short
git diff --check
```

验收标准：

- 文档命名符合中文名 + EnglishName。
- 没有敏感 token。
- 下一环境可以按文档继续。

### PERSIST-001 Project Registry 持久化

目标服务：`forgeops-api`。

前置依赖：

- DB-001。
- API-001。

允许修改：

- `domain/model`
- `domain/repository`
- `infrastructure/persistence`
- `application/service`
- `interfaces/web`
- Flyway migration。

新增能力：

- `GET /api/v1/projects`
- `POST /api/v1/projects`
- `GET /api/v1/projects/{id}`
- `PUT /api/v1/projects/{id}`
- `POST /api/v1/projects/{id}/archive`

验收标准：

- Project 数据从 MySQL 读取。
- Dashboard projects 来自数据库。
- 单元测试覆盖 create/list/get。

### PERSIST-002 Work Order 持久化

目标服务：`forgeops-api`。

前置依赖：

- PERSIST-001。

新增能力：

- `GET /api/v1/work-orders`
- `POST /api/v1/work-orders`
- `GET /api/v1/work-orders/{id}`
- `POST /api/v1/work-orders/{id}/status`

验收标准：

- Work Order 状态可变更。
- 状态枚举集中定义。
- Dashboard openWorkOrderCount 来自数据库。

### WF-001 WORKFLOW.md Parser

目标服务：`forgeops-api`。

前置依赖：

- PERSIST-001。

新增能力：

- 读取项目级 `WORKFLOW.md`。
- 解析 YAML front matter。
- 提取 prompt body。
- 校验 tracker、polling、workspace、hooks、agent、codex。
- 保存到 `workflow_contracts`。

建议依赖：

- Jackson YAML 或 SnakeYAML。

验收标准：

- 支持无 front matter 的 fallback。
- 非 map YAML 返回 typed error。
- 缺少文件返回 `WORKFLOW_001` 类错误码。
- 路径解析符合 root containment。

### CMD-001 Command Template 与 Command Run

目标服务：`forgeops-api`。

前置依赖：

- PERSIST-001。

新增表：

- `command_runs`。

新增能力：

- CRUD command template。
- 执行非 destructive 命令。
- destructive 命令返回 need_approval。
- 保存 stdout、stderr、exit_code、started_at、finished_at。

验收标准：

- 执行目录必须在项目路径或 workspace 内。
- 日志脱敏。
- 命令超时可配置。

### WS-001 Workspace Manager

目标服务：`forgeops-api` 或后续 `forgeops-worker-service`。

前置依赖：

- WF-001。
- CMD-001。

新增能力：

- 创建 workspace root。
- 生成 workspace_key。
- git clone / fetch。
- branch checkout。
- hooks 执行。
- path safety。

验收标准：

- 非法 workspace_key 被清洗。
- `Path.toRealPath()` 校验 root containment。
- 不能删除 root 外路径。
- Hook 失败有明确状态。

### ORCH-001 Orchestrator Skeleton

目标服务：`forgeops-api` 或后续拆分 `forgeops-orchestrator-service`。

前置依赖：

- PERSIST-002。
- WF-001。
- WS-001。

新增能力：

- 定时 polling ready work orders。
- claimed 防重复。
- running 状态。
- retry queue。
- event log。

验收标准：

- 同一 work order 不会重复派发。
- 契约无效不派发。
- 异常进入 retry。
- Dashboard 展示 running/retry。

## 5. 可并行任务

以下任务在契约冻结后可并行：

| 任务 | 可并行条件 | 文件边界 |
| --- | --- | --- |
| UI-Projects | Project API 已冻结 | `frontend/src/views/projects/**` |
| UI-WorkOrders | Work Order API 已冻结 | `frontend/src/views/work-orders/**` |
| UI-Runs | Agent Run API 已冻结 | `frontend/src/views/agent-runs/**` |
| DEVOPS-001 | 服务名和环境变量冻结 | `deploy/**`, `.github/**` |
| TEST-001 | API/DDL 冻结 | `backend/**/src/test/**` |
| DOC-002 | 本轮任务完成 | `docs/**` |

## 6. 高冲突串行区

同一时间只允许一个 agent 修改：

- `backend/pom.xml`
- `backend/forgeops-api/pom.xml`
- Flyway migration。
- `deploy/docker-compose*.yml`
- `.github/workflows/ci.yml`
- `.env.example`
- 共享 response、error、status enum。
- `frontend/src/utils/request.ts`

## 7. 每个任务的 Handoff Packet

完成后必须输出：

```text
任务ID：
状态：DONE / DONE_WITH_CONCERNS / NEEDS_CONTEXT / BLOCKED
完成内容：
实际修改文件：
契约变更：
验证命令和结果：
未验证原因：
风险：
需要主编排 Agent 集成的事项：
```

