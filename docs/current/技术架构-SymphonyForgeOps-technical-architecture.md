# SymphonyForgeOps 技术架构 Technical Architecture

## 1. 架构目标

SymphonyForgeOps 要把“监督每个 AI 编码会话”升级为“管理可追踪、可验证、可审核的研发工作”。

目标：

- 本地优先运行。
- 管理多个独立 GitHub 仓库。
- 通过 `WORKFLOW.md` 读取每个仓库的运行契约。
- 为每个 Work Order 创建可隔离、可恢复、可审计的执行记录。
- 支持命令、测试、review、human decision 和 handoff 的完整账本。
- 采用 WikiForge 的文档、工程、测试、发布和 Agent Team 协作方式。

## 2. 总体架构

v1.0：

```text
User
  -> frontend
  -> forgeops-api
       -> Control Plane APIs
       -> Workflow Contract APIs
       -> Work Order APIs
       -> Command / Review / Dashboard APIs
       -> Worker / Orchestrator APIs
       -> Codex CLI adapter
       -> MySQL
```

v1.x 演进后：

```text
frontend
  -> forgeops-api
       -> project/work-order/review/human-decision/query APIs
       -> MySQL

forgeops-worker-service
  -> workspace manager
  -> command runner
  -> agent subprocess adapter
  -> filesystem safety

forgeops-orchestrator-service
  -> polling
  -> dispatch
  -> retry
  -> reconcile
  -> run event stream

external integrations
  -> GitHub issues / PRs / checks
  -> Codex / Claude Code / OpenHands
  -> PR-Agent or review adapter
  -> WikiForge MCP/API knowledge handoff
```

## 3. Core Modules

### 3.1 Project Registry

记录受管项目：

- project key
- GitHub owner/repo
- repo URL
- local path
- default branch
- work branch prefix
- backend/frontend/deploy directories
- workflow path
- workspace root
- default commands
- status

### 3.2 Workflow Contract

读取每个项目的 `WORKFLOW.md`：

- YAML front matter
- prompt body
- tracker/polling/workspace/hooks/agent/codex/github/review/commands 配置
- last-known-good config
- reload error

### 3.3 Work Order

把需求变成可执行对象：

- source: manual / github_issue / github_pr / schedule / handoff
- title / requirement / acceptance criteria
- target branch / base branch
- allowed files / forbidden files
- verification commands
- status
- priority / risk / human approval

### 3.4 Workspace Manager

负责安全隔离：

- workspace key 清洗。
- workspace root containment。
- clone/fetch/checkout。
- hook execution。
- cleanup safety。

v1.0 已实现 root containment 和最小 workspace API；真实 clone/fetch/checkout 进入 v1.x。

### 3.5 Command Runner

命令执行必须显式模板化：

- command type
- command text
- working directory
- destructive flag
- requires approval
- timeout
- stdout/stderr/exit code

Destructive command 不自动执行。

### 3.6 Orchestrator

受 openai/symphony 启发：

- 轮询 tracker 或本地 ready queue。
- 按优先级和并发限制 dispatch。
- claimed 防重复派发。
- running/retry/completed 状态管理。
- terminal/non-active state reconcile。
- normal exit 进入 continuation 或 waiting_review，而不是自动 done。

v1.0 当前实现：`implementationAgent=Manual` 继续执行工单命令；`implementationAgent=Codex` 通过本机 `codex exec` 调用 Codex CLI，stdout/stderr、exit code、run event 和 agent summary 全部落库。

### 3.7 Worker Runtime

v1.0 worker 模型：

- worker registration
- heartbeat
- capacity / current runs
- assignment
- stdout / stderr / exit code
- run event log

本机 SSH worker 使用 `protocol=ssh`、`host=localhost` 作为验收目标；复杂远程自动安装和主机池调度进入 v1.x。

Codex 工单优先选择 worker key、display name 或 protocol 中包含 `codex` 的在线 worker；没有专用 Codex worker 时才降级到任意可用 worker。

### 3.8 Agent Runtime Adapter

适配：

- Manual adapter
- Codex adapter
- Claude Code adapter
- OpenHands adapter

Codex adapter 当前使用本机 CLI：

- 从受管项目 root 运行 `codex exec`。
- prompt 由 project/work order/test command 渲染。
- 默认 timeout 为 300 秒，可用 `FORGEOPS_CODEX_TIMEOUT_SECONDS` 调整。
- `FORGEOPS_CODEX_COMMAND_TEMPLATE` 用于 deterministic smoke 和测试替身。

统一记录：

- session id
- thread id
- turn id
- pid / external id
- usage / cost
- events
- changed files
- artifacts

### 3.9 Review Gate

Review Gate 读取：

- Work Order
- diff
- command/test results
- agent logs
- changed files

输出：

- risk level
- findings
- open questions
- recommendation
- human decision options

## 4. 数据流

### 4.1 手动 Work Order

```text
User creates work order
  -> Work Order saved
  -> status ready
  -> Orchestrator claims
  -> Workspace created
  -> Agent/Manual run
  -> Test Gate
  -> Review Gate
  -> Human Decision
  -> Handoff artifact
```

### 4.2 GitHub Issue

```text
GitHub issue
  -> tracker sync
  -> Work Order
  -> workspace branch
  -> agent run
  -> PR creation / update
  -> checks and review
  -> human approval
```

### 4.3 Knowledge Handoff To WikiForge

```text
Completion report
  -> ForgeOps artifact
  -> optional WikiForge MCP/API write
  -> developer log / architecture decision / project note
```

## 5. Database Strategy

MySQL 是控制平面和运行账本。

阶段表：

- S1: managed_projects, command_templates, work_orders, review_items, dashboard queries
- S2: workflow_contracts
- S3: command_runs, isolated_workspaces
- S4: agent_runs, run_attempts, run_events, retry_queue
- S5: agent_adapter_configs
- S6: github_links, review_findings, human_decisions
- S7: artifacts
- V1: worker_hosts, worker_heartbeats, worker_assignments

不在早期创建所有长期表。

## 6. Technology Stack

- Java 17 target.
- Spring Boot 3.x.
- Maven.
- MySQL 8.
- Flyway.
- Spring MVC.
- Spring Validation.
- Spring Boot Actuator.
- Vue 3.
- Vite.
- TypeScript.
- Element Plus.
- Pinia.
- Axios.
- Docker Compose.

MVP 暂不引入：

- Nacos
- Kafka
- Redis
- XXL-JOB
- Service Mesh
- 完整工作流引擎
- 多租户权限系统

## 7. Safety

- Workspace root containment before every filesystem operation.
- Command cwd containment before process launch.
- Destructive command requires approval.
- Token and API key masking in logs.
- No managed repository source code copied into SymphonyForgeOps.
- No automatic merge by default.
- No workspace deletion without real-path confirmation.

## 8. Verification

Stage completion requires matching gate:

- T0 docs/Git hygiene.
- T1 backend tests.
- T2 frontend build.
- T3 Docker config.
- T4 stage-level smoke.

Backend verification must use Java 17. When the host shell still resolves `java`/`mvn` to Java 8, run backend tests through `maven:3.9.9-eclipse-temurin-17` and connect to `host.docker.internal:3306/forgeops_test`.

后端测试默认使用 `forgeops_test` schema，不使用共享开发 schema `forgeops`。

Every behavior change must have failing test evidence before implementation.
