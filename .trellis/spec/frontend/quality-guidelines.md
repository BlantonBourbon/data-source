# Quality Guidelines

> Code quality standards for frontend development.

---

## Overview

Frontend quality here is enforced by a combination of:

- Angular ESLint + template linting
- Prettier
- Jasmine/Karma unit tests
- human review for architecture, a11y, and auth/route behavior that lint rules do not fully cover

Run at minimum:

- `cd frontend-monorepo && npm run lint`
- `cd frontend-monorepo && npm test -- --watch=false`

---

## Forbidden Patterns

- Duplicating auth or authorization logic inside random components when `AuthService`, `AuthorizationService`, guards, or the interceptor already own it.
- Introducing a React-style state/query stack into this Angular app for isolated changes.
- Moving reusable configuration contracts out of `core/config/` into component-local untyped objects.
- Hiding non-trivial business logic directly inside templates.
- Treating relaxed template lint rules as permission to ignore accessibility.

---

## Required Patterns

- Keep tests adjacent to the code under test as `*.spec.ts`.
  Examples:
  `auth.service.spec.ts`,
  `api.interceptor.spec.ts`,
  `workspace.component.spec.ts`
- Preserve established app-shell patterns:
  `APP_INITIALIZER` for auth bootstrap,
  route guards for navigation gating,
  interceptor-based handling for `401`/`403`
- Keep route/query-param synchronization explicit when a feature is URL-addressable.
  Example:
  `WorkspaceComponent.syncRouteWithCurrentState()`
- Prefer OnPush change detection where the surrounding component already uses it and updates are explicitly controlled.

---

## Testing Requirements

- Use `TestBed`-based specs for services, guards, interceptors, and components.
- Use `HttpTestingController` for HTTP-facing services.
  Example:
  `frontend-monorepo/projects/multi-module-app/src/app/core/services/auth.service.spec.ts`
- Add guard/interceptor tests when auth or authorization flows change.
  Examples:
  `auth.guard.spec.ts`,
  `api.interceptor.spec.ts`,
  `authorization.service.spec.ts`
- Add component tests when UI behavior or route synchronization changes.
  Examples:
  `home.component.spec.ts`,
  `workspace.component.spec.ts`,
  `data-query.component.spec.ts`

For cross-layer changes, manually verify the app against the local backend profile and dev proxy.

---

## Code Review Checklist

- Is the code in the correct ownership boundary: `core/`, `features/`, or `shared/components/`?
- If the change touches auth or permissions, does it still align with `AuthService`, `AuthorizationService`, guards, and `ApiInterceptor`?
- If the change touches workspace or navigation state, are route/query params still the source of truth where expected?
- Are templates still accessible, with labels and meaningful button semantics?
- Is `any` confined to the genuinely dynamic boundaries, rather than spreading into shared models/services?
- Are tests present for the changed behavior, not only for helper methods?
