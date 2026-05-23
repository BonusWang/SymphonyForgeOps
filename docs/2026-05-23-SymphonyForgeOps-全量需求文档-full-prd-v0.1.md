# SymphonyForgeOps 全量需求文档 Full PRD v0.1

## 版本索引 Version Index

- 最新版本：v0.1
- 日期：2026-05-23
- 范围：非 MVP 限定的完整产品需求，用于后续长期开发规划。
- 参考来源：`openai/symphony` 规范、WikiForge 架构/开发规范/需求/开发者日志。

## 1. 产品背景

用户已经拥有多个独立项目，例如 WikiForge、team-workload，以及未来可能出现的后端、前端、知识库和自动化项目。继续把研发 agent 管理能力放在某一个业务项目内部，会导致职责混乱：

- WikiForge 是知识产品本体，不应该承载所有项目的研发调度。
- 每个独立项目都有自己的技术栈、命令、分支、测试方式和审核规则。
- AI agent 参与开发时，需要稳定的任务、日志、验证、审核和交接账本。
- 个人开发需要从“监督每个 agent 会话”升级为“管理工作单、运行、证据和审核状态”。

因此，SymphonyForgeOps 定位为独立的个人研发控制台，未来可以管理多个项目的开发、测试、代码审核、文档归档和 agent 执行。

## 2. 产品定位

SymphonyForgeOps 是面向个人开发者的 AI 研发操作系统。

它不是单纯的聊天 UI，也不是通用项目管理系统，而是：

- 项目注册中心：记录每个仓库、分支、技术栈、命令和工作流契约。
- Work Order 控制台：把需求转成可执行、可审核、可追踪的工作单。
- Agent Orchestrator：读取工作单，创建隔离 workspace，调度 Codex / Claude / OpenHands 等执行器。
- Test Gate：统一运行构建、测试、Docker、CI 检查。
- Review Gate：接入 PR-Agent 或自研 Review Agent，形成人工合并前的审核结论。
- Observability Surface：展示运行状态、日志、重试、失败原因、token/cost、产物和证据。
- Handoff Ledger：保存每次工作从需求、计划、执行、测试、审核到交接的完整记录。

## 3. 设计思想来源

### 3.1 openai/symphony

需要吸收：

- `WORKFLOW.md` 是仓库级工作契约，包含 YAML front matter 和 prompt body。
- Orchestrator 是单一调度权威，负责 polling、dispatch、retry、reconcile。
- 每个 issue/work order 映射到隔离 workspace，agent 只在该目录内执行。
- workspace 生命周期支持 hooks：`after_create`、`before_run`、`after_run`、`before_remove`。
- Agent Runner 负责创建 workspace、渲染 prompt、启动 coding agent、回传事件。
- 正常退出不等于完成，可以进入 continuation retry 或 human review。
- Observability 至少需要结构化日志，推荐提供 dashboard/status surface。
- 配置应支持动态 reload，非法配置不得击穿服务。
- 安全姿态必须显式记录，不能假装所有 agent 操作天然安全。

不直接照搬：

- 不绑定 Linear；优先支持 GitHub Issues / GitHub PR，保留 Linear adapter。
- 不以 Elixir 参考实现为主工程。
- 不一开始实现 SSH worker、远程主机池或完整 app-server protocol。
- 不把产品做成多租户 SaaS。

### 3.2 WikiForge

需要吸收：

- 技术主线：Java 21、Spring Boot 3.x、Maven、Vue 3、Vite、TypeScript、Element Plus、Pinia、Axios、MySQL 8。
- 开发方式：Contract First，先冻结 API、DTO、状态枚举、DDL、服务边界，再实现。
- 文档方式：中文名 + EnglishName，按日期保留计划、日志、检查和归档。
- 后端分层：`interfaces -> application -> domain <- infrastructure`。
- 前端分层：`views/{domain}`、`api/{domain}`、`stores/{domain}`、`types/`、`utils/request.ts`。
- 响应格式：统一 `ApiResponse<T>`。
- 表归属：每张表必须明确属于哪个服务或领域。
- 高冲突文件串行修改：parent POM、migration、CI、Docker Compose、共享 DTO、状态枚举。
- 任务完成时必须输出完成内容、影响服务、修改文件、验证结果、归档文件、风险、下一步。

