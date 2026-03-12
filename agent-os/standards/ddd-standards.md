# DDD Standards (Mandatory)

This document defines mandatory architecture, naming, and coding rules.
It applies to all code generated or modified by the agent.

If any rule conflicts with local project rules, prefer the stricter rule.
If a change would violate these rules, do not implement it; propose an alternative
that respects the architecture.

---

## 1) Architecture: Layers and Dependency Rules (STRICT)

### 1.1 Layering
The codebase is organized into these layers:

- **domain**
    - Pure business model: entities, value objects, aggregates, domain services, domain events, domain repository interfaces.
- **application**
    - Use cases (or services) orchestrating domain logic.
    - Ports (interfaces) for external dependencies (clock, storage, remote providers).
    - Application DTOs (request/response) that are framework-agnostic.
- **infrastructure** (or **data**)
    - Implementations of ports and repositories.
    - HTTP clients, persistence, caching, serialization, mapping.
- **presentation** (UI / API controllers)
    - UI state, view models, controllers, adapters for user input/output.

### 1.2 Allowed dependencies (one-way)
- presentation → application
- application → domain
- infrastructure/data → application + domain
- domain → (no project layer)

### 1.3 Forbidden dependencies (MUST NOT)
- domain MUST NOT depend on: HTTP clients, Ktor, databases, SQL, serialization libraries, DI frameworks, UI frameworks.
- application MUST NOT depend on: UI frameworks, platform-specific APIs, concrete infrastructure implementations.
- presentation MUST NOT call infrastructure implementations directly (only application layer).
- infrastructure MUST NOT leak its DTOs/models into domain or application.

### 1.4 Import restrictions (practical enforcement)
- `domain/**` MUST NOT import:
    - `io.ktor.*`
    - `kotlinx.serialization.*`
    - `android.*`, `ios.*`, `platform.*`, `javafx.*`
    - `react.*`, `androidx.compose.*`, `androidx.lifecycle.*`
    - `java.sql.*` and SQL libraries
- `application/**` MUST NOT import:
    - UI frameworks (`androidx.compose.*`, `react.*`)
    - Platform-specific packages (`android.*`, `platform.*`, `ios.*`)
    - HTTP/DB frameworks (unless explicitly in infrastructure)
- `presentation/**` MUST NOT import infrastructure repository implementations.

---

## 2) DDD Modeling Rules (MANDATORY)

### 2.1 Domain-first workflow
When implementing a feature:
1) Model the domain objects (entities/value objects/aggregates) with invariants.
2) Add domain behavior (methods) or domain services if behavior does not belong to a single aggregate.
3) Add application use case(s) to orchestrate behavior.
4) Add infrastructure implementations (remote/local) and mapping.
5) Add presentation adapter (UI/controller).

### 2.2 Domain objects
#### Entities
- Have identity (e.g., `BannerId`).
- Equality is based on identity.
- Keep invariants inside constructors/factories.

#### Value Objects
- Immutable.
- Equality is structural.
- Validate invariants in init/factory.

#### Aggregates
- One aggregate root controls invariants and consistency boundaries.
- Do not expose mutable internal collections directly; expose read-only views.

### 2.3 Domain services
Use a Domain Service only when:
- Logic does not naturally belong to a single entity/aggregate root.
- It is still pure domain logic (no IO).

### 2.4 Repositories
- Repository **interfaces** live in `domain.repository`.
- Repository **implementations** live in `infrastructure.repository` (or `data.repository`).
- Domain NEVER sees repository implementations.

### 2.5 Domain events (optional)
Use domain events if they simplify decoupling within the domain/application.
Events must remain in domain and be plain data + semantics.

---

## 3) Application Layer Rules (MANDATORY)

### 3.1 Use cases
- Use cases orchestrate domain operations and dependencies.
- Use cases MUST NOT contain complex business rules that belong in domain.
- Use cases MUST depend only on:
    - domain (models/services/repos interfaces)
    - application ports (interfaces)
    - simple application DTOs

### 3.2 Ports
- All external dependencies (time, storage, remote providers) MUST be expressed as interfaces in `application.port`.
- Infrastructure provides implementations.

### 3.3 Error handling
- All IO-bound operations should return a unified result type (e.g., `ApiResult<T>`).
- Do NOT throw exceptions for expected failures (network, not found, parsing).
- Throw only for programmer errors (illegal state) or truly exceptional conditions.

