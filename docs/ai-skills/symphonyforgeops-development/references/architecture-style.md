# Architecture Style

## Direction

SymphonyForgeOps follows a small-service evolution model:

```text
frontend
  -> forgeops-api
       -> MySQL
       -> local filesystem metadata
       -> GitHub / agent adapters later

future:
  frontend
  -> forgeops-api
  -> forgeops-worker-service
  -> forgeops-orchestrator-service
  -> MySQL
```

## Service Split Rules

- `forgeops-api` owns user-facing REST APIs, dashboard queries, project/work-order/workflow/review/command metadata, and human decisions.
- `forgeops-worker-service` will own workspace creation, command execution, process supervision, and filesystem-heavy work after local control-plane APIs are stable.
- `forgeops-orchestrator-service` will own polling, dispatch, reconcile, retry, and agent lifecycle coordination after Workflow + Workspace + Command are stable.
- UI calls `forgeops-api` first. It must not call worker internals directly.

## Avoid Early Infrastructure

Do not introduce Nacos, Kafka, Redis, XXL-JOB, service mesh, or a full workflow engine in MVP stages. Use Spring Scheduler, MySQL, and explicit state tables until there is a concrete scaling need.

## Data Ownership

Every table must declare owner:

- Control API: projects, workflows, work orders, commands, reviews, decisions, artifacts.
- Worker: workspaces, command runs, filesystem operation logs.
- Orchestrator: claims, retries, run attempts, run events.
- Integration: GitHub links, PR/check sync, adapter metadata.

Cross-service reads must go through APIs or documented DTOs once services are split.
