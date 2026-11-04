---
name: backend-tu
description: Backend Java/Spring Boot DDD test workflow for CyberManager. Use when adding or reviewing backend unit tests or integration tests, especially for domain business objects and value objects, domain commands/queries, application views, use cases, application services, out.adapters.persistences.repositories, out.entities, out.adapters.persistences, database behavior, JPA mappings, repository adapters, and persistence error cases.
---

# Backend TU

## Overview

Use this skill to add rigorous backend TU/TI coverage without breaking DDD boundaries. Coverage must be systematic across DDD layers: every concrete production class in `domain`, every concrete production class in `application`, every class in `out.adapters.persistences.repositories`, every JPA entity in `out.entities`, and every persistence adapter in `out.adapters.persistences` needs its own dedicated test class.

## Workflow

1. Inventory impacted backend components before editing:
   - `Get-ChildItem -Path backend -Recurse -Include *.java | Where-Object { $_.FullName -match '\\src\\main\\java\\com\\cybermanager(\\auth)?\\domain\\' }`
   - `Get-ChildItem -Path backend -Recurse -Include *.java | Where-Object { $_.FullName -match '\\src\\main\\java\\com\\cybermanager(\\auth)?\\application\\' }`
   - `Get-ChildItem -Path backend -Recurse -Include *.java | Where-Object { $_.FullName -like '*\out\adapters\persistences\repositories\*' }`
   - `Get-ChildItem -Path backend -Recurse -Include *.java | Where-Object { $_.FullName -like '*\out\entities\*' }`
   - `Get-ChildItem -Path backend -Recurse -Include *.java | Where-Object { $_.FullName -like '*\out\adapters\persistences\*' }`
2. Read the domain business object or value object, application command/query/view, use case/service, repository port, JPA entity, Spring Data repository, and adapter together.
3. Create one concrete test class per concrete production class in domain, application, and persistence. Never create a catch-all test suite such as `PersistenceIntegrationTest`, `ApplicationLayerTest`, or `DomainModelTest` that tests several production classes together.
4. Use abstract base classes, test fixtures, builders, or local test configuration classes only for shared setup. Shared support must not contain test methods.
5. Add fast unit tests for domain and application behavior before adding infrastructure integration tests.
6. Add unit tests for pure adapter behavior only when mocking repositories gives useful failure coverage.
7. Add `@DataJpaTest` integration tests for JPA repositories, entity mappings, relations, indexes/constraints observable through behavior, sorting, filtering, and adapter round-trips.
8. Run narrow Maven tests first, then the relevant module tests.

## Coverage Rules

For each concrete `domain` class:
- Create a dedicated test class named after the domain type, for example `UserTest`, `UserIdTest`, or `MoneyTest`.
- Test invariants, factory methods, validation, normalization, state transitions, calculations, equality, and boundary cases.
- Test invalid inputs explicitly: null, blank, negative values, impossible dates, illegal status transitions, duplicated children, or empty collections depending on the type.
- Keep domain tests free of Spring, Mockito, JPA, HTTP DTOs, and infrastructure classes.
- Do not skip simple value objects: if the class exists in production, cover its construction, equality/value semantics, and invalid values.

For each concrete `application` class:
- Create a dedicated test class named after the application type, for example `CreateUserServiceTest`, `CreateUserUseCaseTest`, `CreateUserCommandTest`, or `UserViewTest`.
- Test use cases through their `execute` method, using commands or queries as inputs and views as outputs.
- Test orchestration: called ports, transaction-relevant branches, authorization/current-user assumptions when present, returned views, and domain errors.
- Mock domain ports and gateways at the boundary; do not mock the class under test.
- Test commands, queries, and views for construction contracts, normalization, defaults, validation helpers, equality, and mapping-sensitive fields.
- Keep application tests out of API and infrastructure concerns: no controllers, API DTOs, JPA entities, Spring Data repositories, or HTTP assertions.

