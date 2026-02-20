package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.loom.MainThreadDispatcher;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 * Folia RegionScheduler-based dispatcher.
 * Tasks run on the region's thread that owns the specified chunk.
 * <p>
 * Use for: block operations, entity spawning at location.
 */
public final class FoliaRegionDispatcher implements MainThreadDispatcher {

    private final Plugin plugin;
    private final World world;
    private final int chunkX;
    private final int chunkZ;
    private final RegionScheduler regionScheduler;

    public FoliaRegionDispatcher(Plugin plugin, Location location) {
        this(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public FoliaRegionDispatcher(Plugin plugin, World world, int chunkX, int chunkZ) {
        this.plugin = plugin;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.regionScheduler = plugin.getServer().getRegionScheduler();
    }

    @Override
    public void dispatch(Runnable task) {
        this.regionScheduler.run(
            this.plugin, this.world, this.chunkX, this.chunkZ, t -> {
                try {
                    task.run();
                }
                catch (Throwable ex) {
                    this.plugin.getLogger().severe("Exception in region task: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
    }

    @Override
    public boolean isMainThread() {
        return this.plugin.getServer().isOwnedByCurrentRegion(this.world, this.chunkX, this.chunkZ);
    }

    @Override
    public void dispatchLater(Runnable task, long ticks) {
        this.regionScheduler.runDelayed(this.plugin, this.world, this.chunkX, this.chunkZ, t -> task.run(), ticks);
    }

    @Override
    public Cancellable dispatchTimer(Runnable task, long delay, long period) {
        ScheduledTask st = this.regionScheduler.runAtFixedRate(
            this.plugin, this.world, this.chunkX, this.chunkZ, t -> task.run(), delay, period);
        return st::cancel;
    }
}
