# eternalcode-commons-bukkit-loom

Virtual Thread scheduler for Bukkit/Paper plugins.

## Dependency

```kotlin
// Gradle
implementation("com.eternalcode:eternalcode-commons-bukkit-loom:1.3.1")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>com.eternalcode</groupId>
    <artifactId>eternalcode-commons-bukkit-loom</artifactId>
    <version>1.3.1</version>
</dependency>
```

Repository: `https://repo.eternalcode.pl/releases`

## Requirements

- Java 21+
- Paper/Spigot 1.19+
- **Not for Folia** - use `folia-loom` instead

## Usage

```java
public class MyPlugin extends JavaPlugin {
    private BukkitLoomScheduler scheduler;

    @Override
    public void onEnable() {
        this.scheduler = BukkitLoomScheduler.create(this);
    }

    @Override
    public void onDisable() {
        this.scheduler.shutdown(Duration.ofSeconds(5));
    }

    public void loadData(Player player) {
        scheduler.supplyAsync(() -> database.load(player.getUniqueId()))
            .thenAcceptSync(data -> {
                // safe - main thread
                player.sendMessage("Loaded: " + data);
            })
            .exceptionally(e -> getLogger().severe(e.getMessage()));
    }
}
```

## Rules

- `Async` methods → Virtual Thread (no Bukkit API!)
- `Sync` methods → Main Thread (Bukkit API safe)
- Use `.thenAcceptSync()` to switch from async to sync