### 3.3 team-workload

需要吸收：

- 深色后台控制台风格。
- 左侧分组导航 + 顶部标题栏 + 内容滚动区。
- 密集表格承载业务状态和操作。
- 统计卡片突出当前工作状态。
- Element Plus 作为主要交互组件。
- 请求封装、统一错误提示和敏感信息脱敏日志。

## 4. 用户与使用场景

### 4.1 核心用户

- 个人开发者，长期同时维护多个独立项目。
- 熟悉 Java/Vue/Docker/MySQL，希望工具本地优先可控。
- 使用 Codex、Claude Code、OpenHands、PR-Agent 等 AI 工具辅助开发。
- 希望减少重复上下文说明、人工追踪测试和手工代码审核负担。

### 4.2 典型场景

1. 注册一个新项目：
   - 填写 GitHub 仓库、默认分支、本地路径、技术栈、测试命令、构建命令。
   - 读取或生成项目级 `WORKFLOW.md`。

2. 创建开发工作单：
   - 输入需求、目标项目、允许文件、禁止文件、验证命令。
   - 系统生成 Work Order，并进入 backlog / ready。

3. 派发给 agent：
   - Orchestrator 创建隔离 workspace。
   - 执行 before_run hook。
   - 调用 Codex / Claude / OpenHands adapter。
   - 记录运行事件、日志、token、产物和 diff。

4. 自动测试：
   - 执行工作单绑定的 test command。
   - 保存 stdout/stderr、退出码、耗时和失败原因。

5. 代码审核：
   - Review Agent 读取 diff、测试结果和需求。
   - 产出风险等级、发现项、建议和是否可进入人工审核。

6. 人工决策：
   - 用户查看工作单、diff、测试结果、Review 结论。
   - 选择 approve、reject、rerun、request changes、create PR、merge。

7. 交接与归档：
   - 系统保存 completion report。
   - 更新开发者日志或任务记录。
   - 关闭工作单或进入下一轮。

## 5. 产品目标

### 5.1 一期目标

- 建立独立控制台，不依附 WikiForge。
- 支持多个项目注册和状态展示。
- 支持 Work Order、Agent Run、Review Item、Command Template 的完整模型。
- 支持 MySQL 8 持久化。
- 支持本地 Docker Compose 启动。
- 支持基础 Dashboard。
- 支持 `WORKFLOW.md` 契约读取和展示。

### 5.2 中期目标

- 接入 GitHub Issues / PR。
- 支持从 issue 自动生成 Work Order。
- 支持 workspace 创建、复用、清理和路径安全检查。
- 支持 Codex / Claude / OpenHands session registry。
- 支持自动执行测试命令和构建命令。
- 支持 Review Agent 或 PR-Agent 接入。
- 支持运行日志和事件流。

### 5.3 长期目标

- 形成多项目个人研发操作系统。
- 支持多个 agent 并发执行，具备队列、重试、限流和人工确认。
- 支持远程工作机或其他电脑部署。
- 支持插件式 tracker adapter、agent adapter、review adapter。
- 支持成本统计、token 统计、模型供应商配置。
- 支持项目知识沉淀，与 WikiForge 联动写入开发日志、架构决策和项目文档。

## 6. 非目标

- 不做多人企业 SaaS。
- 不做通用低代码 workflow designer。
- 不在早期引入 Nacos、Kafka、Redis、XXL-JOB、Service Mesh。
- 不把业务项目源码复制进 SymphonyForgeOps 仓库。
- 不默认自动 merge 代码。
- 不保存 GitHub token、模型 API key、PAT 等敏感信息到 Git。
- 不让前端直接访问数据库或本地文件系统。

