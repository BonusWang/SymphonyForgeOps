# SymphonyForgeOps 开发者日志 Developer Log

## Version Index

- 最新版本：v1.0-dev
- 日期：2026-05-24
- 最新小节：`2026-05-24 v1.0 Java 17 与本地 worker 闭环`

## 2026-05-23 项目启动

项目目标确定为独立个人 AI 研发控制台。

初始骨架：

- Spring Boot `forgeops-api`
- Vue 3 Dashboard
- MySQL Flyway migration
- Docker Compose
- `WORKFLOW.md`
- 基础 PRD、架构、开发计划、检查计划、环境交接说明

GitHub 状态：

- 本地工程最初和远端 `Initial commit` 没有共同祖先。
- 已按用户选择通过普通 merge 合入远端 `main`。
- 冲突仅 README；保留本地完整说明，并保留远端“脚手架”语义。
- 已推送 `main`，未使用 force push。

## 2026-05-23 环境发现

本机情况：

- Docker 可用。
- WikiForge MySQL 容器 `wikiforge-mysql` 在 3306 healthy。
- 已在该 MySQL 中创建 `forgeops` schema 和 `forgeops` 用户。
- Shell 默认 Java 是 1.8；当时按旧计划不满足 Java 21 要求。
- 未发现精确 JDK 21；后来项目基线调整为 Java 17，不再使用 Trae bundled JDK 25。
- Maven 可用，但用户级 `settings.xml` 指向不可达私有 mirror `nexus.minshenglife.com`。
- 已用临时 Maven settings 验证依赖可从公开仓库解析。

## 2026-05-23 P1 WIP 暂停

曾按旧计划启动 P1 持久化控制平面：

- 写入 `ControlPlaneApiTests`，RED 成功失败在 `/api/v1/projects` 404。
- 开始新增 DTO、ProjectService、migration。

用户随后要求先详细学习 openai/symphony 和 WikiForge，并重调开发计划和项目规范。

处理：

- 暂停 P1 代码推进。
- 将所有 P1 半成品暂存到 stash：`codex-p1-wip-before-wikiforge-replan`。
- 当前工作区回到干净文档重调状态。

## 2026-05-23 openai/symphony + WikiForge 模式重调

学习结论：

### openai/symphony

Symphony 的价值不是 UI，而是服务规范：

- long-running orchestrator
- issue tracker polling
- isolated workspace per issue
- repository-owned `WORKFLOW.md`
- typed config and defaults
- dynamic reload
- single-authority runtime state
- retry/reconcile
- agent runner session metadata
- structured observability

对 SymphonyForgeOps 的调整：

- 以 GitHub/manual tracker 为优先，不绑定 Linear。
- Java + Spring Boot 实现，不照搬 Elixir。
- MySQL 作为运行账本，不采用无数据库长期状态。
- workspace/command/agent 执行必须显式安全边界。

### WikiForge

WikiForge 的可复制价值是工程治理方式：

- `docs/current` 当前事实。
- `docs/archive` 快照。
- `docs/superpowers/plans` 可执行 Work Order。
- 项目内 AI Skill。
- AGENTS / WORKFLOW 双入口规则。
- 开发者日志。
- 架构决策记录。
- 阶段路线图和当前执行指针。
- Agent Team 文件化协作。
- T0-T4 验证门禁。

本轮已新增：

- `docs/current/架构决策-DECISIONS.md`
- `docs/current/技术架构-SymphonyForgeOps-technical-architecture.md`
- `docs/current/2026-05-23-项目整体计划-SymphonyForgeOps-project-roadmap.md`
- `docs/current/开发者日志-SymphonyForgeOps-developer-log.md`
- `docs/ai-skills/symphonyforgeops-development/SKILL.md`
- `docs/ai-skills/symphonyforgeops-development/references/*.md`

本轮已更新：

- `AGENTS.md`

下一步：

- 更新 README 和 `.env.example`，明确共享 MySQL schema 模式。
- 创建首个 archive index。
- 提交推送 S0 文档治理变更。
- 再恢复 P1 WIP，并按新项目 Skill 拆分重做持久化切片。

## 2026-05-24 v1.0 Java 17 与本地 worker 闭环

架构调整：

- 项目 Java 基线从 Java 21 调整为 Java 17，使用本机 Corretto 17 作为稳定执行环境。
- 默认 datasource 改为复用 WikiForge MySQL 3306 的 `forgeops` schema。
- Docker Compose 继续保留独立 MySQL 3308 模式。

本轮实现：

- Project Registry、Work Order、Dashboard 从 MySQL 读取。
- `WORKFLOW.md` reload 保存 typed workflow contract。
- Workspace root containment、workspace key 清洗、破坏性命令审批 gate。
- Orchestrator dispatch、worker registration、heartbeat、capacity、assignment、run event。
- Agent adapter config、GitHub link、review finding、human decision、artifact 表。
- 前端 Dashboard 展示 workers、GitHub links、review findings、human decisions。

验证：

- `mvn -B -s <temp-settings> -pl forgeops-api -am test` 通过，6 tests。
- `npm install` 通过。
- `npm run build` 通过。
- `docker compose -f deploy/docker-compose.dev.yml config` 通过。
