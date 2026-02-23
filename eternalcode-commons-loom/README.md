# eternalcode-commons-loom

Virtual Thread scheduler for Java 21+. Core module - platform-agnostic.

## Dependency

```kotlin
// Gradle
implementation("com.eternalcode:eternalcode-commons-loom:1.3.1")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>com.eternalcode</groupId>
    <artifactId>eternalcode-commons-loom</artifactId>
    <version>1.3.1</version>
</dependency>
```

Repository: `https://repo.eternalcode.pl/releases`

## Usage

```java
// Create with custom dispatcher
LoomScheduler scheduler = new LoomSchedulerImpl(myDispatcher);

// Async (VT) -> transform (VT) -> sync (main thread)
scheduler.supplyAsync(() -> database.load(id))
    .thenApply(data -> transform(data))
    .thenAcceptSync(result -> player.sendMessage(result))
    .exceptionally(e -> logger.severe(e.getMessage()));
```

## Rules

- `runAsync`, `supplyAsync`, `thenApply` → Virtual Thread
- `runSync`, `thenAcceptSync`, `thenApplySync` → Main Thread
- Never call Bukkit API from async
- Never use `join()` or `get()` on futures

## See also

- [bukkit-loom](../eternalcode-commons-bukkit-loom) - Bukkit integration
- [folia-loom](../eternalcode-commons-folia-loom) - Folia integration