For each `out.adapters.persistences.repositories` interface:
- Create a dedicated test class named after the repository, for example `UserJpaRepositoryIntegrationTest`.
- Test at least one successful persisted read/write or query method.
- Test custom finder/query methods explicitly.
- Test a negative case: not found, empty result, duplicate/constraint violation, invalid relation, or delete absence depending on the repository surface.

For each `out.entities` class:
- Create a dedicated test class named after the entity, for example `UserJpaEntityIntegrationTest`.
- Test it through JPA persistence, not only by assigning public fields.
- Validate column mapping for important fields: enum/string status, amounts, dates, booleans, IDs, child collections, and nullable relations.
- Validate relationship behavior: `@ManyToOne`, `@OneToMany`, element collections, cascading, orphan removal, and foreign-key failures when applicable.

For each `out.adapters.persistences` class:
- Create a dedicated test class named after the adapter, for example `UserRepositoryAdapterIntegrationTest`.
- Test domain-to-JPA-to-domain round-trips.
- Test search/filter semantics, sorting, and null/blank parameters.
- Test delete/update behavior where present.
- Test error/edge cases close to production behavior: missing entity returns `Optional.empty`, no current config returns fallback/default when implemented, constraint failures surface from the database, and invalid references fail on flush.

## Naming Rules

- Repository tests: `<RepositoryClassName>IntegrationTest`.
- Entity tests: `<EntityClassName>IntegrationTest`.
- Persistence adapter tests: `<AdapterClassName>IntegrationTest`.
- Domain tests: `<DomainClassName>Test`.
- Application tests: `<ApplicationClassName>Test`.
- Do not use broad names such as `CybermanagerPersistenceIntegrationTest`, `PersistenceAdaptersIntegrationTest`, or `RepositoryLayerIntegrationTest`.
- Do not use broad names such as `CybermanagerDomainTest`, `ApplicationServicesTest`, or `UseCasesTest`.

## DDD Constraints

- Keep domain tests in domain modules, application tests in application modules, and Spring/JPA tests in infrastructure modules.
- Domain modules must not depend on application, infrastructure, API, Spring, JPA, or Mockito-heavy integration setup.
- Application modules may mock ports/gateways but must not depend on API or infrastructure modules.
- Do not make infrastructure tests depend on API modules unless the API is explicitly under test.
- Do not expose or assert API DTO behavior from persistence tests.
- Do not place JPA entities under `infrastructure.entities` or `entities.persistence`; use `infrastructure.out.entities.<bounded-context>` instead.
- Do not place persistence adapters under `infrastructure.out.persistence` or `infrastructure.adapters.persistence`; use `infrastructure.out.adapters.persistences.<bounded-context>`, or `infrastructure.out.adapters.persistences` for a transversal adapter.
- Keep `@DataJpaTest` configuration local to the infrastructure module when no application class exists there.
- Avoid changing production behavior just to satisfy tests; fix real bugs only when the failing behavior is clearly wrong for the feature.

## Maven Commands

Use artifact IDs from `backend/cybermanager-parent`:

```powershell
mvn -pl :cm-infrastructure -am "-Dtest=SomePersistenceIntegrationTest" -DfailIfNoTests=false "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn -pl :cm-application -am "-Dtest=SomeApplicationServiceTest" -DfailIfNoTests=false "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn -pl :cm-domain -am "-Dtest=SomeDomainTest" -DfailIfNoTests=false "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn -pl :cm-auth-domain -am "-Dtest=SomeDomainTest" -DfailIfNoTests=false "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn -pl :cm-auth-application -am "-Dtest=SomeApplicationServiceTest" -DfailIfNoTests=false "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn -pl :cm-domain,:cm-application,:cm-infrastructure -am test
mvn -pl :cm-auth-domain,:cm-auth-application,:cm-auth-infrastructure -am test
mvn -pl :cm-infrastructure -am test
```

Report any pre-existing unrelated failures separately from new test failures.
