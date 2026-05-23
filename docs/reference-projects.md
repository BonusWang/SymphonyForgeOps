# Reference Projects

## Primary References

| Project | Role | Recommendation |
| --- | --- | --- |
| openai/symphony | Work model and spec inspiration | Use as design reference, not as the main fork target |
| builderz-labs/mission-control | Self-hosted mission-control base | Best product-shape reference for dashboard, tasks, logs, and agents |
| ai-genius-automations/octoally | Agent session console | Use as reference for Codex/Claude session management |
| Compozy | PRD to task execution pipeline | Use as documentation workflow reference |
| PR-Agent | Pull request review adapter | Integrate later as a review tool |

## Local Choice

SymphonyForgeOps should not be a direct fork in MVP. A custom lightweight implementation fits personal usage better:

- it can match the existing Java/Vue stack;
- it can keep WikiForge clean;
- it can evolve around personal command templates and local workspace rules;
- it can later borrow execution isolation from Symphony without inheriting a heavy runtime.

