# SymphonyForgeOps 架构决策记录 Architecture Decisions

## 2026-05-23 项目定位

SymphonyForgeOps 是独立的个人 AI 研发控制台，用于管理多个外部 Git 仓库的研发工作。

它管理开发过程，不承载 WikiForge 的知识库业务，不复制受管项目源码。

核心对象：

- Managed Project
- Workflow Contract
- Work Order
- Isolated Workspace
- Command Template / Command Run
- Agent Run / Run Event
- Test Gate
- Review Item
- Human Decision
- GitHub Link
- Handoff Artifact

## 2026-05-23 思想来源

### openai/symphony

采用：

- `WORKFLOW.md` 作为仓库级运行契约。
- Orchestrator 是 polling、claim、dispatch、retry、reconcile 的单一权威。
- 每个 work order / issue 映射到隔离 workspace。
- Workspace lifecycle hooks。
- Agent runner 记录 session/thread/turn、usage、event、结果。
- 正常 agent 退出不等于完成，仍需 continuation、测试、review 或 human decision。
- invalid workflow reload 不击穿服务，保留 last-known-good config。
- 结构化 observability 是基础能力。

不采用：

- 不照搬 Elixir 参考实现。
- 不绑定 Linear，优先 GitHub + manual tracker。
- 不采用无数据库作为长期运行账本。
- 不默认高信任无提示执行 destructive command。

### WikiForge

采用：

- Java 17 + Spring Boot 3.x + Maven。
- Vue 3 + Vite + TypeScript + Element Plus + Pinia + Axios。
- MySQL 8 + Flyway。
- `docs/current` 当前事实、`docs/archive` 快照、`docs/superpowers/plans` 可执行 Work Order。
- 项目内 AI Skill。
- 开发者日志和阶段路线图同步更新。
- Contract First、TDD、验证门禁、Handoff Packet。
- Agent Team 文件化协作。

## 2026-05-23 服务拆分策略

采用渐进式少服务架构。

阶段：

- MVP0/MVP1：单后端 `forgeops-api` + `frontend` + MySQL。
- MVP2/MVP3：仍在 `forgeops-api` 内完成 workflow、command、workspace 的契约和安全边界。
- MVP4：拆出 `forgeops-worker-service`，处理 workspace 和命令执行。
- MVP5：拆出 `forgeops-orchestrator-service`，负责 polling、dispatch、retry、reconcile。
- V1：视需要增加 remote worker、gateway、model/usage service。

原因：

- 当前最重要的是把控制平面和运行账本建稳。
- workspace/命令/agent 执行有更高安全风险，适合在契约稳定后独立为 worker。
- orchestrator 需要单一权威状态，不应过早和 CRUD 控制面混成不可拆的巨型服务。

## 2026-05-23 数据访问决策

MVP 先使用 Spring JDBC / JdbcTemplate 或清晰 SQL 实现最小持久化；当 CRUD、分页和状态流转复杂后，可按 WikiForge 模式引入 MyBatis-Plus。

约束：

- Flyway SQL 是 DDL 事实来源。
- 每张表必须声明服务/领域归属。
- 不一次性创建所有 P7 长期表。
- JSON 字段使用 MySQL JSON 时必须在 DTO 和测试中固定格式。

## 2026-05-23 数据库与本机环境

本机开发可复用 WikiForge 的 MySQL 8 容器：

```text
Host: localhost
Port: 3306
Schema: forgeops
User: forgeops
```

Docker Compose 可以保留独立 MySQL 服务用于干净部署，但 README 和 `.env.example` 必须说明两种模式。

## 2026-05-23 GitHub 发布策略

远端 `main` 已通过普通 merge 接收本地工程提交，不使用 force push。

后续：

- 阶段任务使用短生命周期 `codex/{task-key}` 分支。
- 验证通过后合入 `main` 并推送。
- release/tag 可由 Agent 在验证通过后执行，除非用户另行指定。

## 2026-05-23 当前阻塞

- 项目 Java 基线已调整为 Java 17，优先使用本机 Corretto 17。
- 不使用 Trae bundled JDK 25 作为项目执行环境。
- 用户级 Maven settings 指向不可达私有 mirror；验证时需使用临时 settings 或修复本机 Maven 配置。

## 2026-05-24 v1.0 单服务运行层决策

v1.0 保持 `forgeops-api` 单服务，但已经把运行层账本落库：

- `workflow_contracts`
- `command_runs`
- `isolated_workspaces`
- `agent_runs`
- `run_events`
- `worker_hosts`
- `worker_heartbeats`
- `worker_assignments`
- `agent_adapter_configs`
- `github_links`
- `review_findings`
- `human_decisions`
- `artifacts`

远程 worker 的正式 v1 验收目标采用本机 SSH worker 语义：worker 使用 `protocol=ssh`、`host=localhost` 注册，当前实现以本机 shell 执行作为可测试的本地 worker 执行路径。复杂主机池调度、远程自动安装、透明故障迁移进入 v1.x。