## 7. 功能需求

### 7.1 项目注册 Project Registry

字段：

- 项目 key、名称、描述。
- GitHub owner/repo、repo URL。
- 本地路径、默认分支、默认工作分支前缀。
- 技术栈摘要。
- 后端目录、前端目录、部署目录。
- `WORKFLOW.md` 路径。
- workspace root。
- 默认测试命令、构建命令、启动命令、Docker 命令。
- 状态：active、paused、archived、needs_config。

能力：

- 新增、编辑、归档项目。
- 检查本地路径是否存在。
- 检查是否为 Git 仓库。
- 读取最近 commit、当前分支、dirty 状态。
- 展示项目健康状态。

### 7.2 工作流契约 Workflow Contract

每个项目可以有一个或多个 `WORKFLOW.md`。

前置配置：

- tracker：github、linear、manual。
- polling：interval、enabled。
- workspace：root、cleanup policy。
- hooks：after_create、before_run、after_run、before_remove、timeout。
- agent：max_concurrent_agents、max_turns、retry_backoff。
- codex / claude / openhands：执行命令、超时、approval 策略。
- review：review command、required checks。

能力：

- 解析 YAML front matter。
- 提取 prompt body。
- 校验配置类型、必填项、路径和命令。
- 配置变更后可重新加载。
- 非法配置不影响最后一版可用配置。

### 7.3 Work Order

字段：

- work_order_key。
- project_id。
- 来源：manual、github_issue、github_pr、schedule、handoff。
- 标题、需求正文、验收标准。
- 目标分支、基线分支。
- 允许修改文件、禁止修改文件。
- 依赖契约。
- 测试命令、构建命令、审核命令。
- 状态：backlog、ready、claimed、running、waiting_review、changes_requested、blocked、completed、cancelled、failed。
- 风险级别、优先级、人工审核要求。

能力：

- 创建工作单。
- 从 GitHub issue 导入。
- 拆分子任务。
- 生成 Parallel Work Order。
- 绑定 agent run。
- 记录 handoff packet。

### 7.4 Orchestrator

职责：

- 定时读取 tracker 或本地 ready 队列。
- 根据优先级、阻塞关系和并发限制派发任务。
- 维护 running、claimed、retry、completed 状态。
- 处理 agent 正常退出、异常退出、超时、人工中断。
- 根据 active/terminal 状态做 reconcile。
- 保护单一调度权威，避免重复派发同一工作单。

状态：

- idle。
- polling。
- dispatching。
- running。
- degraded。
- paused。

核心规则：

- 契约无效时不派发新任务。
- workspace 创建失败进入 retry 或 blocked。
- 测试失败进入 waiting_review 或 changes_requested，不自动合并。
- 人工取消后停止运行并保存现场。

### 7.5 Workspace Manager

职责：

- 将 work_order_key 映射为安全的 workspace_key。
- 创建隔离目录。
- 克隆或同步目标仓库。
- 切换目标分支。
- 执行 hooks。
- 校验所有路径在 workspace root 内。
- 记录 workspace 生命周期。

安全要求：

- workspace root 必须是绝对路径。
- workspace_key 只允许 `[A-Za-z0-9._-]`，其他字符替换为 `_`。
- 禁止 agent 工作目录逃逸 workspace root。
- 清理 workspace 前必须确认路径归属。
- 默认不删除业务项目主目录。

### 7.6 Agent Runtime Adapter

第一批 adapter：

- Manual Adapter：只记录人工执行，用于早期闭环。
- Codex Adapter：调用 Codex CLI 或 app-server。
- Claude Code Adapter：记录/管理 Claude Code 会话。
- OpenHands Adapter：调用 OpenHands runtime。

统一模型：

