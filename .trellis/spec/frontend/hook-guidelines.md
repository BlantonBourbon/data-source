# Hook Guidelines

> How shared stateful logic is handled in this Angular project.

---

## Overview

This repo does not use React hooks. There are no `use*` modules and no React-style hook conventions to follow.

Angular equivalents used here are:

- services for shared state and backend integration
- guards for route-entry decisions
- interceptors for cross-cutting HTTP behavior
- `APP_INITIALIZER` for startup/bootstrap flows
- local Angular signals in a few components where component-local reactive state is useful

If you are thinking "custom hook", map that need to one of the Angular mechanisms above.

---

## Angular Equivalents To Hooks

- Service-backed shared state:
  `frontend-monorepo/projects/multi-module-app/src/app/core/services/auth.service.ts`,
  `frontend-monorepo/projects/multi-module-app/src/app/core/services/config.service.ts`,
  `frontend-monorepo/projects/multi-module-app/src/app/core/workspace/tab.service.ts`
- Route guards:
  `frontend-monorepo/projects/multi-module-app/src/app/core/guards/auth.guard.ts`,
  `frontend-monorepo/projects/multi-module-app/src/app/core/guards/module-access.guard.ts`
- HTTP interceptor:
  `frontend-monorepo/projects/multi-module-app/src/app/core/interceptors/api.interceptor.ts`
- Startup bootstrap:
  `APP_INITIALIZER` factory in `frontend-monorepo/projects/multi-module-app/src/app/app.module.ts`
- Component-local signals:
  `frontend-monorepo/projects/multi-module-app/src/app/features/auth/login/login.component.ts`

---

## Data Fetching

Data fetching is split by reuse level:

- use services when the data or auth state is shared across the app or should be cached
  Examples:
  `AuthService.initialize()` loading `/api/me`,
  `ConfigService.loadExternalApps()`
- use guards/interceptors when the fetch or redirect is part of navigation or global request handling
  Examples:
  `AuthGuard`,
  `ApiInterceptor`
- use component-local HTTP calls when the data is tightly coupled to one screen and not reused elsewhere
  Example:
  `DataQueryComponent` loading query results

This repo does not use React Query, SWR, or an Angular query library.

---

## Naming Conventions

- Services end with `Service`
- Guards end with `Guard`
- Interceptors end with `Interceptor`
- bootstrap factories use imperative names such as `initializeAuth`
- signal state is declared inline in the owning component rather than extracted into a `useSomething` helper

Do not create `useAuth`, `useTabs`, `useConfig`, or similar hook-shaped files.

---

## Common Mistakes

- Introducing React vocabulary or file naming into an Angular codebase.
- Hiding route/auth side effects inside random helper functions instead of guards or interceptors.
- Re-fetching startup auth state from every component instead of using `APP_INITIALIZER` and `AuthService`.
- Moving service-owned shared state into a component just because one screen currently uses it.
- Turning every reusable function into a service when a plain local helper or typed class method would be simpler.
