# eternalcode-commons-folia-loom

Virtual Thread scheduler for Folia. Region-aware dispatching.

## Dependency

```kotlin
// Gradle
implementation("com.eternalcode:eternalcode-commons-folia-loom:1.3.1")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>com.eternalcode</groupId>
    <artifactId>eternalcode-commons-folia-loom</artifactId>
    <version>1.3.1</version>
</dependency>
```

Repository: `https://repo.eternalcode.pl/releases`

## Requirements

- Java 21+
- Folia 1.19+

## Why different from Bukkit?

Folia has no single main thread. Different regions run on different threads.
You must use the correct context for each operation.

## Usage

```java
public class MyPlugin extends JavaPlugin {
    private FoliaLoomScheduler scheduler;

    @Override
    public void onEnable() {
        this.scheduler = FoliaLoomScheduler.create(this);
    }

    // Player operations - use forEntity()
    public void teleport(Player player, Location dest) {
        scheduler.forEntity(player)
            .supplyAsync(() -> database.canTeleport(player.getUniqueId()))
            .thenAcceptSync(canTp -> {
                if (canTp)
                    player.teleport(dest);  // safe - player's region thread
            });
    }

    // Block operations - use forLocation()
    public void setBlock(Location loc, Material mat) {
        scheduler.forLocation(loc)
            .runSync(() -> loc.getBlock().setType(mat));  // safe - location's region thread
    }

    // Global operations - use forGlobal()
    public void broadcast(String msg) {
        scheduler.forGlobal()
            .runSync(() -> Bukkit.broadcast(Component.text(msg)));
    }
}
```

## Which context?

| Operation                   | Method              |
|-----------------------------|---------------------|
| Player teleport, inventory  | `forEntity(player)` |
| Block get/set, entity spawn | `forLocation(loc)`  |
| Broadcast, plugin state     | `forGlobal()`       |

## Rules

- Each `forX()` dispatches sync tasks to that context's thread
- Don't mix contexts (e.g. don't teleport playerB from forEntity(playerA))
- Async methods always run on Virtual Threads regardless of context
