# State Management

> How state is managed in this project.

---

## Overview

This frontend uses service-driven state management, not NgRx/Redux. Current state sources are:

- `BehaviorSubject`-backed singleton services for app/session/workspace state
- route params and query params for URL-shareable workspace state
- component fields for local UI state
- `localStorage` for simple persisted preferences
- selective Angular `signal()` usage in newer component-local code

Match this layering instead of introducing a global store for isolated workflows.

---

## State Categories

- App/session state:
  `AuthService` owns the current user through `currentUserSubject`
- Workspace cross-component state:
  `TabService` owns open tabs and active tab index
- URL state:
  `WorkspaceComponent` restores and synchronizes `tabs` and `active` query params
- Local UI state:
  `HomeComponent` keeps search/favorites/loading in component fields,
  `DataQueryComponent` keeps tab result sets, export state, and view-only selections locally
- Persistent browser state:
  `HomeComponent` stores favorite module ids in `localStorage`
- Local reactive state via signals:
  `LoginComponent` uses `signal()` for sign-in progress and error messaging

---

## When To Use Global State

Promote state into a root-provided service when at least one of these is true:

- multiple routes/components need to read or mutate it
- the state must survive component recreation during navigation
- auth, authorization, or startup behavior depends on it
- the URL needs to be synchronized from a single authority

Current examples:

- `AuthService` because auth state is app-wide
- `TabService` because workspace tabs are coordinated across routing and shell UI
- `ConfigService` because external app config is cached and shared

Keep state local when it is only relevant to one screen instance:

- filter text
- loading spinners
- selected tab-local query results
- transient form input

---

## Server State

Server state is handled with plain Angular/RxJS patterns:

- `AuthService` normalizes `/api/me` into a frontend-safe `AuthUser`
- `ConfigService` caches `/external-apps.json` with `shareReplay(1)` and an in-memory array
- `DataQueryComponent` keeps fetched result sets in component-local tab state instead of a global cache

This repo does not use a dedicated server-state library.

When consuming backend state:

- normalize data once near the service boundary
- cache only when reuse exists
- avoid duplicating the same server payload in both a root service and a component unless there is a clear reason

---

## Common Mistakes

- Adding NgRx-style global state for one narrow screen.
- Keeping the same state in both the router and a service without a clear source of truth.
- Promoting transient form/view state into root services.
- Forgetting to clear or reset shared state on auth/logout transitions.
- Skipping authorization filtering when restoring route-driven tab state.
