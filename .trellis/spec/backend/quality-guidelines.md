# Quality Guidelines

> Code quality standards for backend development.

---

## Overview

The backend quality bar is defined more by behavioral consistency than by a heavy style toolchain. The key expectations are:

- preserve the generic entity architecture unless a change truly needs a custom path
- preserve generated/manual separation
- keep auth/profile behavior explicit and tested
- add tests for externally visible behavior, not only happy-path implementation details

Run at minimum:

- `cd panel && ./gradlew test`

---

## Forbidden Patterns

- Duplicating CRUD/query code in bespoke controllers or services when the generic registry/service flow already supports the entity.
- Returning JPA entities directly to clients when the codebase already has model + mapper layers.
- Editing generated files such as `*MapperBase` as if they were hand-written extension points.
- Bypassing `SecurityConfiguration` or auth handlers with controller-local security hacks.
- Introducing schema changes without matching updates to SQL bootstrap, models/entities, and tests.

---

## Required Patterns

- Use constructor injection, `final` dependencies, or Lombok-generated constructor injection patterns already present in the repo.
  Examples:
  `panel/src/main/java/com/data/service/core/controller/GenericEntityController.java`,
  `panel/src/main/java/com/data/service/core/security/ReturnUrlAuthenticationFailureHandler.java`,
  `panel/src/main/java/com/data/service/core/security/SecurityConfiguration.java`
- Keep dynamic entity behavior inside `EntityRegistry`, `GenericService`, repositories, and mappers.
- When adding a new scaffolded entity, update the full chain:
  `entity-model.yaml` -> generated artifacts -> `schema.sql`/`data.sql` -> tests
- Keep local, test, and prod profile behavior explicit in properties files and covered by tests where the behavior matters.

---

## Testing Requirements

- Use `@SpringBootTest` + `@AutoConfigureMockMvc` for HTTP, security chain, and profile behavior.
  Examples:
  `panel/src/test/java/com/data/service/core/security/SecurityIntegrationTest.java`,
  `panel/src/test/java/com/data/service/core/security/LocalProfileStartupTest.java`,
  `panel/src/test/java/com/data/service/core/CryptoAssetIntegrationTest.java`
- Use Mockito/JUnit tests for isolated service logic and specification behavior.
  Examples:
  `panel/src/test/java/com/data/service/core/service/GenericServiceTest.java`,
  `panel/src/test/java/com/data/service/core/search/GenericSpecificationTest.java`
- Add regression tests when changing:
  - auth redirects
  - local profile behavior
  - route authorization
  - dynamic query/metric behavior

---

## Code Review Checklist

- Does the change fit the existing package and generic entity structure?
- If persistence changed, were `schema.sql`, entities/models, mappers, and repositories kept in sync?
- If auth changed, are `401`/`403`/redirect behaviors still correct for `/api/user/**`, `/api/grafana/**`, `/api/auth/**`, and the `local` profile?
- Are logs useful and safe, with no secrets or raw token values?
- Are tests present at the right level: integration for HTTP/security behavior, unit tests for isolated logic?
- Did the change avoid touching generated files unless regeneration was intentional?
