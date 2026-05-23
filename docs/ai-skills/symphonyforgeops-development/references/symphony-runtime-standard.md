# Symphony Runtime Standard

## Concepts To Preserve

This project adapts the openai/symphony service specification into a Java + Vue + MySQL implementation.

## WORKFLOW.md Contract

`WORKFLOW.md` is repository-owned and version-controlled. It contains:

- optional YAML front matter
- Markdown prompt body

Required core config domains:

- `tracker`
- `polling`
- `workspace`
- `hooks`
- `agent`
- `codex`
- future extensions: `github`, `review`, `commands`, `claude`, `openhands`

Parser behavior:

- no front matter means empty config and full body as prompt.
- non-map YAML is a typed error.
- unknown top-level keys are ignored for forward compatibility.
- prompt rendering must fail on unknown variables.

## Workspace Safety

- Workspace root must resolve to an absolute path.
- Workspace key is derived from work order key or issue key by replacing characters outside `[A-Za-z0-9._-]` with `_`.
- All command cwd and workspace paths must pass root containment checks before use.
- Never delete a path unless its real path is confirmed inside workspace root.

## Orchestrator Rules

- One orchestrator is the authority for polling, claiming, dispatch, retry, and reconcile.
- Active work may be stopped when tracker state becomes ineligible.
- Terminal states allow cleanup only after path safety checks.
- Normal agent exit is not automatically done; it may schedule continuation or move to waiting review.
- Retry queue records attempt, due time, identifier, and error.
- Invalid workflow reload keeps the last known good config and surfaces the error.

## Observability

Every run should preserve:

- work order
- workspace
- branch
- agent adapter
- session id / thread id / turn id when available
- command stdout/stderr summaries
- changed files
- validation evidence
- review outcome
- human decision

Structured logs must include work order key and run key when available.
