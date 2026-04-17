# Database Guidelines

> Database patterns and conventions for this project.

---

## Overview

This backend uses Spring Data JPA on top of H2 for local and test environments. The current database workflow is:

- JPA entities in `model/`
- repositories extending `JpaRepository` and `JpaSpecificationExecutor`
- mapper-based conversion between JPA entities and API/domain models
- dynamic filtering through `SearchRequest` + `GenericSpecification`
- schema bootstrap through `src/main/resources/schema.sql` and `data.sql`

There is currently no Flyway or Liquibase setup. Treat SQL bootstrap files and entity/model mappings as the source of truth for schema changes.

---

## Query Patterns

- Use repository CRUD methods for straightforward create/read/update/delete.
  Examples:
  `panel/src/main/java/com/data/service/core/repository/TradeRepository.java`,
  `panel/src/main/java/com/data/service/core/service/GenericService.java`
- Use `JpaSpecificationExecutor` for user-driven filters from the frontend query builder.
  Examples:
  `panel/src/main/java/com/data/service/core/search/GenericSpecification.java`,
  `panel/src/main/java/com/data/service/core/search/SearchCriteria.java`
- Use `EntityManager` + Criteria API only when query shape goes beyond the repository/specification path, such as aggregations and grouped metrics.
  Example:
  `panel/src/main/java/com/data/service/core/service/GenericService.java`
- Keep query inputs typed at the boundary and normalize them once. This codebase currently parses date strings inside the specification layer instead of scattering conversion across controllers.

---

## Schema And Migration Workflow

- Baseline schema lives in `panel/src/main/resources/schema.sql`
- Seed/sample data lives in `panel/src/main/resources/data.sql`
- Profile-specific behavior is controlled through `application.properties`, `application-local.properties`, `application-test.properties`, and `application-prod.properties`
- Entity scaffolding is generated from `panel/src/main/resources/entity-model.yaml` via `panel/gradle/code-gen.gradle`

When adding or changing a persistent entity, update the following together:

1. `entity-model.yaml` if the entity is scaffolded
2. generated entity/model/repository/mapper-base artifacts if regeneration is required
3. `schema.sql`
4. `data.sql` when sample data matters
5. tests covering repository or API behavior

Do not assume a migration history table or versioned migration runner exists today.

---

## Naming Conventions

- SQL tables use lowercase snake_case names:
  `trade`, `crypto_assets`
- Java entity fields use camelCase and rely on Spring/JPA naming strategy to match SQL column names
- Primary keys are `id` with generated identity values in current tables
- JPA entity classes end with `Entity`
- Repository names match the unsuffixed domain name plus `Repository`
- Route keys are pluralized lowercase model names such as `trades` and `cryptoassets`

Representative files:

- `panel/src/main/resources/schema.sql`
- `panel/src/main/java/com/data/service/core/model/TradeEntity.java`
- `panel/src/main/java/com/data/service/core/model/CryptoAssetEntity.java`

---

## Common Mistakes

- Changing `schema.sql` without updating the corresponding entity, model, mapper, and tests.
- Writing ad hoc query code in controllers instead of routing through `GenericService`, repositories, and specifications.
- Bypassing mappers and returning JPA entities directly to API callers.
- Assuming a Flyway/Liquibase workflow exists and creating one-off migration files that nothing runs.
- Customizing generated files such as `*MapperBase` or generated repositories instead of regenerating or extending the safe hand-written layer.
