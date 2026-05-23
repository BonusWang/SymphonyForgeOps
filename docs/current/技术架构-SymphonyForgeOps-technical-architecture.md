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

MVP 初期：

```text
User
  -> frontend
  -> forgeops-api
       -> Control Plane APIs
       -> Workflow Contract APIs
       -> Work Order APIs
       -> Command / Review / Dashboard APIs
       -> MySQL
```

演进后：

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

第一阶段只建表和 API；真实执行进入 worker 阶段。

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

### 3.7 Agent Runtime Adapter

适配：

- Manual adapter
- Codex adapter
- Claude Code adapter
- OpenHands adapter

统一记录：

- session id
- thread id
- turn id
- pid / external id
- usage / cost
- events
- changed files
- artifacts

### 3.8 Review Gate

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
- S5: agent_sessions, adapter_configs, usage_records
- S6: github_links, check_runs, review_findings, human_decisions
- S7: artifacts, knowledge_exports

不在早期创建所有长期表。

## 6. Technology Stack

- Java 21 LTS target.
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

Every behavior change must have failing test evidence before implementation.
