# Type Safety

> Type safety patterns in this project.

---

## Overview

The frontend runs with strict TypeScript and strict Angular templates:

- `strict: true`
- `noImplicitOverride: true`
- `noPropertyAccessFromIndexSignature: true`
- `strictTemplates: true`

At the same time, the repo intentionally allows limited dynamic typing at AG Grid and generic-data boundaries. The standard here is not "never use `any`"; it is "contain `any` to genuinely dynamic edges and normalize data quickly."

---

## Type Organization

- Put shared application contracts in `core/models/`.
  Example:
  `frontend-monorepo/projects/multi-module-app/src/app/core/models/auth.models.ts`
- Put reusable feature configuration contracts in `core/config/`.
  Example:
  `frontend-monorepo/projects/multi-module-app/src/app/core/config/data-query.config.ts`
- Keep feature-local or component-local interfaces near the component when they are not reused elsewhere.
  Examples:
  `ModuleCard` in `core/home/home.component.ts`,
  `QueryTab` in `features/data-query/data-query.component.ts`
- Type service state explicitly.
  Examples:
  `BehaviorSubject<AuthUser | null>`,
  `BehaviorSubject<TabItem[]>`,
  `signal(false)`

---

## Validation

This repo does not currently use Zod, Yup, or io-ts. Runtime validation is handled in code close to the boundary:

- normalize backend payloads inside services
  Example:
  `AuthService.mapBackendUserContext()`
- normalize permission lists, strings, and optional fields before using them
  Examples:
  `AuthorizationService.normalizeList()`,
  `AuthService.normalizeString()`
- validate UI-specific inputs locally in the owning component
  Example:
  `DataQueryComponent` email export validation

Prefer small, explicit normalization helpers over spreading unchecked property access through templates and components.

---

## Common Patterns

- `HttpClient` generics for typed fetches:
  `this.authHttp.get<BackendUserContext>(...)`
- utility types such as `Pick<...>` for narrowed contracts:
  `AuthorizationService.hasModuleAccess(...)`
- `Record<string, unknown>` or `Record<string, object-like>` shapes for dynamic but still bounded data
- string literal unions for constrained UI state:
  `currentTab: 'all' | 'favorites'`
- `Type<unknown>` for dynamically rendered component types in the workspace tab model

---

## Forbidden Patterns

- Do not spread `any` through services and shared models when the shape is known.
- Do not use type assertions to skip normalization of backend or route data unless there is no better boundary available.
- Do not mirror backend optional fields as always-present values in templates without a normalization step.
- Do not move dynamic data from typed config/models into untyped ad hoc object literals.

Limited `any` usage is acceptable today at truly dynamic boundaries, for example:

- AG Grid row data in `DynamicTableComponent`
- query result arrays in `DataQueryComponent`

Even there, prefer moving back to typed interfaces once a result shape becomes stable.

---

## Common Mistakes

- Treating `no-explicit-any` being disabled in ESLint as permission to stop typing everything.
- Accessing route params or backend payloads directly in templates before they have been normalized.
- Repeating permission/string normalization logic in many components instead of centralizing it in a service.