- start_session。
- run_turn。
- stop_session。
- stream_event。
- collect_usage。
- collect_artifacts。

必须记录：

- session_id、thread_id、turn_id。
- pid 或外部 session 标识。
- start/end 时间。
- last event、last message。
- token usage、cost。
- stdout/stderr。
- changed files。

### 7.7 Command Template

命令类型：

- install。
- dev。
- test。
- build。
- package。
- docker_config。
- docker_build。
- docker_up。
- review。
- lint。

字段：

- command_key、command_type、command_text。
- working_directory。
- destructive flag。
- requires_approval。
- timeout_ms。
- environment profile。

规则：

- destructive 命令默认需要人工确认。
- 命令执行必须绑定 project 或 work order。
- 输出必须保存为 command_run。
- 失败时记录 exit_code、stderr 摘要和建议下一步。

### 7.8 Test Gate

能力：

- 运行项目级测试命令。
- 运行工作单级验证命令。
- 支持后端、前端、Docker、数据库迁移检查。
- 展示历史测试结果。
- 将测试结果作为 Review Gate 输入。

状态：

- pending。
- running。
- passed。
- failed。
- skipped。
- blocked_by_environment。

### 7.9 Review Gate

能力：

- 读取需求、diff、测试结果、agent 日志。
- 生成 Review Item。
- 标记风险：low、medium、high、critical。
- 输出 findings、open questions、建议修复、是否可人工审核。
- 接入 PR-Agent。
- 后续支持自研 Review Agent。

人工决策：

- approve。
- reject。
- request_changes。
- rerun。
- merge。
- archive_only。

### 7.10 GitHub Integration

能力：

- 读取 issue。
- 读取 PR。
- 创建分支。
- 创建 PR。
- 同步 PR 状态和 CI 状态。
- 评论运行结果。
- 关联 work order 与 issue/PR。

安全：

- GitHub token 只来自环境变量或本机安全配置。
- 前端不显示完整 token。
- 日志中 token 必须脱敏。

### 7.11 Observability

视图：

- 总览 Dashboard。
- Orchestrator 状态。
- Running Agent 列表。
- Retry Queue。
- Workspace 列表。
- Command Run 日志。
- Review Queue。
- Project Health。

事件类型：

- workflow_loaded。
- workflow_invalid。
- work_order_created。
- work_order_dispatched。
- workspace_created。
- hook_started。
- hook_failed。
- agent_session_started。
- agent_event。
- command_started。
- command_failed。
- review_created。
- human_decision_recorded。

### 7.12 项目知识沉淀

SymphonyForgeOps 不替代 WikiForge，但可以向 WikiForge 输出研发知识：

- 开发者日志。
- 架构决策。
- PRD 变更。
- 任务完成报告。
- Review 总结。
- 事故复盘。

输出方式：

- 先保存为 ForgeOps artifact。
- 后续通过 WikiForge API 或 MCP 写入 WikiForge。

## 8. 数据需求

核心表：

- managed_projects。
- workflow_contracts。
- work_orders。
- command_templates。
- isolated_workspaces。
- agent_runs。
- run_attempts。
- run_events。
- retry_queue。
- command_runs。
- review_items。
- human_decisions。
- github_links。
- artifacts。
- environment_profiles。
- model_providers。
- usage_records。

表归属：

- Control API：project、workflow、work_order、review、command、artifact 查询。
- Orchestrator：dispatch、run、retry、workspace 状态写入。
- Integration：GitHub、agent adapter、review adapter 外部同步。
- Future Gateway：后续统一前端入口。

## 9. API 需求

统一前缀：

```text
/api/v1/{domain}
```

统一响应：

```json
{
  "success": true,
  "data": {},
  "message": "ok",
  "code": null
}
```

核心 API：

