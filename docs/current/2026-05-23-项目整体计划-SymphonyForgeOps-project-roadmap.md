# SymphonyForgeOps 项目整体计划 Project Roadmap

## Version Index

- 最新版本：v1.0-dev
- 日期：2026-05-24
- 当前指针：(x) V1 Local Remote Worker
- 目标：按 WikiForge 的项目模式，把 SymphonyForgeOps 发展为完整非 MVP 研发控制台，但每轮只做可验证切片。

## Current State

- [x] GitHub `main` 已合并远端 Initial commit 并普通 push。
- [x] 当前项目骨架已建立：Spring Boot API、Vue Dashboard、MySQL migration、Docker Compose。
- [x] 已补充 openai/symphony + WikiForge 学习后的项目治理规则。
- [x] 项目 Java 基线调整为 Java 17，并使用 Temurin/Corretto 17 路径验证。
- [ ] 宿主机默认 Java 仍可能是 8；验证时需要显式 Java 17 或 `maven:3.9.9-eclipse-temurin-17` 容器。
- [x] Dashboard 已从 MySQL 聚合读取。
- [x] P1 WIP 已恢复并按 v1 路线重做。

## Stage Roadmap

### (x) S0 Governance / Environment

目标：把项目规则、文档结构、环境基线和执行方式调整为 WikiForge 模式。

- [x] 建立 `docs/current/` 主线文档。
- [x] 建立 `docs/ai-skills/symphonyforgeops-development/` 项目内 Skill。
- [x] 更新 `AGENTS.md`。
- [ ] 更新 README 环境说明：共用 WikiForge MySQL 3306 + `forgeops` schema。
- [ ] 更新 `.env.example` 为共享 MySQL 默认值，同时保留独立 Docker MySQL 说明。
- [ ] 创建 `docs/archive/2026-05-23/` 首个归档索引。

验证门禁：T0。

### (x) S1 Persistent Control Plane

目标：Project Registry、Work Order、Command Template、Review Item 和 Dashboard 全部从 MySQL 读取。

范围：

- `GET/POST/PUT/archive /api/v1/projects`
- `GET/POST/status /api/v1/work-orders`
- command template CRUD 最小能力
- review item list / decision 最小能力
- Dashboard 统计从数据库聚合

约束：

- 先写 MockMvc RED 测试。
- Flyway 只补 S1 必要字段和表。
- 先保留单服务 `forgeops-api`。

验证门禁：T1 + MySQL Flyway 空库迁移。

### (x) S2 Workflow Contract

目标：实现 `WORKFLOW.md` parser、验证、reload 和 last-known-good 账本。

范围：

- YAML front matter + prompt body。
- 支持 no-front-matter fallback。
- 非 map YAML typed error。
- `tracker/polling/workspace/hooks/agent/codex/github/review/commands` typed config。
- reload API。
- `workflow_contracts` 保存当前版本、状态、错误。

验证门禁：T1。

### (x) S3 Command And Workspace Safety

目标：命令模板、命令运行、workspace key、root containment、hook 机制落地。

范围：

- `command_runs`
- `isolated_workspaces`
- non-destructive command run
- destructive command returns `requires_approval`
- workspace path sanitizer
- before_run / after_run hook 记录

验证门禁：T1 + targeted path-safety tests。

### (x) S4 Orchestrator Skeleton

目标：实现单一调度权威的最小状态机。

范围：

- ready queue polling
- claimed 防重复
- running/retry/completed bookkeeping
- continuation retry
- terminal/non-active reconcile
- event log

v1.0 已支持 Manual 命令执行路径，并在 Codex 工单中调用本机 Codex CLI。

验证门禁：T1。

### (x) S5 Agent Runtime Adapters

目标：接入 Manual、Codex、Claude Code、OpenHands 的统一 adapter registry。

范围：

- adapter config
- session metadata
- event stream
- usage/cost
- artifact summary

Codex adapter 当前采用本机 `codex exec`；Claude Code / OpenHands 继续先保存配置和 smoke 状态。

验证门禁：T1 + real-integration smoke 可跳过但必须记录原因。

### (x) S6 GitHub And Review Gate

目标：GitHub issue/PR 同步、review gate 和 human decision 闭环。

范围：

- GitHub issue import
- GitHub PR link
- check status sync
- review findings
- human decision
- create PR / update PR 走 GitHub CLI 或 GitHub app 能力

验证门禁：T1 + GitHub smoke。

### (x) S7 Observability And Knowledge Handoff

目标：完善 Dashboard、日志、handoff artifact，并与 WikiForge 知识沉淀联动。

范围：

- run event timeline
- retry queue view
- command logs
- review queue
- completion report
- optional WikiForge MCP/API write

验证门禁：T2 + browser check。

### (x) V1 Multi-Machine Runtime

目标：远程 worker、主机容量、长运行任务和更完整的自动化。

进入条件：

- S1-S7 已形成可用本地闭环。
- workspace/command/review 安全边界通过验证。
- v1.0 范围限定为本机 SSH worker 语义和单 worker capacity dispatch。

## Parallel Work Rules

允许并行前必须冻结：

- API path
- DTO
- status enum
- migration number
- error codes
- allowed files
- verification commands

高冲突串行区见 `AGENTS.md`。

## Next Recommended Work Order

`V1-WIKIFORGE-CODEX-VERIFY`：用 WikiForge 受管项目创建验证工单，派发给 Codex agent，并确认最终结果回写 Dashboard/run events。
