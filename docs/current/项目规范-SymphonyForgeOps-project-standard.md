# SymphonyForgeOps 项目规范 Project Standard

## 1. 开发入口

任何任务先读：

1. `AGENTS.md`
2. `WORKFLOW.md`
3. `docs/ai-skills/symphonyforgeops-development/SKILL.md`
4. `docs/current/2026-05-23-项目整体计划-SymphonyForgeOps-project-roadmap.md`
5. 当前任务相关主线文档

## 2. 文档结构

```text
docs/current/        # 当前主线事实
docs/process/        # 调研、评审、自检
docs/archive/        # 日期快照
docs/superpowers/    # 可执行计划
docs/ai-skills/      # 项目内 Skill
```

## 3. 阶段执行

每个阶段必须有：

- 阶段目标。
- 允许修改文件。
- 禁止修改文件。
- API / DTO / DDL 契约。
- 验证门禁。
- Handoff Packet。

## 4. 代码分层

后端：

```text
interfaces -> application -> domain <- infrastructure
```

前端：

```text
api/{domain}
stores/{domain}
types
views/{domain}
utils/request.ts
```

## 5. 状态命名

Work Order 状态：

```text
backlog
ready
claimed
running
waiting_review
changes_requested
blocked
completed
cancelled
failed
```

Run 状态：

```text
pending
running
waiting_review
retry_scheduled
completed
failed
cancelled
blocked
```

Review 状态：

```text
pending
approved
rejected
changes_requested
archived
```

Agent 运行规则：

```text
Manual -> 执行 work order testCommand
Codex -> 调用本机 codex exec，并记录 stdout/stderr/exit code/run events
Claude/OpenHands -> v1.0 保存 adapter config，真实执行进入后续切片
```

测试数据库规则：

```text
local/dev runtime -> forgeops
backend integration tests -> forgeops_test
CI backend tests -> forgeops_test
```

Java 验证规则：

```text
project runtime baseline -> Java 17
host Java 8 -> do not use for backend verification
host Java 25 -> do not use as project baseline
fallback verification -> maven:3.9.9-eclipse-temurin-17 container
```

## 6. 安全规则

- Token/API key/PAT 不进 Git。
- Command log 脱敏。
- destructive command 必须 `requires_approval`。
- workspace 路径必须 root containment。
- Codex adapter 默认从受管项目 root 启动，timeout 默认 300 秒。
- 测试清理只能发生在 `forgeops_test`，不能清共享开发库 `forgeops`。
- 删除 workspace 前必须校验真实路径。
- 不自动 merge。
- 不删除受管项目主目录。

## 7. Git 规则

- `main` 是稳定主干。
- 任务分支用 `codex/{task-key}`。
- 阶段完成后合入 `main`。
- 不强推，除非用户当前明确要求。
- 提交前跑对应验证门禁。

## 8. Handoff Packet

```text
任务ID：
状态：
完成内容：
实际修改文件：
契约变更：
验证命令和结果：
未验证原因：
风险：
下一步：
```
