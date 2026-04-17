# Frontend Development Guidelines

> Repo-grounded frontend standards for the Angular application in `frontend-monorepo/`.

---

## Scope

These guidelines describe the frontend that currently lives in `frontend-monorepo/projects/multi-module-app/`:

- Angular 21
- NgModule-rooted application shell
- Angular Material + AG Grid UI stack
- auth/bootstrap handled through services, guards, interceptors, and `APP_INITIALIZER`
- route/query-param driven workspace behavior

Document the app as it exists today. Do not rewrite the spec around React, NgRx, or a fully standalone-component architecture that this repo does not currently use consistently.

---

## Read This Before Frontend Work

1. Read this index first.
2. Read the detailed guides for the files you will touch:
   - [Directory Structure](./directory-structure.md)
   - [Component Guidelines](./component-guidelines.md)
   - [Hook Guidelines](./hook-guidelines.md)
   - [State Management](./state-management.md)
   - [Type Safety](./type-safety.md)
   - [Quality Guidelines](./quality-guidelines.md)
3. If the change touches auth, `/api` payloads, or proxy behavior, read the backend spec as well.
4. For local frontend work:
   - `cd frontend-monorepo && npm start`
   - the dev server proxies `/api`, `/oauth2`, `/oauth`, and `/login/oauth2` to `http://localhost:8080` via `projects/multi-module-app/proxy.conf.json`

---

## Guidelines Index

| Guide | Description | Status |
|-------|-------------|--------|
| [Directory Structure](./directory-structure.md) | `core/`, `features/`, `shared/` ownership and file layout | Filled |
| [Component Guidelines](./component-guidelines.md) | Angular component patterns, templates, styling, a11y | Filled |
| [Hook Guidelines](./hook-guidelines.md) | Angular equivalents for shared stateful logic and fetching | Filled |
| [State Management](./state-management.md) | Service state, route state, local state, caching | Filled |
| [Quality Guidelines](./quality-guidelines.md) | Lint/test requirements, review checklist, forbidden shortcuts | Filled |
| [Type Safety](./type-safety.md) | Strict TS usage, dynamic boundaries, contract normalization | Filled |

---

## Non-Negotiable Conventions

- `core/` owns app shell, config, guards, interceptors, services, and cross-feature models.
- `features/` owns routed or product-specific UI modules.
- Shared state is currently service-driven; there is no NgRx/Redux store.
- React hook terminology does not apply here. Shared logic lives in Angular services, guards, interceptors, and bootstrap functions.
- Angular template control flow (`@if`, `@for`, `@switch`) is already in active use and should not be treated as experimental inside this repo.

---

## Reference Files

- `frontend-monorepo/projects/multi-module-app/src/app/app.module.ts`
- `frontend-monorepo/projects/multi-module-app/src/app/core/home/home.component.ts`
- `frontend-monorepo/projects/multi-module-app/src/app/core/workspace/workspace.component.ts`
- `frontend-monorepo/projects/multi-module-app/src/app/core/services/auth.service.ts`
- `frontend-monorepo/projects/multi-module-app/src/app/features/data-query/data-query.component.ts`

---

**Language**: All documentation in this directory should stay in **English**.
