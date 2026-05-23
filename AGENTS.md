# SymphonyForgeOps Agent Rules

本文件约束参与 SymphonyForgeOps 开发的 AI Agent 和人工开发者。项目目标是用 Java + Vue 技术栈实现一个受 openai/symphony 启发、按 WikiForge 工程模式治理的个人 AI 研发控制台。

## Start Every Task

开始任何代码、文档、架构、Docker、CI、GitHub、Agent 或 Orchestrator 工作前，按顺序读取：

1. `WORKFLOW.md`。
2. `docs/ai-skills/symphonyforgeops-development/SKILL.md`。
3. `docs/current/2026-05-23-项目整体计划-SymphonyForgeOps-project-roadmap.md`。
4. `docs/current/架构决策-DECISIONS.md`。
5. 与任务相关的 `docs/current/` 主线文档。
6. 如存在 `docs/archive/YYYY-MM-DD/`，读取当天最新归档索引。

优先级：

1. 用户当前明确指令。
2. 本文件和 `WORKFLOW.md`。
3. `docs/current/` 当前主线文档。
4. `docs/archive/` 历史快照。

如最新指令和主线文档冲突，先按用户当前指令执行，并在开发者日志记录调整原因。

## Project Boundary

- SymphonyForgeOps 是独立研发控制台，不属于 WikiForge 业务代码。
- WikiForge、team-workload 和未来项目都是被管理仓库，不把它们的源码复制进本仓库。
- 本项目保存项目元数据、工作单、工作流契约、workspace、agent run、命令日志、测试结果、review、人类决策和交接账本。
- 运行日志、workspace、数据库数据、`.env`、token、模型 key 和用户业务项目源码不得提交。

## Architecture Direction

采用 WikiForge 的少服务微服务治理方式，但第一阶段保持可落地：

- MVP0/MVP1：`forgeops-api` + `frontend` + MySQL。
- MVP2/MVP3：补强 Workflow Contract、Command、Workspace、GitHub、Review。
- MVP4：拆出 `forgeops-worker-service` 处理 workspace/命令/agent 执行。
- MVP5：拆出 `forgeops-orchestrator-service` 和可选 `forgeops-orchestration-ui`。
- V1 后再评估 gateway、remote worker、model/usage service。

不照搬 openai/symphony 的 Elixir 参考实现；吸收其服务规范、状态机、workspace 安全、retry/reconcile、`WORKFLOW.md` 契约和 observability 思想。

## Engineering Defaults

- Contract First：API、DTO、状态枚举、错误码、DDL、服务边界和验证命令先冻结，再实现。
- TDD：新增行为先写失败测试，再写最小实现。
- DDD-style backend：`interfaces -> application -> domain <- infrastructure`。
- Vue frontend：`views/{domain}`、`api/{domain}`、`stores/{domain}`、`types/`、`utils/request.ts`。
- MySQL 是控制平面、索引库和运行账本，不保存不必要的大型正文或外部源码。
- Flyway migration 按阶段新增，禁止一次性创建所有长期规划表。
- 所有 destructive command 默认需要人工确认。
- 默认不自动 merge，不自动删除 workspace，不自动删除用户项目目录。

## Documentation Rules

文档分层：

```text
docs/current/        # 当前主线文档
docs/process/        # 调研、评审、自检、阶段材料
docs/archive/        # 日期归档快照
docs/superpowers/    # 可执行 Work Order / implementation plans
docs/ai-skills/      # 项目内 AI 开发 Skill
```

除工具约定文件外，文档采用中文名 + EnglishName：

```text
中文名-EnglishName.md
YYYY-MM-DD-中文名-EnglishName.md
YYYY-MM-DD-中文名-EnglishName-v0.1.md
```

每个阶段完成后必须同步：

- 项目整体计划当前指针。
- 开发者日志。
- 相关 Work Order。
- 归档快照或归档索引。

## Branch And Release Rules

- `main` 是稳定主干；阶段完成后必须合入并推送。
- 日常任务使用短生命周期分支：`codex/{task-key}`。
- 历史阶段分支可保留用于追溯，不自动删除远程分支。
- 用户已授权：验证通过后可自动提交、推送、创建标签和 GitHub Release；如发布策略尚未冻结，先记录为 preview。
- 不使用 force push，除非用户在当前 turn 明确要求。

## Parallel Agent Team Rules

并行前必须先产出 Parallel Work Order，并创建：

```text
agentteam/{YYYY-MM-DD}-{task-id}-{team-name}/
  README.md
  任务计划-team-plan.md
  agents/
    {agent-name}/
      README.md
      PROMPT.md
      SKILL.md
      WORKSPACE.md
      STATUS.md
```

规则：

- 主 Agent 负责契约、高冲突文件、最终集成、验证、正式文档、归档和发布。
- 子 Agent 只处理独立模块、只读审查或低冲突草案。
- 子 Agent 不直接修改 Roadmap、开发者日志、归档索引、发布说明和主 Work Order，除非任务明确授权。
- 子 Agent 通过自己的 `STATUS.md` 汇报改动、验证、风险和需要主 Agent 集成的事项。

## High-Conflict Files

同一时间只允许一个 Agent 修改：

- `backend/pom.xml`
- `backend/*/pom.xml`
- Flyway migration
- `.github/workflows/*.yml`
- `deploy/docker-compose*.yml`
- `.env.example`
- 共享 response、error、status enum
- `frontend/src/utils/request.ts`
- `WORKFLOW.md`
- `AGENTS.md`
- `docs/current/2026-05-23-项目整体计划-SymphonyForgeOps-project-roadmap.md`
- `docs/current/开发者日志-SymphonyForgeOps-developer-log.md`

## Verification Gates

T0 文档和 Git 卫生：

```powershell
git status --short
git diff --check
rg "github_pat|ghp_|gho_|TOKEN|API_KEY|SECRET|PASSWORD|Bearer " -n .
```

T1 后端单服务：

```powershell
cd backend
mvn -B -pl forgeops-api -am test
```

T2 前端构建：

```powershell
cd frontend
npm install
npm run build
```

T3 Docker 配置：

```powershell
docker compose -f deploy/docker-compose.dev.yml config
```

T4 阶段验收：

- 后端测试通过。
- 前端构建通过。
- Docker Compose config 通过。
- MySQL 空库 Flyway migration 通过。
- Dashboard 或相关页面人工/浏览器检查通过。
- 开发者日志和项目整体计划已更新。

## Completion Report

每个任务完成后输出：

```text
任务ID：
状态：DONE / DONE_WITH_CONCERNS / BLOCKED
完成内容：
实际修改文件：
契约变更：
验证命令和结果：
未验证原因：
风险：
下一步：
```
