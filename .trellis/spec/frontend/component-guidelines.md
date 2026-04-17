# Component Guidelines

> How components are built in this project.

---

## Overview

Components in this repo are class-based Angular components with separate HTML and SCSS files in most cases. The codebase is primarily NgModule-driven, but it does contain a mixed reality:

- many route and shell components are explicitly `standalone: false`
- some reusable components declare their own `imports` in the component decorator
- Angular Material and AG Grid are the main UI dependencies
- Angular template control flow syntax (`@if`, `@for`, `@switch`) is actively used

Match the surrounding pattern instead of forcing a different component style into an existing area.

---

## Component Structure

Typical structure:

- decorator with `selector`, `templateUrl`, stylesheet reference, and optional `changeDetection`
- typed `@Input()` and `@Output()` members when data crosses component boundaries
- lifecycle methods for initialization/sync
- helper methods kept in the component class, not in the template

Representative examples:

- `frontend-monorepo/projects/multi-module-app/src/app/core/home/home.component.ts`
  OnPush component with injected services and derived getter state.
- `frontend-monorepo/projects/multi-module-app/src/app/core/workspace/workspace.component.ts`
  Shell component coordinating route params, authorization, and tab restoration.
- `frontend-monorepo/projects/multi-module-app/src/app/shared/components/query-builder/query-builder.component.ts`
  Reusable component with typed input/output and form logic.

Inline templates are acceptable only for very small support components.
Example:
`AggregationDialogComponent` inside `features/data-query/data-query.component.ts`

---

## Data And Composition Conventions

- Use explicit interfaces for cross-component contracts.
  Examples:
  `QueryCondition`,
  `TabItem`,
  `DataQueryConfig`
- Keep reusable composition typed through `@Input()` and `@Output()`, not through implicit DOM coupling.
  Examples:
  `QueryBuilderComponent.availableFields`,
  `QueryBuilderComponent.querySubmit`,
  `DynamicTableComponent.rowClicked`
- Prefer container/presenter-style composition where shell components orchestrate services and route state while shared components stay focused on UI behavior.
  Examples:
  `WorkspaceComponent` + `TabService`,
  `DataQueryComponent` + `QueryBuilderComponent`

---

## Styling Patterns

- Use colocated SCSS files per component.
- Use Angular Material primitives for controls, forms, menus, tabs, icons, and dialogs.
- Use AG Grid for dense tabular data instead of hand-rolled table systems.
- Keep layout/state CSS class names semantic and tied to the feature, not generic utility soup.

Examples:

- `frontend-monorepo/projects/multi-module-app/src/app/core/home/home.component.scss`
- `frontend-monorepo/projects/multi-module-app/src/app/features/data-query/data-query.component.scss`
- `frontend-monorepo/projects/multi-module-app/src/app/shared/components/dynamic-table/dynamic-table.component.scss`

---

## Accessibility

Template accessibility rules are partially relaxed in ESLint, but accessibility is still required by review.

Expected patterns:

- label inputs explicitly or via Material form-field labels
- provide `aria-label` where the UI uses icon-only controls
- keep interactive elements as real buttons/links
- preserve keyboard navigation for tabs, dialogs, menus, and data actions

Representative examples:

- `app.component.html` uses `aria-label` and an off-screen label for quick launch
- `data-query.component.html` labels export actions and filter inputs
- `login.component.html` and `query-builder.component.html` rely on explicit field labels and hints

---

## Common Mistakes

- Putting non-trivial filtering/formatting logic directly in templates instead of moving it into class methods or typed getters.
- Mixing feature-specific behavior into shared components until they are no longer reusable.
- Replacing typed inputs/outputs with `any` when the contract is known.
- Assuming every component should be rewritten to a standalone-only pattern. This repo is still primarily NgModule-oriented.
- Forgetting to preserve `ChangeDetectionStrategy.OnPush` on components that already depend on explicit update patterns.
