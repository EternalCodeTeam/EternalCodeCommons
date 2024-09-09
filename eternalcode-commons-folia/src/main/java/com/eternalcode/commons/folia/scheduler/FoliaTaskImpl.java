package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.Task;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public class FoliaTaskImpl implements Task {

    private final ScheduledTask rootTask;

    public FoliaTaskImpl(ScheduledTask rootTask) {
        this.rootTask = rootTask;
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
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.rootTask.getOwningPlugin();
    }

    @Override
    public boolean isRunning() {
        ScheduledTask.ExecutionState state = this.rootTask.getExecutionState();

        return state == ScheduledTask.ExecutionState.RUNNING || state == ScheduledTask.ExecutionState.CANCELLED_RUNNING;
    }

    @Override
    public boolean isRepeating() {
        return this.rootTask.isRepeatingTask();
    }
}