- `GET /api/v1/dashboard`
- `GET /api/v1/projects`
- `POST /api/v1/projects`
- `GET /api/v1/projects/{id}/health`
- `GET /api/v1/workflows`
- `POST /api/v1/workflows/reload`
- `GET /api/v1/work-orders`
- `POST /api/v1/work-orders`
- `POST /api/v1/work-orders/{id}/dispatch`
- `POST /api/v1/work-orders/{id}/cancel`
- `GET /api/v1/agent-runs`
- `GET /api/v1/agent-runs/{id}/events`
- `GET /api/v1/reviews`
- `POST /api/v1/reviews/{id}/decision`
- `GET /api/v1/workspaces`
- `POST /api/v1/commands/run`

## 10. 前端需求

页面：

- Dashboard：总览、项目健康、运行、重试、审核。
- Projects：项目注册和命令配置。
- Work Orders：工作单列表、详情、创建、派发。
- Agent Runs：会话、事件、日志、token/cost。
- Workspaces：隔离工作区、路径、安全状态、清理。
- Reviews：审核队列、风险、结论、人工决策。
- Commands：命令模板和运行历史。
- Settings：GitHub、模型、路径、环境 profile。

交互原则：

- 深色后台控制台。
- 左侧分组导航。
- 表格密集展示，可筛选、可排序。
- 状态标签明确。
- 失败原因必须可见。
- 不做营销首页。
- 不在页面中放长篇说明，长说明进入 docs。

## 11. 部署需求

本地默认：

- 源码：`D:\Projects\SymphonyForgeOps`。
- workspace：`D:\ForgeOps\workspaces`。
- Maven 仓库：`E:\repository`。
- MySQL 8：Docker 或外部部署。
- 后端端口：8090。
- 前端端口：5173 或 Nginx 80。

迁移到其他电脑：

- 所有本机路径通过环境变量配置。
- 不在镜像内写死 Windows 路径。
- 数据库连接通过 `FORGEOPS_DATASOURCE_*`。
- workspace root 通过 `FORGEOPS_WORKSPACE_ROOT`。
- GitHub token 通过环境变量或本机密钥管理。

## 12. 安全需求

- PAT、API key、模型 key 不进入 Git。
- 命令日志必须脱敏。
- destructive command 需要人工确认。
- workspace 路径必须做 root containment。
- 删除 workspace 前必须二次校验真实路径。
- 默认不自动 merge。
- 默认不自动删除用户业务项目目录。
- 允许配置高信任本地模式，但必须在系统中显示风险。

## 13. 质量需求

- 后端单元测试覆盖 workflow parser、workspace path safety、orchestrator dispatch、retry。
- 集成测试覆盖 MySQL Flyway 空库迁移。
- 前端构建必须通过 `npm run build`。
- Docker Compose 至少通过 `config` 和基础启动检查。
- 每个开发切片必须有 Work Order 和 Completion Report。
- 关键契约变更必须同步文档。

## 14. 长期路线

### P0：控制台骨架

- 单 API 服务。
- Vue Dashboard。
- MySQL 8。
- 基础表结构。
- 文档规范。

### P1：持久化控制平面

- Project Registry CRUD。
- Work Order CRUD。
- Command Template CRUD。
- Review Item CRUD。
- Dashboard 从数据库读取。

### P2：Workflow Contract

- 解析 `WORKFLOW.md`。
- 校验 front matter。
- 支持 reload。
- 保存 workflow_contracts。

### P3：Workspace 与命令执行

- Workspace Manager。
- Git clone / branch。
- 命令执行器。
- Command Run 日志。

### P4：Orchestrator

- polling。
- dispatch。
- retry。
- reconcile。
- event stream。

### P5：Agent Adapter

- Manual。
- Codex。
- Claude Code。
- OpenHands。

### P6：GitHub 与 Review

- GitHub Issues / PR。
- PR-Agent。
- Review Gate。
- Human Decision。

### P7：多机与运行层

- 远程 worker。
- worker health。
- host capacity。
- usage/cost。
- WikiForge 知识沉淀联动。

