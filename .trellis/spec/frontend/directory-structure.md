# Directory Structure

> How frontend code is organized in this project.

---

## Overview

The Angular app is organized by responsibility:

- `core/` for application shell and cross-cutting concerns
- `features/` for routed/product-specific screens and modules
- `shared/components/` for reusable UI building blocks

This repo does not use a flat `components/`, `hooks/`, `utils/` layout. New code should have a clear ownership boundary.

---

## Directory Layout

```text
frontend-monorepo/projects/multi-module-app/src/app/
├── app.module.ts
├── app-routing.module.ts
├── core/
│   ├── config/
│   ├── guards/
│   ├── home/
│   ├── interceptors/
│   ├── models/
│   ├── services/
│   └── workspace/
├── features/
│   ├── auth/
│   ├── data-explorer/
│   ├── data-query/
│   └── external-iframe/
└── shared/
    └── components/
        ├── dynamic-table/
        └── query-builder/
```

Related runtime/config files:

- `frontend-monorepo/projects/multi-module-app/public/` for static assets
- `frontend-monorepo/projects/multi-module-app/proxy.conf.json` for dev-server backend proxying
- `frontend-monorepo/angular.json` for Angular CLI defaults and project build/test configuration

---

## Module Organization

- Put app-wide services, guards, interceptors, config objects, and reusable models in `core/`.
  Examples:
  `core/services/auth.service.ts`,
  `core/services/config.service.ts`,
  `core/guards/auth.guard.ts`,
  `core/config/data-query.config.ts`
- Put route-level or product-specific experiences in `features/`.
  Examples:
  `features/data-query/data-query.component.ts`,
  `features/external-iframe/external-iframe.component.ts`,
  `features/auth/login/login.component.ts`
- Put reusable visual primitives in `shared/components/`.
  Examples:
  `shared/components/query-builder/query-builder.component.ts`,
  `shared/components/dynamic-table/dynamic-table.component.ts`
- Keep workspace shell logic in `core/workspace/` because it coordinates multiple feature components and route state.
  Examples:
  `core/workspace/workspace.component.ts`,
  `core/workspace/tab.service.ts`

---

## Naming Conventions

- Component folders use kebab-case names and colocate:
  `*.component.ts`, `*.component.html`, `*.component.scss`, `*.component.spec.ts`
- Services end with `*.service.ts`
- Guards end with `*.guard.ts`
- Interceptors end with `*.interceptor.ts`
- Config modules end with `*.config.ts`
- Shared model files currently use `*.models.ts`
- Angular selectors use the `app-` prefix and kebab-case, matching ESLint rules

Project defaults from `angular.json` still generate components/directives/pipes as `standalone: false`, so new structure should assume NgModule integration unless the surrounding feature already uses a different pattern intentionally.

---

## Good Examples

- `frontend-monorepo/projects/multi-module-app/src/app/core/home/`
  Clear component folder with colocated template, stylesheet, and tests.
- `frontend-monorepo/projects/multi-module-app/src/app/core/workspace/`
  Good example of pairing a shell component with a focused state service.
- `frontend-monorepo/projects/multi-module-app/src/app/features/data-query/`
  Shows a feature module that owns its routed UI while importing shared components.

---

## Anti-Patterns And Common Mistakes

- Do not place feature-specific code in `shared/components/` just because it might be reused later.
- Do not duplicate API/config models inside feature folders when `core/models/` or `core/config/` already owns them.
- Do not introduce a generic `hooks/` directory. Angular shared logic belongs in services, guards, interceptors, or local helpers.
- Do not let `core/` become a dumping ground for one-off feature code. If the code only serves one routed feature, keep it under `features/`.
