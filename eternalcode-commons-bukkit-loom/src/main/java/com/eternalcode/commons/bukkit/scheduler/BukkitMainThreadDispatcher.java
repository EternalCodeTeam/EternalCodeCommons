package com.eternalcode.commons.bukkit.scheduler;

import com.eternalcode.commons.scheduler.loom.MainThreadDispatcher;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Bukkit implementation - queues tasks from VT to main thread.
 */
public final class BukkitMainThreadDispatcher implements MainThreadDispatcher {

    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Plugin plugin;
    private final BukkitScheduler bukkitScheduler;
    private BukkitTask tickTask;

    public BukkitMainThreadDispatcher(Plugin plugin) {
        this.plugin = plugin;
        this.bukkitScheduler = plugin.getServer().getScheduler();
        this.tickTask = this.bukkitScheduler.runTaskTimer(this.plugin, this::drainQueue, 1L, 1L);
    }

    private void drainQueue() {
        Runnable task;
        while ((task = this.queue.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                this.plugin.getLogger().severe("Exception in sync task: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    @Override
    public void dispatch(Runnable task) {
        if (isMainThread()) {
            try {
                task.run();
            } catch (Throwable t) {
                this.plugin.getLogger().severe("Exception in sync task: " + t.getMessage());
                t.printStackTrace();
            }
            return;
        }
        this.queue.offer(task);
    }

    @Override
    public boolean isMainThread() {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public void dispatchLater(Runnable task, long ticks) {
        this.bukkitScheduler.runTaskLater(this.plugin, task, ticks);
    }

    @Override
    public Cancellable dispatchTimer(Runnable task, long delay, long period) {
        BukkitTask t = this.bukkitScheduler.runTaskTimer(this.plugin, task, delay, period);
        return t::cancel;
    }

    public void shutdown() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
        }
        Runnable task;
        while ((task = this.queue.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                this.plugin.getLogger().severe("Exception in shutdown task: " + t.getMessage());
            }
        }
    }

    public int getPendingCount() {
        return this.queue.size();
    }
}
