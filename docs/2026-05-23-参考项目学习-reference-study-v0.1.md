# 参考项目学习 Reference Study v0.1

## 学习对象

- `openai/symphony`: 作为设计规范和调度思想来源。
- `BonusWang/team-workload`: 作为个人后台系统的前端设计和交互参考。
- `BonusWang/WikiForge`: 作为项目规范、工程边界和 AI 协作规则参考。

## openai/symphony 提炼

适合吸收的部分：

- `WORKFLOW.md` 是仓库内的工作契约，包含 front matter 配置和 prompt body。
- Orchestrator 是单一调度权威，负责 polling、dispatch、retry、reconcile。
- 每个 issue/work order 都有隔离 workspace，agent 只在该 workspace 内执行。
- Hooks 分为 `after_create`、`before_run`、`after_run`、`before_remove`。
- 状态面需要展示 running、retry、token/cost、last event、rate limit 等可观测信息。
- 正常退出不代表工作完成，可能进入 continuation retry 或 human review。

MVP 不直接实现的部分：

- 不绑定 Linear，先抽象为 tracker adapter。
- 不直接实现 Codex app-server protocol。
- 不实现 SSH worker 和远程主机池。
- 不把调度器做成通用 workflow engine。

## team-workload 提炼

适合吸收的部分：

- 深色后台控制台，而不是营销页。
- 左侧分组导航、顶部标题、右侧内容滚动区。
- 统计卡片突出业务状态，表格承载密集操作数据。
- Element Plus 作为主要交互组件。
- 请求统一封装，日志中隐藏敏感 token。
- 页面强调状态、筛选、审批和管理动作。

调整到 ForgeOps：

- 导航分为 `工作台` 和 `治理`。
- Dashboard 展示项目、工作单、运行、重试、审核。
- 表格使用紧凑状态标签和风险标识。
- 不做登录模块，个人本地 MVP 先保持单用户。

## WikiForge 提炼

适合吸收的部分：

- 技术栈：Java 21 / Spring Boot 3.x / Maven / Vue 3 / Vite / TypeScript / Element Plus / MySQL 8。
- 后端分层：`interfaces -> application -> domain <- infrastructure`。
- 对外 API 使用 `/api/v1/{domain}`。
- 响应统一使用 `ApiResponse<T>`。
- Contract First，数据库 migration、DTO、状态枚举和命令模板先冻结。
- 文档和 Work Order 是 AI 协作的一等产物。
- CI/Docker 不写死本机路径和敏感配置。

调整到 ForgeOps：

- 新增根目录 `WORKFLOW.md`，作为 ForgeOps 自身可执行契约。
- 数据库新增 workflow、workspace、run、retry、event 预留表。
- Dashboard API 改为统一 `ApiResponse<T>`。
- 前端目录继续保持 `frontend/`，但交互风格向 `team-workload` 靠拢。

