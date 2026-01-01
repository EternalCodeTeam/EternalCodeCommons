package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.loom.MainThreadDispatcher;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia EntityScheduler-based dispatcher.
 * Tasks run on the entity's owning region thread.
 * <p>
 * Use for: player teleport, entity modification, inventory.
 * Don't use for: block operations (use FoliaRegionDispatcher).
 */
public final class FoliaEntityDispatcher implements MainThreadDispatcher {

    private final Plugin plugin;
    private final Entity entity;
    private final EntityScheduler entityScheduler;

    public FoliaEntityDispatcher(Plugin plugin, Entity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.entityScheduler = entity.getScheduler();
    }

    @Override
    public void dispatch(Runnable task) {
        ScheduledTask st = this.entityScheduler.run(
            this.plugin, t -> {
                try {
                    task.run();
                }
                catch (Throwable ex) {
                    this.plugin.getLogger().severe("Exception in entity task: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }, null); // null = entity retired, task won't run

        if (st == null) {
            // entity was retired (removed from world) - task didn't schedule
            // this is expected in some cases, don't spam logs
        }
    }

    @Override
    public boolean isMainThread() {
        return this.plugin.getServer().isOwnedByCurrentRegion(this.entity);
    }

    @Override
    public void dispatchLater(Runnable task, long ticks) {
        this.entityScheduler.runDelayed(this.plugin, t -> task.run(), null, ticks);
    }

    @Override
    public Cancellable dispatchTimer(Runnable task, long delay, long period) {
        ScheduledTask st = this.entityScheduler.runAtFixedRate(this.plugin, t -> task.run(), null, delay, period);
        if (st == null) {
            return () -> {
            };
        }
        return st::cancel;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
