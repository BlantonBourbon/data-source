# Type Safety

> Type safety patterns for the Spring Boot backend.

---

## Overview

The backend is strongly typed by default and already separates several concerns cleanly:

- JPA persistence classes (`*Entity`)
- API/domain models without the `Entity` suffix
- typed request DTOs for search and metric operations
- typed configuration binding via `@ConfigurationProperties`
- immutable record-based response shapes where appropriate

The only intentionally dynamic boundaries are the generic entity dispatch layer and a few map-shaped payloads such as claims and scoped data.

---

## Type Organization

- Keep persistence and API/domain models separate.
  Examples:
  `panel/src/main/java/com/data/service/core/model/TradeEntity.java`,
  `panel/src/main/java/com/data/service/core/model/Trade.java`
- Use mappers to cross the entity/model boundary.
  Examples:
  `panel/src/main/java/com/data/service/core/mapper/EntityMapper.java`,
  `panel/src/main/java/com/data/service/core/mapper/TradeMapperBase.java`,
  `panel/src/main/java/com/data/service/core/mapper/TradeMapper.java`
- Use dedicated request types for search/query operations instead of generic maps once the structure is known.
  Examples:
  `panel/src/main/java/com/data/service/core/search/SearchRequest.java`,
  `panel/src/main/java/com/data/service/core/search/MetricRequest.java`
- Bind configuration into typed objects instead of repeatedly reading raw string properties.
  Example:
  `panel/src/main/java/com/data/service/core/security/PanelSecurityProperties.java`

---

## Required Patterns

- Preserve the generic service signature `GenericService<M, E>` when building reusable CRUD/query behavior.
- Keep enum-constrained operations as enums, not arbitrary strings.
  Example:
  `panel/src/main/java/com/data/service/core/search/SearchOperation.java`
- Use Java records for read-only transport shapes when that fits the use case.
  Example:
  `panel/src/main/java/com/data/service/core/security/BackendUserContext.java`
- Keep type conversion centralized. Current code parses `LocalDate` search values inside `GenericSpecification` instead of spreading date parsing across controllers and services.

---

## Dynamic Boundaries

These patterns exist in the repo and are acceptable only at the edges:

- `@RequestBody Map<String, Object>` in `GenericEntityController#create` because entity type is selected dynamically at runtime
- `Map<String, Object>` for claims and data scopes in `BackendUserContext` and `PanelSecurityProperties.LocalDev`
- targeted raw-type suppression inside reflection/generic discovery code such as `EntityRegistry` and `GenericEntityController`

If you need a cast or raw type:

- keep it local to the registry/dispatch boundary
- suppress warnings narrowly
- immediately convert back into typed models or DTOs

---

## Forbidden Patterns

- Do not expose JPA entities directly from controllers when a model + mapper pair already exists.
- Do not replace typed request DTOs with `Map<String, Object>` just because the frontend sends JSON.
- Do not spread raw `Object`, raw `Map`, or raw repository/service types through the service layer.
- Do not encode dates, booleans, or numeric comparisons as arbitrary strings outside the established query contract.

---

## Good Examples

- `panel/src/main/java/com/data/service/core/service/GenericService.java`
  Shows typed generics for repository, specification executor, mapper, model class, and entity class.
- `panel/src/main/java/com/data/service/core/security/PanelSecurityProperties.java`
  Shows nested typed configuration objects instead of loose property lookups.
- `panel/src/main/java/com/data/service/core/controller/EntityRegistry.java`
  Contains the unavoidable raw/generic bridge in one place rather than leaking it across the codebase.
