package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.loom.MainThreadDispatcher;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.plugin.Plugin;

/**
 * Folia GlobalRegionScheduler-based dispatcher.
 * For plugin-wide operations, broadcasts, etc.
 */
public final class FoliaGlobalDispatcher implements MainThreadDispatcher {

    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Plugin plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final ScheduledTask tickTask;

    public FoliaGlobalDispatcher(Plugin plugin) {
        this.plugin = plugin;
        this.globalScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.tickTask = this.globalScheduler.runAtFixedRate(this.plugin, t -> drainQueue(), 1L, 1L);
    }

    private void drainQueue() {
        Runnable task;
        while ((task = this.queue.poll()) != null) {
            try {
                task.run();
            }
            catch (Throwable t) {
                this.plugin.getLogger().severe("Exception in global task: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    @Override
    public void dispatch(Runnable task) {
        this.queue.offer(task);
    }

    @Override
    public boolean isMainThread() {
        return this.plugin.getServer().isGlobalTickThread();
    }

    @Override
    public void dispatchLater(Runnable task, long ticks) {
        this.globalScheduler.runDelayed(this.plugin, t -> task.run(), ticks);
    }

    @Override
    public Cancellable dispatchTimer(Runnable task, long delay, long period) {
        ScheduledTask st = this.globalScheduler.runAtFixedRate(this.plugin, t -> task.run(), delay, period);
        return st::cancel;
    }

    public void shutdown() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
        }
        Runnable task;
        while ((task = this.queue.poll()) != null) {
            try {
                task.run();
            }
            catch (Throwable ignored) {
            }
        }
    }
}
