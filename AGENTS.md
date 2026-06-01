# AGENTS.md

Compact orientation for OpenCode sessions. Read [agent-os/standards/rules.md](file:///home/rezzonicop/dev/stodolist/agent-os/standards/rules.md) and [agent-os/standards/ddd-standards.md](file:///home/rezzonicop/dev/stodolist/agent-os/standards/ddd-standards.md) for the full project conventions (naming, DDD layering, commit format, UI/theme rules). This file only flags what those documents miss or get wrong.

## What this repo actually is

- **Kotlin Multiplatform** app (Android + iOS) with **Compose Multiplatform** UI. The [README.md](file:///home/rezzonicop/dev/stodolist/README.md) is stale and still describes it as Android-only / plain SQLite — do not trust it.
- Two Gradle modules only: `:app` (Android entrypoint) and `:shared` (KMP library). See [settings.gradle](file:///home/rezzonicop/dev/stodolist/settings.gradle). Root project name is `Stodolist` (capital S) — `./gradlew` task paths are case-sensitive.
- `iosApp/` is the SwiftUI host; it is **not** a Gradle module. It consumes the framework produced by `:shared`.

## Module layout (the real one)

- [shared/src/commonMain/kotlin/fr/unilim/stodolist/](file:///home/rezzonicop/dev/stodolist/shared/src/commonMain/kotlin/fr/unilim/stodolist/) — `models/`, `repository/`, `viewmodel/`, `database/`, `ui/{screens,components,theme}`, `Platform.kt`. **All shared Compose UI lives here**, not in `:app`.
- [shared/src/commonMain/sqldelight/fr/unilim/stodolist/database/](file:///home/rezzonicop/dev/stodolist/shared/src/commonMain/sqldelight/fr/unilim/stodolist/database/) — `.sq` files. Generated package is `fr.unilim.stodolist.database`, database class `StodolistDatabase` (configured in [shared/build.gradle.kts](file:///home/rezzonicop/dev/stodolist/shared/build.gradle.kts)).
- `shared/src/{androidMain,iosMain}/kotlin/...` — `expect/actual` for `DatabaseDriverFactory` and `Platform`.
- [app/src/main/java/fr/unilim/stodolist/](file:///home/rezzonicop/dev/stodolist/app/src/main/java/fr/unilim/stodolist/) — `ui/MainActivity` + `PermissionScreen`, `permissions/`, `notifications/`. Android-only concerns live here, never in `shared/`.

The DDD standard talks about `domain/application/infrastructure/presentation` packages — the actual code uses `models/repository/viewmodel/ui/` (see rules.md §3.1). Follow the real layout; the layering intent still applies (`models/` must stay framework-free).

## Versions: trust the build files, not the docs

`agent-os/standards/rules.md` §1 lists outdated versions. Authoritative versions live in [build.gradle](file:///home/rezzonicop/dev/stodolist/build.gradle), [app/build.gradle](file:///home/rezzonicop/dev/stodolist/app/build.gradle), [shared/build.gradle.kts](file:///home/rezzonicop/dev/stodolist/shared/build.gradle.kts):

- Kotlin **2.1.0**, AGP **8.13.2**, Compose Multiplatform **1.7.1**, Compose Compiler via `kotlin.plugin.compose`.
- SQLDelight **2.0.2**, Ktor **3.0.1**, kotlinx.serialization **1.7.3**, coroutines **1.9.0**.
- `compileSdk` / `targetSdk` = **35**, `minSdk` **24**, JVM target **1.8**.
- `:app` uses Groovy DSL (`build.gradle`); `:shared` uses Kotlin DSL (`build.gradle.kts`). Don't unify without being asked.

## Commands that actually work here

```bash
./gradlew :app:assembleDebug                 # Android APK
./gradlew :shared:build                      # KMP shared (all targets the host can build)
./gradlew :shared:linkDebugFrameworkIosArm64 # iOS framework (requires macOS host)
./gradlew :shared:generateCommonMainStodolistDatabaseInterface  # regenerate SQLDelight
./gradlew :shared:verifySqlDelightMigration

./gradlew :shared:allTests                   # all KMP test targets
./gradlew :app:testDebugUnitTest             # Android JVM unit tests
./gradlew :app:connectedAndroidTest          # instrumented (needs device/emulator)
```

iOS targets (`iosX64`, `iosArm64`, `iosSimulatorArm64`) are declared but **only build on macOS**. On Linux, expect `:shared:build` to skip/fail iOS link tasks — that's not a regression.

## Testing gotchas

- Standards mandate shared tests in `shared/src/commonTest/kotlin/` — **the directory does not exist yet**. Create it (and matching package) when adding the first shared test; don't put logic tests under `:app`.
- The only existing tests are placeholder Android stubs: [ExampleUnitTest.kt](file:///home/rezzonicop/dev/stodolist/app/src/test/java/fr/unilim/stodolist/ExampleUnitTest.kt) and [ExampleInstrumentedTest.kt](file:///home/rezzonicop/dev/stodolist/app/src/androidTest/java/fr/unilim/stodolist/ExampleInstrumentedTest.kt). No CI is configured (no `.github/`).
- Use `kotlin.test` (already wired into `commonTest`). The Android module uses JUnit 4.

## Conventions worth remembering

- Commits **must** follow Angular convention (`feat(scope): ...`) — see rules.md §12.
- No primitive obsession (rules.md §8): wrap IDs/dates/titles in `@JvmInline value class` — current `Task`/`Category` models in [models/](file:///home/rezzonicop/dev/stodolist/shared/src/commonMain/kotlin/fr/unilim/stodolist/models/) predate that rule and still use raw `Long`/`String`. If you touch them, follow the rule going forward; don't silently refactor unrelated call sites.
- UI must use the glassmorphism theme helpers in [ui/theme/](file:///home/rezzonicop/dev/stodolist/shared/src/commonMain/kotlin/fr/unilim/stodolist/ui/theme/) (`GlassCard`, `glassBackground`, soft-purple palette). Don't introduce a second theme.
- Repository pattern: interface (`TaskRepository.kt`) and impl (`TaskRepositoryImpl.kt`) live **in the same `repository/` package** (not split into `domain`/`infrastructure`).
- Notifications use `AlarmManager` (`POST_NOTIFICATIONS` + `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` declared in [AndroidManifest.xml](file:///home/rezzonicop/dev/stodolist/app/src/main/AndroidManifest.xml)). Permission flow is enforced through [permissions/](file:///home/rezzonicop/dev/stodolist/app/src/main/java/fr/unilim/stodolist/permissions/) — follow the existing `PermissionState` sealed class.

## Other agent metadata

- `.junie/memory/` exists but every file is empty (placeholders); ignore unless asked.
- No `opencode.json` and no `.opencode/` — this `AGENTS.md` is the only OpenCode-facing config.
