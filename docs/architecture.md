# SymphonyForgeOps Architecture

## Goal

SymphonyForgeOps is a personal research and development mission-control system. It coordinates AI-assisted implementation work across multiple independent repositories.

## Core Ideas

- Mission Control provides the product shape: dashboard, task queue, logs, cost/status visibility.
- Symphony Spec provides the work model: isolated implementation runs, durable work records, and project-level workflow files.
- OctoAlly provides the session-console inspiration: long-running agent sessions, diffs, commands, and local operator control.
- PR-Agent is a candidate review adapter rather than the main system.

## MVP Service Shape

```text
frontend
  -> forgeops-api
       -> projects
       -> work_orders
       -> agent_runs
       -> review_items
       -> command_templates
       -> local database
```

MVP uses one backend service. Split services only after the control model is stable.

## Managed Project Model

A managed project is an external Git repository plus local execution metadata.

```text
Project
  id
  name
  repo_url
  local_path
  default_branch
  stack
  status
```

## Work Model

```text
WorkOrder
  -> ImplementationRun
  -> TestRun
  -> ReviewItem
  -> HumanDecision
```

Every run should preserve:

- input requirement
- plan
- command log
- changed files
- test result
- review conclusion
- final decision

## Recommended Roadmap

1. MVP0: project skeleton, dashboard, project registry, work order model.
2. MVP1: GitHub repository sync and local command templates.
3. MVP2: Codex/Claude/OpenHands session registry.
4. MVP3: automated test and review pipeline.
5. MVP4: GitHub PR integration and PR-Agent adapter.
6. V1: isolated workspace execution inspired by OpenAI Symphony.