### 3.4 Transactions / consistency
If persistence is involved:
- The application layer defines the transaction boundary (e.g., one use case = one transaction).
- Infrastructure implements it (DB transaction, etc.).

---

## 4) Infrastructure/Data Layer Rules (MANDATORY)

### 4.1 Mapping
- Remote DTOs and persistence entities MUST NOT leak outside infrastructure.
- Always map:
    - `RemoteDto` → `Domain` via `infrastructure.mapper`
    - `Domain` → `PersistenceEntity` via `infrastructure.mapper`

### 4.2 Serialization
- Serialization annotations and JSON field names belong only to infrastructure DTOs.
- Domain models MUST remain serialization-agnostic.

### 4.3 HTTP clients, persistence, caching
- All of it stays in infrastructure/data.
- Expose only ports/repository interfaces to application/domain.

### 4.4 Determinism for testing
- Time must use a `Clock` port.
- Randomness must use a `RandomProvider` port (if used).
- Environment/config should be a port (or injected config object) and not read directly inside domain/application.

---

## 5) Presentation Layer Rules (MANDATORY)

### 5.1 Presentation responsibilities
- Translate user input to application requests.
- Call use cases.
- Map use case results into UI state or API responses.

### 5.2 Forbidden
- UI/controller MUST NOT do domain-level validation logic beyond basic input checks.
- UI/controller MUST NOT access DB/HTTP clients directly.
- UI/controller MUST NOT instantiate infrastructure repos directly.

---

## 6) Naming Conventions (STRICT)

### 6.1 Kotlin naming
- Packages: `lowercase.with.dots`
- Classes/Interfaces: `PascalCase`
- Functions/vars: `camelCase`
- Constants: `UPPER_SNAKE_CASE`

### 6.2 DDD naming
- Entity/Aggregate Root: `Banner`, `UserAccount`, `GachaGame`
- ID value objects: `BannerId`, `UserId`
- Repositories (interfaces): `BannerRepository`
- Repository implementations: `BannerRepositoryImpl`
- Domain services: `BannerEligibilityService` (only when needed)

### 6.3 Use case naming
- `VerbNounUseCase` (e.g., `GetCurrentBannersUseCase`, `ToggleFavoriteBannerUseCase`)
- Requests/responses:
    - `GetCurrentBannersRequest`
    - `GetCurrentBannersResponse`

### 6.4 DTO naming
- Remote DTO: `XxxRemoteDto`
- Persistence entity: `XxxEntity` (or `XxxDbEntity`)
- Mappers: `XxxMapper`

---

## 7) Code Style & Quality (STRICT)

### 7.1 Kotlin style
- Follow Kotlin official style.
- Prefer immutability (`val`) and pure functions in domain.

### 7.2 Nullability
- Avoid nullable fields in domain unless domain semantics require it.
- Use sealed classes or explicit types instead of `null` to represent states.

### 7.3 Coroutines
- Expose `suspend` APIs for IO.
- Domain should generally be synchronous (pure) unless there is a strong reason.

### 7.4 No duplication
- If logic is duplicated across layers, refactor to the correct layer:
    - business rules → domain
    - orchestration → application
    - integration/mapping → infrastructure

---

## 8) Testing Rules (MANDATORY when logic is non-trivial)

### 8.1 Unit tests
- Domain invariants and behaviors MUST have unit tests.
- Use cases with branching logic MUST have unit tests.
- Infrastructure mapping MUST have tests for key conversions.

### 8.2 Architecture tests (recommended)
Add enforceable tests to ensure:
- domain imports forbidden packages = 0
- application imports forbidden packages = 0
- repositories follow interface/impl separation

---

## 9) Agent Execution Checklist (MUST FOLLOW)

Before writing code:
1) Identify target layer(s).
2) Confirm dependencies are legal.
3) Model domain changes first (if any).
4) Add/update use case(s).
5) Implement infrastructure pieces + mapping.
6) Update presentation adapter.
7) Add/adjust tests.

Before finishing:
- Ensure naming rules are respected.
- Ensure no forbidden imports exist.
- Ensure domain remains framework-free.
- Ensure mapping prevents DTO leakage.
- Ensure errors follow the unified result strategy.

---

## 10) If a requirement conflicts with DDD
If a feature request suggests:
- domain depending on HTTP/DB
- use case doing network calls without ports
- UI calling repository implementations

Then refuse that approach and implement a port/repository interface + infrastructure implementation instead.