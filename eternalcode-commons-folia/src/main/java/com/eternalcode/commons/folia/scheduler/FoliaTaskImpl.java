package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.Task;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class FoliaTaskImpl implements Task {

    private final ScheduledTask rootTask;
    private final boolean async;

    public FoliaTaskImpl(ScheduledTask rootTask) {
        this.rootTask = rootTask;
        this.async = false;
    }

    public FoliaTaskImpl(ScheduledTask rootTask, boolean async) {
        this.rootTask = rootTask;
        this.async = async;
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
        return async;
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
