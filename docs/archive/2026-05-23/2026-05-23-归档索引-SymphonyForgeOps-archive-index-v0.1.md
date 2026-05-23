# SymphonyForgeOps 归档索引 Archive Index v0.1

## Version Index

- 最新版本：v0.1
- 日期：2026-05-23
- 范围：项目启动、GitHub main 合并、openai/symphony + WikiForge 模式重调。

## 当前事实来源

优先阅读：

```text
AGENTS.md
WORKFLOW.md
docs/ai-skills/symphonyforgeops-development/SKILL.md
docs/current/2026-05-23-项目整体计划-SymphonyForgeOps-project-roadmap.md
docs/current/架构决策-DECISIONS.md
docs/current/技术架构-SymphonyForgeOps-technical-architecture.md
docs/current/项目规范-SymphonyForgeOps-project-standard.md
docs/current/开发者日志-SymphonyForgeOps-developer-log.md
```

## 本轮归档结论

- GitHub `main` 已通过普通 merge 发布本地工程，未 force push。
- 本机 MySQL 可复用 WikiForge 容器 3306，独立 schema 为 `forgeops`。
- Java 21 仍是正式验收前置；当前 shell 默认 Java 8。
- Trae bundled JDK 25 可临时执行 Java 21 target 编译，但不是最终环境标准。
- 用户级 Maven settings 指向不可达私有 mirror；验证时使用临时 settings 或修复本机配置。
- P1 持久化半成品已暂存为 `codex-p1-wip-before-wikiforge-replan`，后续需按新规范恢复/拆分/重做。

## 后续执行指针

当前指针：

```text
S0 Governance / Environment
```

下一任务：

```text
S0-DOC-001: 完成 README、.env.example、archive index，提交推送治理基线。
S1-PERSIST-001: 恢复 P1 WIP，按 DDD + TDD 拆分 Project Registry 和 Work Order 持久化。
```
