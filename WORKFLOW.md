---
tracker:
  kind: github
  repository: BonusWang/SymphonyForgeOps
  active_states:
    - backlog
    - ready
    - in_progress
  terminal_states:
    - merged
    - cancelled
    - done
polling:
  interval_ms: 30000
workspace:
  root: D:/ForgeOps/workspaces
hooks:
  timeout_ms: 60000
  before_run: |
    git status --short
agent:
  max_concurrent_agents: 2
  max_turns: 20
  max_retry_backoff_ms: 300000
codex:
  command: codex app-server
  turn_timeout_ms: 3600000
  stall_timeout_ms: 300000
---

# SymphonyForgeOps Workflow

You are working on SymphonyForgeOps itself or running an isolated implementation attempt for a managed project.

Follow this order:

1. Read `AGENTS.md`.
2. Read `docs/ai-skills/symphonyforgeops-development/SKILL.md`.
3. Read the current roadmap and architecture decisions under `docs/current/`.
4. Confirm the work order scope, allowed files, forbidden files, contracts, and verification gate.
5. Preserve user changes and never run destructive commands without explicit approval.
6. For managed project work, work only inside the assigned workspace.
7. Run the declared verification command before handoff whenever the local environment supports it.
8. Update roadmap, developer log, and archive/index documents when a stage changes.
9. Produce a completion report with changed files, verification results, risks, and review notes.

Handoff state:

- If implementation and tests pass, move the work order to `waiting_review`.
- If blocked by environment or missing context, move the work order to `blocked`.
- If code changes are made, require human review before merge.
- If the task is documentation/governance only, use T0 verification.
- If the task changes backend behavior, use TDD and T1 verification.
- If the task changes frontend behavior, use T2 verification.
