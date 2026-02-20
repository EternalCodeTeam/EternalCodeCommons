package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.loom.LoomScheduler;
import com.eternalcode.commons.scheduler.loom.LoomSchedulerImpl;
import com.eternalcode.commons.scheduler.loom.VirtualThreadExecutor;
import java.time.Duration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia-aware LoomScheduler.
 * <p>
 * Folia has no single "main thread" - different regions have different threads.
 * Use forEntity() for player/entity ops, forLocation() for block ops.
 */
public final class FoliaLoomScheduler {

    private final Plugin plugin;
    private final VirtualThreadExecutor vtExecutor;
    private final FoliaGlobalDispatcher globalDispatcher;
    private final LoomSchedulerImpl globalScheduler;

    private FoliaLoomScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.vtExecutor = new VirtualThreadExecutor();
        this.globalDispatcher = new FoliaGlobalDispatcher(plugin);
        this.globalScheduler = new LoomSchedulerImpl(this.globalDispatcher, this.vtExecutor);
    }

    public static FoliaLoomScheduler create(Plugin plugin) {
        return new FoliaLoomScheduler(plugin);
    }

    /**
     * Global context - for broadcasts, plugin state, console commands.
     * Don't use for entity/block operations!
     */
    public LoomScheduler forGlobal() {
        return this.globalScheduler;
    }

    /**
     * Entity context - sync tasks run on entity's region thread.
     * Use for: player teleport, inventory, entity modification.
     * <p>
     * Note: creates new dispatcher per call. For hot paths, cache the scheduler.
     */
    public LoomScheduler forEntity(Entity entity) {
        return new LoomSchedulerImpl(new FoliaEntityDispatcher(this.plugin, entity), this.vtExecutor);
    }

    /**
     * Location context - sync tasks run on location's region thread.
     * Use for: block get/set, entity spawning at location.
     */
    public LoomScheduler forLocation(Location location) {
        return new LoomSchedulerImpl(new FoliaRegionDispatcher(this.plugin, location), this.vtExecutor);
    }

    /**
     * Chunk context - sync tasks run on chunk's region thread.
     */
    public LoomScheduler forChunk(World world, int chunkX, int chunkZ) {
        return new LoomSchedulerImpl(new FoliaRegionDispatcher(this.plugin, world, chunkX, chunkZ), this.vtExecutor);
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public boolean shutdown(Duration timeout) {
        this.globalDispatcher.shutdown();
        return this.vtExecutor.shutdown(timeout);
    }

    public void shutdownNow() {
        this.globalDispatcher.shutdown();
        this.vtExecutor.shutdownNow();
    }
}
