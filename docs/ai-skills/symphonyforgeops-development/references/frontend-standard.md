# Frontend Standard

## Stack

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Axios

## Structure

```text
frontend/src/
  api/{domain}/
  stores/{domain}.ts
  types/
  views/{domain}/
  utils/request.ts
  styles/
```

Small early-stage files may stay flat, but new feature domains should use this shape.

## UX Direction

SymphonyForgeOps is an operations console, not a landing page.

- Dense but readable tables.
- Left navigation + top title/action bar.
- Status tags for work order, run, review, command, and workspace state.
- Failures and blockers visible near the item they affect.
- No decorative marketing hero.
- No long instructional copy in the app; put long explanations in docs.

## Safety

- Frontend never receives full tokens or secrets.
- Frontend never reads local filesystem directly.
- Destructive operations must show explicit approval state returned by backend.
- Command execution UI must distinguish dry-run, requires approval, running, failed, passed.

## Verification

- Run `npm run build` for any frontend behavior or TypeScript change.
- For major UI pages, inspect in browser at desktop and narrow widths.
