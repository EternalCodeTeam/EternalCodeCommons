# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run tests for a single module
./gradlew :eternalcode-commons-shared:test
./gradlew :eternalcode-commons-bukkit:test
./gradlew :eternalcode-commons-loom:test

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Publish to EternalCode Maven (requires ETERNAL_CODE_MAVEN_USERNAME and ETERNAL_CODE_MAVEN_PASSWORD env vars)
./gradlew publish
```

Test sources live in `test/` (not `src/test/java/`) in each module — this is a non-standard layout set by `commons-java-unit-test.gradle.kts`.

## Module Architecture

This is a multi-module Gradle library providing utilities for Minecraft plugin development (Bukkit/Spigot/Paper and Folia). All modules publish under `com.eternalcode` group, version defined in `buildSrc/src/main/kotlin/commons-publish.gradle.kts`.

### Module Dependency Tree

```
eternalcode-commons-shared       (base utilities, no MC dependency)
├── eternalcode-commons-bukkit   (Spigot/Paper API, Java 17)
│   └── eternalcode-commons-folia  (Folia API, extends bukkit)
└── eternalcode-commons-adventure  (Kyori Adventure text, Java 21)

eternalcode-commons-loom         (virtual thread scheduler, platform-agnostic, Java 21)
├── eternalcode-commons-bukkit-loom   (Bukkit/Paper + loom integration)
└── eternalcode-commons-folia-loom    (Folia + loom, region-aware)

eternalcode-commons-updater      (Modrinth update checker, uses Gson)
eternalcode-commons-updater-example  (runnable example, not published as library)
```

### Convention Plugins (buildSrc)

All modules compose from these convention plugins:
- `commons-java` — Java 8, non-standard src layout (`src/` not `src/main/java/`)
- `commons-java-17` — Java 17, standard `src/main/java/` layout
- `commons-java-21` — Java 21, standard `src/main/java/` layout
- `commons-java-unit-test` — Adds JUnit 5 + Mockito + AssertJ + Awaitility; sets test source dir to `test/`
- `commons-repositories` — Adds Paper and Spigot Maven repos
- `commons-publish` — Maven publishing to `repo.eternalcode.pl`

### Key Abstractions

**Scheduler interface** (`eternalcode-commons-shared`) — platform-agnostic scheduling API with sync/async variants returning `Task`. Implementations:
- `BukkitSchedulerImpl` — wraps Bukkit scheduler
- `FoliaSchedulerImpl` — wraps Folia region scheduler
- `LoomSchedulerImpl` — virtual threads (requires `MainThreadDispatcher`)

**LoomScheduler** (`eternalcode-commons-loom`) — fluent future-chaining API using Java 21 virtual threads. `runAsync`/`supplyAsync`/`thenApply` run on virtual threads; `runSync`/`thenAcceptSync`/`thenApplySync` dispatch to main thread via injected `MainThreadDispatcher`. Never call `join()` or `get()` on these futures.

**Folia loom contexts** — Folia has no single main thread. Use `FoliaLoomScheduler.forEntity(player)`, `.forLocation(loc)`, or `.forGlobal()` to get the correct regional dispatcher before chaining.

### Java Version Split

- Most modules: Java 17
- `eternalcode-commons-adventure`, `eternalcode-commons-loom`, `eternalcode-commons-bukkit-loom`, `eternalcode-commons-folia-loom`: Java 21 (virtual threads require it)
