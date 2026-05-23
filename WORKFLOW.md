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

You are running an isolated implementation attempt for a managed project.

Follow this order:

1. Read the managed project's `AGENTS.md` or equivalent project rules.
2. Confirm the work order scope, allowed files, forbidden files, and verification commands.
3. Work only inside the assigned workspace.
4. Preserve user changes and never run destructive commands without explicit approval.
5. Run the declared verification command before handoff whenever the local environment supports it.
6. Produce a completion report with changed files, verification results, risks, and review notes.

Handoff state:

- If implementation and tests pass, move the work order to `waiting_review`.
- If blocked by environment or missing context, move the work order to `blocked`.
- If code changes are made, require human review before merge.

