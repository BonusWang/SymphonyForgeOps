---
name: symphonyforgeops-development
description: SymphonyForgeOps project development rules for AI agents and human developers. Use when designing, coding, reviewing, documenting, or operating SymphonyForgeOps architecture, backend services, frontend modules, Docker/CI, GitHub integration, workflow contracts, workspaces, command execution, agent adapters, review gates, or multi-agent collaboration.
---

# SymphonyForgeOps Development Skill

This skill keeps SymphonyForgeOps aligned with the user's requested mode: learn from `openai/symphony`, follow WikiForge's project discipline, and implement with Java + Spring Boot + Vue rather than copying another stack.

## Start Every Task

1. Read `AGENTS.md`.
2. Read `WORKFLOW.md`.
3. Read `docs/current/2026-05-23-项目整体计划-SymphonyForgeOps-project-roadmap.md`.
4. Read `docs/current/架构决策-DECISIONS.md`.
5. Read the relevant reference below before changing code or docs.
6. Confirm the current stage, task card, allowed files, forbidden files, and verification gate.

## Reference Map

- Architecture or service split: `references/architecture-style.md`
- Backend implementation: `references/backend-ddd-standard.md`
- Frontend implementation: `references/frontend-standard.md`
- Workflow/orchestrator/workspace work: `references/symphony-runtime-standard.md`
- Docker, CI, release, environment: `references/ci-docker-standard.md`
- Parallel or multi-agent work: `references/multi-agent-collaboration.md`

## Core Product Boundary

SymphonyForgeOps is the development operating layer for independent repositories. It manages:

- managed projects
- repository workflow contracts
- work orders
- isolated workspaces
- command templates and command runs
- agent runs and events
- test gates
- review gates
- GitHub links
- human decisions
- handoff artifacts

It must not become WikiForge itself. WikiForge can be the first managed project and a reference implementation style.

## OpenAI Symphony Adaptation Rule

Absorb these concepts:

- repository-owned `WORKFLOW.md`
- workflow loader with YAML front matter and prompt body
- typed config defaults and validation
- dynamic reload with last-known-good behavior
- single-authority orchestrator state
- active/terminal state reconciliation
- deterministic workspace keys and root containment
- lifecycle hooks
- agent runner session metadata
- retry queue and continuation retries
- structured observability

Do not copy these choices blindly:

- Elixir implementation
- Linear-only tracker model
- no-database scheduler assumptions
- high-trust defaults without visible risk
- remote SSH worker before local workspace execution works

## WikiForge Adaptation Rule

Follow WikiForge's discipline:

- docs/current as current truth
- docs/archive as snapshots
- docs/superpowers/plans as executable work orders
- developer log after every stage
- Contract First before implementation
- TDD for behavior changes
- small-service microservice evolution
- DDD-style backend boundaries
- Element Plus operational console UI
- Docker/CI from early phases
- explicit high-conflict files and parallel-agent boundaries

## Stage Discipline

Do not implement the full non-MVP product in one change. The non-MVP product is the roadmap; implementation happens in verifiable slices.

Current ordered stages:

1. S0 Governance and environment alignment.
2. S1 Persistent control plane.
3. S2 Workflow contract parser and reload.
4. S3 Command and workspace safety.
5. S4 Orchestrator skeleton.
6. S5 Agent runtime adapters.
7. S6 GitHub and review gate.
8. S7 Observability, usage, and WikiForge knowledge handoff.
9. V1 Multi-machine worker and deeper automation.

## Before Completion

Always report:

- files changed
- service boundary affected
- verification run
- known blockers
- docs and archive updates
- whether WIP was stashed, restored, committed, or pushed
