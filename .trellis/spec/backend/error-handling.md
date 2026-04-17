# Error Handling

> How errors are handled in this project.

---

## Overview

This backend does not currently use a project-wide `@ControllerAdvice` or a universal JSON error envelope. Error handling is mostly local and explicit:

- generic controllers return `404` or `204` directly where appropriate
- security filter chains return `401`, `403`, or redirects depending on the endpoint family
- auth success/failure handlers log context and redirect to the frontend
- most service/repository exceptions are allowed to bubble unless a caller can add meaningful handling

Match this explicit style unless you are intentionally standardizing error behavior across the whole backend.

---

## Error Types

- Local controller-specific runtime exceptions with `@ResponseStatus`
  Example:
  `panel/src/main/java/com/data/service/core/controller/GenericEntityController.java`
- Spring Security entry points and handlers for auth failures and access denial
  Example:
  `panel/src/main/java/com/data/service/core/security/SecurityConfiguration.java`
- OAuth-specific authentication failures that are inspected and logged before redirecting
  Example:
  `panel/src/main/java/com/data/service/core/security/ReturnUrlAuthenticationFailureHandler.java`

There are few custom exception classes today. Avoid creating a large exception taxonomy unless it improves behavior for real callers.

---

## Error Handling Patterns

- For unknown dynamic entity names, fail fast in the controller and return `404`.
  Example:
  `GenericEntityController#getServiceOrThrow`
- For missing records, return `ResponseEntity.notFound()` when the route semantics are simple.
  Example:
  `GenericEntityController#getById`
- For unauthenticated API requests, prefer HTTP status responses over HTML login redirects.
  Example:
  `/api/user/**` uses `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` in `SecurityConfiguration`
- For frontend login flows, redirect with a preserved `returnUrl` and user-facing error message instead of exposing backend exception details.
  Example:
  `ReturnUrlAuthenticationFailureHandler`

Catch exceptions only when you can:

- map them to a different HTTP behavior
- attach useful request/auth context to logs
- preserve UX-critical redirect state

---

## API Error Responses

Current API behavior is endpoint-family specific:

- Generic entity routes:
  - unknown entity key -> `404`
  - unknown entity id -> `404`
  - successful delete -> `204`
- User-facing API security routes:
  - unauthenticated `/api/user/**` -> `401`
- Grafana application routes:
  - missing/unknown certificate -> `401`
  - authenticated-but-forbidden -> `403`
- Frontend auth entrypoints:
  - login/logout flows redirect rather than returning JSON error bodies

Representative tests:

- `panel/src/test/java/com/data/service/core/security/SecurityIntegrationTest.java`
- `panel/src/test/java/com/data/service/core/security/LocalProfileStartupTest.java`

---

## Common Mistakes

- Swallowing backend exceptions in controllers and returning misleading success responses.
- Redirecting API requests to an interactive login page when the frontend expects `401`.
- Adding broad `try/catch (Exception)` blocks without logging enough context or changing behavior meaningfully.
- Losing `returnUrl`, OIDC state, or request context when handling authentication failures.
- Introducing a global error envelope without updating tests and all existing callers that currently depend on status-code-specific behavior.
