# Backend Development Guidelines

> Repo-grounded backend standards for the Spring Boot application in `panel/`.

---

## Scope

These guidelines describe the backend that currently lives in `panel/`:

- Java 17 + Spring Boot 3.4
- Spring Web, Spring Data JPA, Spring Security, OAuth2 client
- H2-backed local/test setup with SQL bootstrap files
- Generic entity/controller scaffolding driven by repositories, mappers, and the entity registry

Document reality, not target-state architecture. If the codebase and these docs disagree, update the docs only after confirming the code convention has actually changed.

---

## Read This Before Backend Work

1. Read this index first.
2. Read the detailed guides for the files you will touch:
   - [Directory Structure](./directory-structure.md)
   - [Database Guidelines](./database-guidelines.md)
   - [Type Safety](./type-safety.md)
   - [Error Handling](./error-handling.md)
   - [Logging Guidelines](./logging-guidelines.md)
   - [Quality Guidelines](./quality-guidelines.md)
3. If the change affects auth, redirects, or frontend contracts, also read `.trellis/spec/frontend/` and the cross-layer guide.
4. For local backend work, prefer the `local` profile:
   - `cd panel && ./gradlew bootRun --args='--spring.profiles.active=local'`
   - `panel/src/main/resources/application-local.properties` currently sets `panel.security.local-dev.auth-disabled=true`

---

## Guidelines Index

| Guide | Description | Status |
|-------|-------------|--------|
| [Directory Structure](./directory-structure.md) | Package layout, generated/manual split, module boundaries | Filled |
| [Database Guidelines](./database-guidelines.md) | JPA, SQL bootstrap, query patterns, scaffold flow | Filled |
| [Type Safety](./type-safety.md) | Model/entity separation, typed configs, dynamic boundaries | Filled |
| [Error Handling](./error-handling.md) | Local exception handling, auth status/redirect behavior | Filled |
| [Logging Guidelines](./logging-guidelines.md) | SLF4J usage, security diagnostics, safe fields | Filled |
| [Quality Guidelines](./quality-guidelines.md) | Required tests, review checklist, forbidden shortcuts | Filled |

---

## Non-Negotiable Conventions

- Match the generic entity pipeline unless there is a clear reason to diverge: entity/model -> mapper -> repository -> registry -> generic service -> controller.
- Keep security-specific behavior inside `panel/src/main/java/com/data/service/core/security/`, not mixed into generic CRUD packages.
- Preserve the generated/manual split established by `panel/gradle/code-gen.gradle`. Generated base classes are disposable; hand-written extension points are not.
- Treat `application-local.properties`, `application-test.properties`, and `application-prod.properties` as part of the backend contract. Profile behavior is tested and should not drift silently.

---

## Reference Files

- `panel/src/main/java/com/data/service/core/controller/GenericEntityController.java`
- `panel/src/main/java/com/data/service/core/controller/EntityRegistry.java`
- `panel/src/main/java/com/data/service/core/service/GenericService.java`
- `panel/src/main/java/com/data/service/core/security/SecurityConfiguration.java`
- `panel/src/test/java/com/data/service/core/security/LocalProfileStartupTest.java`

---

**Language**: All documentation in this directory should stay in **English**.
