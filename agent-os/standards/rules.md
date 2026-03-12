# Stodolist - Project Rules (Comprehensive)

This document consolidates ALL mandatory rules for the Stodolist project.
All contributors (human or AI) MUST follow these rules.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Code Style Guidelines](#2-code-style-guidelines)
3. [Architecture & DDD Rules](#3-architecture--ddd-rules)
4. [Error Handling](#4-error-handling)
5. [Testing Requirements](#5-testing-requirements)
6. [Build & Commands](#6-build--commands)
7. [File Organization](#7-file-organization)
8. [Primitive Obsession Avoidance](#8-primitive-obsession-avoidance-mandatory)
9. [SOLID Principles](#9-solid-principles-mandatory)
10. [DRY, KISS, and STUPID Avoidance](#10-dry-kiss-and-stupid-avoidance-mandatory)
11. [Testing Requirements (Extended)](#11-testing-requirements-mandatory)
12. [Git Commit Convention](#12-git-commit-convention-mandatory)
13. [UI/UX Design Standards](#13-uiux-design-standards-mandatory)
14. [Permission Handling](#14-permission-handling-mandatory)

---

## 1) Project Overview

**Stodolist** is a Kotlin Multiplatform task management application that allows users to create, edit, and delete tasks with reminders and notifications.

**Target Platforms:** Android, iOS

**Tech Stack:**
| Category | Technology | Version |
|----------|------------|---------|
| Language (Shared) | Kotlin | 1.9.21 |
| Mobile UI | Compose Multiplatform | 1.5.11 |
| Database | SQLDelight | 2.0.1 |
| Networking | Ktor Client | 2.3.7 |
| Serialization | kotlinx.serialization | 1.6.2 |
| Coroutines | kotlinx-coroutines | 1.7.3 |
| Android SDK | Target: 34, Min: 24 | - |
| iOS | iosX64, iosArm64, iosSimulatorArm64 | - |

---

## 2) Code Style Guidelines

### 2.1 Kotlin Style

- **Style Guide:** Official Kotlin code style (`kotlin.code.style=official`)
- **JVM Target:** Java 1.8
- **Prefer immutability:** Use `val` over `var`
- **Pure functions in domain:** Domain logic should be free of side effects

#### Naming Conventions (STRICT)

| Element | Convention | Examples |
|---------|------------|----------|
| Packages | `lowercase.with.dots` | `fr.unilim.stodolist.domain` |
| Classes/Interfaces | `PascalCase` | `Task`, `TaskRepository` |
| Functions/Variables | `camelCase` | `getAllTasks()`, `taskId` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Platform-specific files | `FileName.platform.kt` | `DatabaseDriverFactory.android.kt` |

#### DDD-Specific Naming

| Type | Pattern | Examples |
|------|---------|----------|
| Entity/Aggregate Root | Noun | `Task`, `Category` |
| ID Value Objects | `XxxId` | `TaskId`, `CategoryId` |
| Repository Interface | `XxxRepository` | `TaskRepository` |
| Repository Implementation | `XxxRepositoryImpl` | `TaskRepositoryImpl` |
| Domain Service | `XxxService` | `TaskValidationService` |
| Use Case | `VerbNounUseCase` | `GetAllTasksUseCase`, `CreateTaskUseCase` |
| Remote DTO | `XxxRemoteDto` | `TaskRemoteDto` |
| Persistence Entity | `XxxEntity` / `XxxDbEntity` | `TaskEntity` |
| Mappers | `XxxMapper` | `TaskMapper` |
| ViewModel | `XxxViewModel` | `TaskViewModel`, `CommonTaskViewModel` |
| UI Screens | `XxxScreen` | `TaskListScreen`, `AddTaskScreen` |
| UI Components | Descriptive noun | `TaskItem`, `PermissionBanner` |

#### Import Order (Kotlin)

1. Package declaration
2. External libraries
3. Project imports

### 2.2 Compose Multiplatform Style

- **Composables:** Use `@Composable` annotation for all UI functions
- **State management:** Use `remember`, `mutableStateOf`, and `collectAsState` for state
- **Theme usage:** Always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`
- **Modifiers:** Pass `Modifier` as first optional parameter with default `Modifier`
- **Preview:** Add `@Preview` annotation for composables when debugging

#### Composable Function Conventions

```kotlin
@Composable
fun MyComponent(
    modifier: Modifier = Modifier,  // Always first optional parameter
    // Other parameters
    onAction: () -> Unit            // Callbacks last
) {
    // Implementation
}
```

---

## 3) Architecture & DDD Rules

### 3.1 Layer Structure (STRICT)

```
shared/src/commonMain/kotlin/fr/unilim/stodolist/
├── models/          # Domain models (entities, value objects)
├── repository/      # Repository interfaces AND implementations
├── viewmodel/       # Shared ViewModels for Compose Multiplatform
├── database/        # SQLDelight driver factory (expect/actual)
├── ui/              # Shared Compose Multiplatform UI
│   ├── screens/     # Full screen composables
│   ├── components/  # Reusable UI components
│   └── theme/       # Theme, colors, typography, glassmorphism
└── core/            # Core utilities (ApiResult, etc.)

app/src/main/java/fr/unilim/stodolist/
├── ui/              # Android-specific UI (MainActivity, etc.)
├── permissions/     # Android permission handling
└── notifications/   # Android notification scheduling
```

### 3.2 Allowed Dependencies (ONE-WAY ONLY)

```
presentation (ui/) → viewmodel → repository → models
infrastructure (database/, notifications/) → models
models → (NOTHING external)
```

### 3.3 Forbidden Dependencies (MUST NOT VIOLATE)

| Layer | MUST NOT Depend On |
|-------|-------------------|
| `models` | HTTP clients, Ktor, databases, SQL, serialization (except @Serializable for DTO), DI, UI frameworks |
| `viewmodel` | Platform-specific APIs, concrete infrastructure |
| `ui/screens` | Repository implementations directly (use ViewModel) |

### 3.4 Import Restrictions (ENFORCED)

**`models/**` MUST NOT import:**
- `io.ktor.*` (except for data transfer)
- `android.*`, `ios.*`, `platform.*`
- `androidx.compose.*`, `androidx.lifecycle.*`
- `java.sql.*` and SQL libraries
- `app.cash.sqldelight.*`

**`viewmodel/**` MUST NOT import:**
- Platform-specific packages (`android.*`, `platform.*`, `ios.*`)
- Direct database access

**`ui/**` SHOULD NOT import:**
- Repository implementations directly

### 3.5 Domain-First Workflow (MANDATORY)

When implementing a feature, follow this order:
1. Model domain objects (entities/value objects) in `models/`
2. Add repository interface and implementation in `repository/`
3. Add SQLDelight queries in `sqldelight/` if persistence needed
4. Add ViewModel logic in `viewmodel/`
5. Add UI components and screens in `ui/`
6. Add platform-specific implementations (permissions, notifications) in `app/`

### 3.6 Domain Objects Rules

**Entities:**
- Have identity (e.g., `Task.id`)
- Equality based on identity
- Use `data class` with validation in factory/constructor

**Value Objects:**
- Immutable (`val` only)
- Structural equality
- Validate invariants in init

**Models (Current Implementation):**
```kotlin
@Serializable
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false
)
```

### 3.7 Repository Rules

- Repository **interfaces** → `repository/TaskRepository.kt`
- Repository **implementations** → `repository/TaskRepositoryImpl.kt`
- Use `Flow<List<T>>` for observable collections
- Use `suspend` functions for single operations

### 3.8 SQLDelight Integration

- SQL files location: `shared/src/commonMain/sqldelight/`
- Generated package: `fr.unilim.stodolist.database`
- Database name: `StodolistDatabase`
- Use `expect/actual` for `DatabaseDriverFactory`

```kotlin
// commonMain - expect declaration
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain - actual implementation
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(StodolistDatabase.Schema, context, "stodolist.db")
    }
}
```

### 3.9 Kotlin Multiplatform expect/actual

```kotlin
// commonMain
expect fun getPlatform(): Platform

// androidMain
actual fun getPlatform(): Platform = AndroidPlatform()

// iosMain
actual fun getPlatform(): Platform = IOSPlatform()
```

---

## 4) Error Handling

### 4.1 Result-Based Error Handling

Use sealed classes or `Result<T>` for operations that can fail:

```kotlin
sealed class TaskResult<out T> {
    data class Success<T>(val value: T) : TaskResult<T>()
    data class Error(val message: String) : TaskResult<Nothing>()
}
```

### 4.2 Error Handling Rules

- Do NOT throw exceptions for expected failures (not found, validation)
- Throw ONLY for programmer errors or truly exceptional conditions
- Use `Flow` error handling with `catch` operator for stream operations
- Use `try-catch` in ViewModel for database operations

### 4.3 UI Error Handling

- Show user-friendly error messages via Snackbar or Dialog
- Never expose raw exception messages to users
- Log errors for debugging purposes

**Location:** Error utilities should be in `shared/src/commonMain/kotlin/fr/unilim/stodolist/core/`

---

## 5) Testing Requirements

### 5.1 Test Locations

| Test Type | Location |
|-----------|----------|
| Shared tests | `shared/src/commonTest/kotlin/` |
| Android unit tests | `app/src/test/kotlin/` |
| Android instrumented tests | `app/src/androidTest/kotlin/` |

### 5.2 Test Pattern

```kotlin
package fr.unilim.stodolist

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskTest {
    @Test
    fun `should create task with valid title`() {
        val task = Task(title = "Test Task")
        assertEquals("Test Task", task.title)
    }
}
```

### 5.3 Mandatory Tests (when logic is non-trivial)

- Model invariants and behaviors MUST have unit tests
- Repository operations MUST have tests for key operations
- ViewModel state transitions SHOULD have unit tests

### 5.4 Recommended: Architecture Tests

Add tests to ensure:
- Models import no forbidden packages
- Repositories follow interface/impl separation

---

## 6) Build & Commands

### 6.1 Build Commands

```bash
# Build all modules
./gradlew build

# Build shared KMP module
./gradlew :shared:build

# Build Android app
./gradlew :app:assembleDebug

# Build iOS framework
./gradlew :shared:linkDebugFrameworkIosArm64
```

### 6.2 Test Commands

```bash
# All shared module tests (all platforms)
./gradlew :shared:allTests

# Android unit tests
./gradlew :app:testDebugUnitTest

# Run a specific test class
./gradlew :shared:allTests --tests "fr.unilim.stodolist.TaskTest"
```

### 6.3 SQLDelight Commands

```bash
# Generate SQLDelight code
./gradlew :shared:generateCommonMainStodolistDatabaseInterface

# Verify schema
./gradlew :shared:verifySqlDelightMigration
```

### 6.4 Clean Commands

```bash
# Clean build
./gradlew clean

# Clean and rebuild
./gradlew clean build
```

---

## 7) File Organization

### 7.1 Project Structure

```
stodolist/
├── shared/                           # Kotlin Multiplatform shared module
│   ├── build.gradle.kts              # KMP module configuration
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/fr/unilim/stodolist/
│       │   │   ├── models/           # Domain models (Task.kt)
│       │   │   ├── repository/       # TaskRepository, TaskRepositoryImpl
│       │   │   ├── viewmodel/        # CommonTaskViewModel
│       │   │   ├── database/         # DatabaseDriverFactory (expect)
│       │   │   ├── ui/
│       │   │   │   ├── screens/      # TaskListScreen, AddTaskScreen
│       │   │   │   ├── components/   # TaskItem, PermissionBanner, PermissionDialog
│       │   │   │   └── theme/        # Theme, GlassStyle, Animations, Dimensions
│       │   │   └── Platform.kt       # Platform expect declaration
│       │   └── sqldelight/           # SQLDelight .sq files
│       ├── androidMain/              # Android-specific implementations
│       │   └── kotlin/fr/unilim/stodolist/
│       │       └── database/         # DatabaseDriverFactory (actual)
│       └── iosMain/                  # iOS-specific implementations
│           └── kotlin/fr/unilim/stodolist/
│               └── database/         # DatabaseDriverFactory (actual)
├── app/                              # Android application module
│   ├── build.gradle                  # Android app configuration
│   └── src/main/java/fr/unilim/stodolist/
│       ├── ui/                       # MainActivity, PermissionScreen
│       ├── permissions/              # NotificationPermissionHandler, PermissionState
│       └── notifications/            # NotificationScheduler, TaskNotificationReceiver
├── iosApp/                           # iOS SwiftUI application
├── build.gradle                      # Root build configuration
├── settings.gradle                   # Project settings
└── gradle/                           # Gradle wrapper and configuration
```

### 7.2 Key Files Reference

| Purpose | Location |
|---------|----------|
| Root build config | `build.gradle` |
| Shared module config | `shared/build.gradle.kts` |
| Domain models | `shared/src/commonMain/kotlin/fr/unilim/stodolist/models/` |
| Repository | `shared/src/commonMain/kotlin/fr/unilim/stodolist/repository/` |
| SQLDelight queries | `shared/src/commonMain/sqldelight/fr/unilim/stodolist/database/` |
| Theme & Styling | `shared/src/commonMain/kotlin/fr/unilim/stodolist/ui/theme/` |
| Permission handling | `app/src/main/java/fr/unilim/stodolist/permissions/` |

---

## 8) Primitive Obsession Avoidance (MANDATORY)

### 8.1 No Primitive Obsession

**NEVER use primitive types for domain concepts.** Every meaningful concept MUST have its own type.

#### Bad (FORBIDDEN)
```kotlin
// DON'T DO THIS
data class User(
    val id: String,           // ❌ Primitive
    val email: String,        // ❌ Primitive
    val profileUrl: String,   // ❌ Primitive
    val age: Int              // ❌ Primitive
)
```

#### Good (REQUIRED)
```kotlin
// DO THIS
@JvmInline value class UserId(val value: String)
@JvmInline value class Email(val value: String) {
    init { require(value.contains("@")) { "Invalid email format" } }
}
@JvmInline value class Url(val value: String) {
    init { require(value.startsWith("http")) { "Invalid URL" } }
}
@JvmInline value class Age(val value: Int) {
    init { require(value >= 0) { "Age must be non-negative" } }
}

data class User(
    val id: UserId,
    val email: Email,
    val profileUrl: Url,
    val age: Age
)
```

### 8.2 Common Value Types to Create

| Concept | Type Name | Notes |
|---------|-----------|-------|
| Identifiers | `XxxId` | `TaskId`, `CategoryId` |
| Dates/Times | `DueDate`, `CreatedAt` | Wrap timestamps with validation |
| Task state | `TaskStatus` | Enum or sealed class |
| Titles | `TaskTitle` | Non-empty validation |
| Priority | `TaskPriority` | Enum (Low, Medium, High) |

### 8.3 Benefits

- **Type safety:** Compiler prevents mixing `UserId` with `BannerId`
- **Validation at construction:** Invalid data cannot exist
- **Self-documenting:** Code reads clearly
- **Refactoring safety:** Changes to one type don't affect others

---

## 9) SOLID Principles (MANDATORY)

### 9.1 Single Responsibility Principle (SRP)

- Each class/module has ONE reason to change
- Use cases do ONE thing
- Don't mix concerns (e.g., validation + persistence + formatting)

### 9.2 Open/Closed Principle (OCP)

- Open for extension, closed for modification
- Use interfaces and abstractions
- Add new behavior by adding new classes, not modifying existing ones

### 9.3 Liskov Substitution Principle (LSP)

- Subtypes must be substitutable for their base types
- Don't violate contracts in derived classes
- Sealed classes are preferred for type hierarchies

### 9.4 Interface Segregation Principle (ISP)

- Prefer small, focused interfaces
- Clients should not depend on methods they don't use
- Split fat interfaces into smaller ones

### 9.5 Dependency Inversion Principle (DIP)

- Depend on abstractions, not concretions
- High-level modules don't depend on low-level modules
- Both depend on abstractions (ports/interfaces)

---

## 10) DRY, KISS, and STUPID Avoidance (MANDATORY)

### 10.1 DRY (Don't Repeat Yourself)

- No duplicated logic
- Extract common behavior to shared functions/classes
- If you copy-paste, you're doing it wrong

**Refactoring guide:**
| Duplication Location | Refactor To |
|---------------------|-------------|
| Business rules | Domain service or entity method |
| Orchestration | Application use case |
| Mapping/conversion | Infrastructure mapper |
| UI logic | Shared component or hook |

### 10.2 KISS (Keep It Simple, Stupid)

- Prefer simple solutions over clever ones
- Avoid premature optimization
- Write code that's easy to read and understand

### 10.3 Avoid STUPID Code

| Letter | Anti-Pattern | Solution |
|--------|--------------|----------|
| **S** | Singleton abuse | Use DI |
| **T** | Tight coupling | Use interfaces/ports |
| **U** | Untestability | Design for testability |
| **P** | Premature optimization | Profile first |
| **I** | Indescriptive naming | Use clear, descriptive names |
| **D** | Duplication | Follow DRY |

---

## 11) Testing Requirements (MANDATORY)

### 11.1 Test-First Rule

- **Every feature MUST have tests**
- Write tests for each new feature
- All tests MUST pass after every modification
- Run tests before committing: `./gradlew :shared:allTests`

### 11.2 Test Pyramid

| Level | What to Test | Tool |
|-------|--------------|------|
| Unit | Domain logic, use cases, mappers | kotlin.test |
| Integration | Repository implementations, API clients | kotlin.test + mocks |
| E2E | Critical user flows | Platform-specific |

### 11.3 Mandatory Test Coverage

| Layer | Must Test |
|-------|-----------|
| Domain | All entity invariants, value object validation, domain service logic |
| Application | All use case branches, error handling paths |
| Infrastructure | All mappers, critical API responses |
| Presentation | Complex UI logic (if applicable) |

### 11.4 Test Naming Convention

```kotlin
@Test
fun `should return error when banner is expired`() { ... }

@Test
fun `should calculate pity correctly for 5-star`() { ... }
```

### 11.5 Testing Checklist

Before pushing code:
- [ ] All new features have tests
- [ ] All existing tests pass
- [ ] Edge cases are covered
- [ ] Error paths are tested

---

## Summary Checklist

Before writing code:
- [ ] Identify target layer(s) (models, repository, viewmodel, ui)
- [ ] Confirm dependencies are legal (see 3.2, 3.3)
- [ ] Model domain changes first (if any)
- [ ] Create value types for new concepts (no primitives!)
- [ ] Add repository methods if needed
- [ ] Update ViewModel logic
- [ ] Implement UI components/screens

Before finishing:
- [ ] Naming rules respected (see 2.1)
- [ ] No forbidden imports (see 3.4)
- [ ] Models remain framework-free (except @Serializable)
- [ ] No primitive obsession - all concepts have types
- [ ] SOLID principles followed
- [ ] No code duplication (DRY)
- [ ] All tests pass (`./gradlew :shared:allTests`)
- [ ] Commit follows Angular convention (see 12)

---

## 12) Git Commit Convention (MANDATORY)

### 12.1 Angular Commit Convention

All commits MUST follow the Angular commit convention:

```
<type>(<scope>): <short description>

[optional body]
[optional footer]
```

### 12.2 Commit Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code refactoring (no feature change) |
| `style` | Code style/formatting changes |
| `docs` | Documentation changes |
| `test` | Adding or updating tests |
| `chore` | Maintenance tasks, dependencies |
| `perf` | Performance improvements |
| `ci` | CI/CD configuration changes |
| `build` | Build system changes |

### 12.3 Scope Examples

| Scope | Description |
|-------|-------------|
| `theme` | Theme, colors, styling |
| `notifications` | Notification system |
| `permissions` | Permission handling |
| `kmp` | Kotlin Multiplatform setup |
| `ui` | General UI changes |
| `db` | Database/SQLDelight |
| `task` | Task-related features |
| `app` | Android app module |

### 12.4 Commit Examples

```bash
feat(task): add task deletion with swipe gesture
fix(notifications): correct alarm scheduling for overdue tasks
refactor(app): remove deprecated Fragment-based UI code
feat(theme): implement glassmorphism styling and animation transitions
feat(kmp): migrate to Kotlin Multiplatform with Compose UI
```

---

## 13) UI/UX Design Standards (MANDATORY)

### 13.1 Theme: Soft Purple/Lavender

The application uses a soft purple/lavender color palette:

**Light Theme:**
- Primary: `#7C5CBF` (Soft purple)
- Primary Container: `#EDE7F6` (Very light lavender)
- Secondary: `#B39DDB` (Light lavender accent)
- Background: `#FDFBFF` (Warm white)

**Dark Theme:**
- Primary: `#D0BCFF` (Light lavender)
- Primary Container: `#4F378B` (Deep purple)
- Secondary: `#CCC2DC` (Muted lavender)
- Background: `#1C1B1F` (Neutral dark)

### 13.2 Glassmorphism Style

All card-like components should use glassmorphism styling:

```kotlin
// Use GlassCard or GlassSurface for card components
GlassCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = { /* action */ }
) {
    // Content
}

// Use glassBackground modifier for custom glass effects
Modifier.glassBackground(
    isDarkTheme = isSystemInDarkTheme(),
    shape = MaterialTheme.shapes.medium
)
```

**Configuration (GlassConfig):**
- Light mode alpha: `0.7f`
- Dark mode alpha: `0.5f`
- Border alpha: `0.1f`
- Default corner radius: `16.dp`

### 13.3 Shape System

| Size | Radius | Usage |
|------|--------|-------|
| extraSmall | 8.dp | Chips, small buttons |
| small | 12.dp | Input fields |
| medium | 16.dp | Cards, dialogs |
| large | 24.dp | Bottom sheets |
| extraLarge | 32.dp | Full-width containers |

### 13.4 Animation Guidelines

- Use smooth transitions for state changes
- Implement subtle entrance animations for lists
- Add haptic feedback for important actions
- See `Animations.kt` for shared animation specs

---

## 14) Permission Handling (MANDATORY)

### 14.1 Android Permissions

The app requires the following permissions on Android:
- `POST_NOTIFICATIONS` (Android 13+) - for task reminders
- `SCHEDULE_EXACT_ALARM` (Android 12+) - for precise scheduling

### 14.2 Permission State Model

```kotlin
sealed class PermissionState {
    data object NotRequested : PermissionState()
    data object Granted : PermissionState()
    data object Denied : PermissionState()
    data object PermanentlyDenied : PermissionState()
    data object ShouldShowRationale : PermissionState()
}
```

### 14.3 Permission UI Pattern

1. **Rationale Dialog**: Explain why permission is needed BEFORE requesting
2. **Request Permission**: Use Android's permission request flow
3. **Denied Banner**: Show dismissible banner if denied
4. **Settings Dialog**: Guide user to settings if permanently denied

### 14.4 Permission UI Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `PermissionBanner` | `ui/components/` | Shows when permission denied |
| `PermissionDialog` | `ui/components/` | Rationale and settings dialogs |
| `NotificationPermissionHandler` | `permissions/` | Android permission logic |
| `PermissionState` | `permissions/` | State management |

### 14.5 Permission Flow

```
App Start
    ↓
Check Permission Status
    ↓
┌─────────────────────────────────────┐
│ If NotRequested or ShouldShowRationale │
│     → Show Rationale Dialog          │
│     → Request Permission             │
├─────────────────────────────────────┤
│ If Denied                           │
│     → Show Denial Banner            │
│     → Allow re-request              │
├─────────────────────────────────────┤
│ If PermanentlyDenied                │
│     → Show Settings Dialog          │
│     → Guide to app settings         │
├─────────────────────────────────────┤
│ If Granted                          │
│     → Proceed normally              │
└─────────────────────────────────────┘
```

---

**References:**
- DDD Standards: `agent-os/standards/ddd-standards.md`
- Agent Instructions: `AGENTS.md`
