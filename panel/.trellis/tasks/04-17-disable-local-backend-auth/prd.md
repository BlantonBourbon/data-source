# Disable Auth For Local Backend Run

## Goal
Allow the backend to run locally without interactive authentication while keeping the frontend usable against the local profile.

## Requirements
- Disable backend authentication for the local profile only.
- Keep non-local profiles on the existing security path.
- Return a stable mock user context from `/api/me` when local auth is disabled.
- Give the mock user enough permissions for the existing local frontend modules.
- Avoid breaking the in-progress OIDC/security work already present in the worktree.

## Acceptance Criteria
- [ ] Running the backend with the local profile does not require OIDC login.
- [ ] `/api/me` returns a mock authenticated user in local mode.
- [ ] `/api/user/**` endpoints are accessible locally without login.
- [ ] Existing security tests still pass or are updated for the local-only behavior.
- [ ] The backend can be started locally after the change.

## Technical Notes
- Scope the behavior behind a `panel.security.local-dev.*` property set in `application-local.properties`.
- Prefer a local-only mock user contract over changing frontend auth logic.
- Keep production and test security behavior unchanged.
