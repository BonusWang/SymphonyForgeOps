# Multi-Agent Collaboration

## Lead Agent Responsibility

The lead agent owns:

- contract freeze
- shared migration numbering
- high-conflict files
- final integration
- verification
- official docs
- archive snapshots
- commit, push, tag, and release

## Agent Team Folder

Before parallel work, create:

```text
agentteam/{YYYY-MM-DD}-{task-id}-{team-name}/
  README.md
  任务计划-team-plan.md
  agents/
    backend-agent/
      README.md
      PROMPT.md
      SKILL.md
      WORKSPACE.md
      STATUS.md
    frontend-agent/
      README.md
      PROMPT.md
      SKILL.md
      WORKSPACE.md
      STATUS.md
    test-agent/
      README.md
      PROMPT.md
      SKILL.md
      WORKSPACE.md
      STATUS.md
```

## Parallel Preconditions

- Shared API path, DTO, enum, error code, and migration number are frozen.
- Each agent has disjoint file ownership.
- Each agent knows allowed files, forbidden files, and verification command.
- Main branch or baseline commit is pushed so worktrees/subagents can reproduce context.

## Status File Contract

Each `STATUS.md` must include:

```text
任务范围：
状态：
分支或工作区：
修改文件：
验证命令：
验证结果：
风险：
需要主 Agent 处理：
```

Subagents do not modify roadmap, developer log, release notes, or archive index unless the work order explicitly makes them the docs owner.
