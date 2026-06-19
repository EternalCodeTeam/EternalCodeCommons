<div align="center">

# EternalCode Commons

A collection of utilities and abstractions for Minecraft plugin development.

[![Gradle](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/built-with/gradle_vector.svg)](https://gradle.org/)
[![Chat on Discord](https://raw.githubusercontent.com/vLuckyyy/badges/main//chat-with-us-on-discord.svg)](https://discord.com/invite/FQ7jmGBd6c)
[![Supports Paper](https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/supported/paper_vector.svg)](https://papermc.io)

</div>

## About

EternalCode Commons is a modular utility library for Minecraft plugin development. It provides platform-agnostic
abstractions for scheduling, time parsing, cooldowns, and more — with ready-made implementations for Bukkit, Paper, and
Folia.

## Modules

| Module                            | Description                                                                        | Java | Platform     |
|-----------------------------------|------------------------------------------------------------------------------------|------|--------------|
| `eternalcode-commons-shared`      | Core utilities — scheduling API, time parsing, cooldowns, progress bars, lazy init | 17   | Any          |
| `eternalcode-commons-bukkit`      | Bukkit/Spigot/Paper scheduler implementation, item utils, position adapters        | 17   | Bukkit/Paper |
| `eternalcode-commons-folia`       | Folia scheduler implementation                                                     | 17   | Folia        |
| `eternalcode-commons-adventure`   | Kyori Adventure helpers — legacy color conversion, auto-clickable URLs             | 21   | Any          |
| `eternalcode-commons-loom`        | Virtual thread scheduler (platform-agnostic)                                       | 21   | Any          |
| `eternalcode-commons-bukkit-loom` | Virtual thread scheduler for Bukkit/Paper                                          | 21   | Bukkit/Paper |
| `eternalcode-commons-folia-loom`  | Virtual thread scheduler for Folia (region-aware)                                  | 21   | Folia        |
| `eternalcode-commons-updater`     | Modrinth update checker                                                            | 17   | Any          |

## Getting Started

Add the EternalCode repository and the modules you need to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.eternalcode.pl/releases")
}

dependencies {
    // Core utilities (scheduling API, time parsing, cooldowns, etc.)
    implementation("com.eternalcode:eternalcode-commons-shared:1.4.2")

    // Pick your platform's scheduler implementation:
    implementation("com.eternalcode:eternalcode-commons-bukkit:1.4.2")   // Bukkit/Paper
    // implementation("com.eternalcode:eternalcode-commons-folia:1.4.2") // Folia

    // Optional: virtual thread scheduler (Java 21+)
    implementation("com.eternalcode:eternalcode-commons-bukkit-loom:1.4.2")   // Bukkit/Paper
    // implementation("com.eternalcode:eternalcode-commons-folia-loom:1.4.2") // Folia

    // Optional: Adventure text utilities
    implementation("com.eternalcode:eternalcode-commons-adventure:1.4.2")

    // Optional: Modrinth update checker
    implementation("com.eternalcode:eternalcode-commons-updater:1.4.2")
}
```

## Quick Examples

### Scheduler (Bukkit/Paper)

The `MinecraftScheduler` interface wraps Bukkit's scheduler with `Duration`-based API and async support:

```java
public class MyPlugin extends JavaPlugin {

    private MinecraftScheduler scheduler;

    @Override
    public void onEnable() {
        this.scheduler = new BukkitSchedulerImpl(this);
    }

    public void example(Player player) {
        // Run once, async
        scheduler.runAsync(() -> {
            String data = database.load(player.getUniqueId());

            // Switch back to main thread
            scheduler.run(() -> player.sendMessage(data));
        });

        // Repeating task with delay
        scheduler.timer(() -> player.sendMessage("tick"), Duration.ofSeconds(1), Duration.ofSeconds(5));
    }
}
```

### Virtual Thread Scheduler (Java 21+, Bukkit/Paper)

`BukkitLoomScheduler` provides a fluent async pipeline using Java 21 virtual threads:

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
        scheduler.supplyAsync(() -> database.load(player.getUniqueId()))  // virtual thread
            .thenApply(data -> transform(data))                           // virtual thread
            .thenAcceptSync(result -> player.sendMessage(result))         // main thread
            .exceptionally(e -> getLogger().severe(e.getMessage()));
    }
}
```

> **Rule:** `Async` / `thenApply` → virtual thread (never call Bukkit API here). `Sync` / `thenAcceptSync` → main
> thread.

### Virtual Thread Scheduler (Java 21+, Folia)

Folia has no single main thread — use the correct context for each operation:

```java
FoliaLoomScheduler scheduler = FoliaLoomScheduler.create(plugin);

// Player operations
scheduler.

forEntity(player)
    .

supplyAsync(() ->database.

load(player.getUniqueId()))
    .

thenAcceptSync(data ->player.

sendMessage(data));  // player's region thread

// Block operations
    scheduler.

forLocation(location)
    .

runSync(() ->location.

getBlock().

setType(Material.STONE));  // location's region thread

// Global operations (broadcast, plugin state)
    scheduler.

forGlobal()
    .

runSync(() ->Bukkit.

broadcast(Component.text("Hello!")));
```

### Duration Parsing

Parse human-readable durations from user input:

```java
// Pre-built parsers
Duration duration = DurationParser.TIME_UNITS.parse("1h30m");    // 1 hour 30 minutes
Duration duration2 = DurationParser.DATE_TIME_UNITS.parse("2d12h"); // 2 days 12 hours

// Or build a custom one
TemporalAmountParser<Duration> parser = new DurationParser()
    .withUnit("s", ChronoUnit.SECONDS)
    .withUnit("m", ChronoUnit.MINUTES);

Duration result = parser.parse("5m30s"); // 5 minutes 30 seconds
```

### Cooldowns (Delay)

Track per-player (or any key) cooldowns backed by Caffeine:

```java
Delay<UUID> cooldown = Delay.withDefault(() -> Duration.ofSeconds(10));

public void onCommand(Player player) {
    if (cooldown.hasDelay(player.getUniqueId())) {
        Duration remaining = cooldown.getRemaining(player.getUniqueId());
        player.sendMessage("Wait " + remaining.toSeconds() + "s");
        return;
    }

    cooldown.markDelay(player.getUniqueId());
    // ... execute command
}
```

### Progress Bar

Render a customizable text progress bar:

```java
ProgressBar bar = ProgressBar.builder()
    .length(20)
    .filledChar("█")
    .emptyChar("░")
    .filledColor("§a")
    .emptyColor("§7")
    .brackets("§8[", "§8]")
    .build();

String rendered = bar.render(current, max);
// e.g. §8[§a██████████░░░░░░░░░░§8]
```

### Update Checker (Modrinth)

Check for new plugin versions on Modrinth:

```java
UpdateChecker checker = new ModrinthUpdateChecker();
UpdateResult result = checker.check("your-modrinth-project-id", new Version(getDescription().getVersion()));

if(result.

isUpdateAvailable()){

getLogger().

info("New version available: "+result.latestVersion());

getLogger().

info("Download: "+result.downloadUrl());
    }
```

### Adventure Utilities

Convert legacy `&`-color codes to Adventure components, and make URLs clickable:

```java
// Parse &-color codes to Adventure component
Component component = AdventureUtil.component("&aHello &bWorld!");

// Auto-linkify URLs in a component
Component withLinks = new AdventureUrlPostProcessor().apply(component);

// Convert §-color codes (e.g. from config) to &-codes before parsing
String normalized = new AdventureLegacyColorPreProcessor().apply(rawString);
```

## 📄 License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- [Discord Community](https://discord.gg/FQ7jmGBd6c)
- [EternalCode Website](https://eternalcode.pl/)
- [Maven Repository](https://repo.eternalcode.pl)
