# Logging Guidelines

> How logging is done in this project.

---

## Overview

The backend uses SLF4J with `LoggerFactory`. Logging is currently concentrated in security and request-diagnostic flows rather than every CRUD path.

Representative files:

- `panel/src/main/java/com/data/service/core/security/LoggingAuthorizationRequestRepository.java`
- `panel/src/main/java/com/data/service/core/security/ReturnUrlAuthenticationFailureHandler.java`

The house style is parameterized, single-message structured logging with stable field names.

---

## Log Levels

- `info`
  Use for normal auth lifecycle events and request-state transitions that matter in production diagnostics.
  Example:
  saved/loaded/removed OIDC authorization requests in `LoggingAuthorizationRequestRepository`
- `warn`
  Use for recoverable problems, suspicious state mismatches, or expected failures that still need attention.
  Example:
  auth-request removal failures and authentication failures in `ReturnUrlAuthenticationFailureHandler`
- `error`
  Reserve for failures that abort a request or startup and need operator attention. This codebase does not currently overuse `error`, which is good.
- `debug`
  Use sparingly for temporary or high-volume diagnostics. Do not introduce noisy debug logs on hot request paths without a real need.

---

## Structured Logging

- Prefer parameterized messages over string concatenation.
- Include stable fields in a consistent order so logs are easy to grep and compare.
- Include request/auth context when it materially helps diagnosis:
  - session identifiers
  - callback state
  - request URI and query string summary
  - forwarded host/proto/port headers
  - cookie names, not cookie values
- Pass the exception object as the final logger argument when a stack trace is useful.

Good examples:

- `LoggingAuthorizationRequestRepository` logs `sessionId`, `callbackState`, `requestUri`, forwarded headers, and cookie names
- `ReturnUrlAuthenticationFailureHandler` logs `errorCode`, `exceptionType`, session/request metadata, and the exception itself

---

## What To Log

- Authentication lifecycle events that are hard to reproduce locally
- Redirect and callback correlation fields during OAuth2/OIDC flows
- Security context mismatches across proxy/load-balancer boundaries
- Failures whose root cause depends on request metadata

In practice, log when the data will help explain:

- why a request was rejected
- why a callback state could not be matched
- why a redirect target changed

---

## What NOT To Log

- OAuth tokens, client secrets, raw cookie values, or authorization codes
- Full claim payloads unless the change explicitly needs them and they are redacted
- Raw request bodies for user/auth routes by default
- PII that is not required for diagnosis
- `System.out.println` and ad hoc console debugging in committed code

---

## Common Mistakes

- Logging sensitive values because they are convenient during auth debugging.
- Using concatenated log strings that hide field names or make grep-based support harder.
- Adding verbose logs to high-frequency CRUD paths with no operational value.
- Omitting proxy-related headers in auth logs, which makes callback issues much harder to diagnose.
