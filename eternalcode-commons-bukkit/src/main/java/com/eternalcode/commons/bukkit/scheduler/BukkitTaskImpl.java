package com.eternalcode.commons.bukkit.scheduler;

import com.eternalcode.commons.scheduler.Task;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

class BukkitTaskImpl implements Task {

    private final BukkitTask rootTask;

    private boolean isRepeating;

    BukkitTaskImpl(BukkitTask rootTask) {
        this.rootTask = rootTask;
        this.isRepeating = false;
    }

    public BukkitTaskImpl(BukkitTask rootTask, boolean isRepeating) {
        this.rootTask = rootTask;
        this.isRepeating = isRepeating;
    }

    @Override
    public void cancel() {
        this.rootTask.cancel();
    }

    @Override
    public boolean isCanceled() {
        return this.rootTask.isCancelled();
    }

    @Override
    public boolean isAsync() {
        return !this.rootTask.isSync();
    }

    @Override
    public Plugin getPlugin() {
        return this.rootTask.getOwner();
    }

    @Override
    public boolean isRunning() {
        // There's no other way,
        // there's no other way
        // All that you can do is [...]]
        // https://www.youtube.com/watch?v=LJzCYSdrHMI
        return Bukkit.getServer().getScheduler().isCurrentlyRunning(this.rootTask.getTaskId());
    }

    @Override
    public boolean isRepeating() {
        return this.isRepeating;
    }
}
