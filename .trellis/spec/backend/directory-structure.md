# Directory Structure

> How backend code is organized in this project.

---

## Overview

The backend is organized primarily by technical role, not by vertical feature slice. Most business entities follow the same generic pipeline, so the main structure is:

- controllers expose audience-specific HTTP routes
- repositories provide persistence access
- mappers bridge API/domain models and JPA entities
- the entity registry wires generic services dynamically
- security flows live in a dedicated package

Do not introduce a parallel package layout for a new entity unless the generic flow is no longer sufficient.

---

## Directory Layout

```text
panel/
├── src/main/java/com/data/service/core/
│   ├── controller/    # Generic and audience-scoped REST controllers
│   ├── mapper/        # Generated base mappers + hand-written extensions
│   ├── model/         # API/domain models and JPA entities
│   ├── repository/    # Spring Data repositories
│   ├── search/        # Search/filter request DTOs and JPA specifications
│   ├── security/      # Auth controllers, handlers, security config, user context
│   └── service/       # Shared generic service logic
├── src/main/resources/
│   ├── application*.properties
│   ├── schema.sql
│   ├── data.sql
│   └── entity-model.yaml
├── src/test/java/com/data/service/core/
│   ├── security/
│   ├── service/
│   ├── search/
│   └── ...
└── gradle/
    ├── code-gen.gradle
    └── templates/
```

---

## Module Organization

- Put HTTP entrypoints in `controller/` when they are generic data endpoints and in `security/` when they are auth/user-context endpoints.
  Examples:
  `panel/src/main/java/com/data/service/core/controller/GenericEntityController.java`,
  `panel/src/main/java/com/data/service/core/controller/UserEntityController.java`,
  `panel/src/main/java/com/data/service/core/security/AuthController.java`
- Keep entity-specific persistence access in one repository per JPA entity under `repository/`.
  Examples:
  `panel/src/main/java/com/data/service/core/repository/TradeRepository.java`,
  `panel/src/main/java/com/data/service/core/repository/CryptoAssetRepository.java`
- Put reusable query/filter DTOs and specifications in `search/`, not inline inside controllers.
  Examples:
  `panel/src/main/java/com/data/service/core/search/SearchRequest.java`,
  `panel/src/main/java/com/data/service/core/search/GenericSpecification.java`
- Keep auth, current-user, redirect handling, and certificate/OIDC flows inside `security/`.
  Examples:
  `panel/src/main/java/com/data/service/core/security/SecurityConfiguration.java`,
  `panel/src/main/java/com/data/service/core/security/CurrentUserController.java`,
  `panel/src/main/java/com/data/service/core/security/LoggingAuthorizationRequestRepository.java`
- Treat `gradle/code-gen.gradle` and `gradle/templates/` as part of the module structure. They generate entities, models, mapper bases, repositories, and concrete mapper shells from `src/main/resources/entity-model.yaml`.

---

## Naming Conventions

- JPA entities end with `Entity`: `TradeEntity`, `CryptoAssetEntity`, `AccountEntity`
- API/domain models use the unsuffixed name: `Trade`, `CryptoAsset`, `Account`
- Repositories end with `Repository`
- Generated mapper bases end with `MapperBase`; hand-written extension points keep the plain `Mapper` name
- Request DTOs end with `Request`: `SearchRequest`, `MetricRequest`
- Configuration holders end with `Properties`: `PanelSecurityProperties`
- Audience-specific controllers use the audience in the name: `UserEntityController`, `GrafanaEntityController`

The entity registry also relies on naming conventions:

- repository bean names are derived from the entity name plus `Repository`
- route keys are derived from the model class name lowercased plus `s`
- current examples include `trades` and `cryptoassets`

---

## Good Examples

- `panel/src/main/java/com/data/service/core/controller/EntityRegistry.java`
  Shows the core project pattern: discover mappers, infer types, locate repositories, build `GenericService` instances dynamically.
- `panel/src/main/java/com/data/service/core/security/SecurityConfiguration.java`
  Keeps all security filter-chain wiring in one package instead of scattering auth concerns across controllers.
- `panel/src/main/java/com/data/service/core/mapper/TradeMapper.java`
  Demonstrates the generated/manual split: the concrete mapper is the safe customization point, while `TradeMapperBase` is generated.

---

## Anti-Patterns And Common Mistakes

- Do not add a custom per-entity service or controller when `EntityRegistry` + `GenericService` already covers the use case.
- Do not place security/auth helpers under `controller/`, `service/`, or `search/`; keep them in `security/`.
- Do not edit generated base classes such as `*MapperBase` as if they were permanent hand-written code. The scaffold task can overwrite them.
- Do not create a generic `utils/` dumping ground. New logic should have an obvious home in `search/`, `security/`, `mapper/`, or another existing package.
