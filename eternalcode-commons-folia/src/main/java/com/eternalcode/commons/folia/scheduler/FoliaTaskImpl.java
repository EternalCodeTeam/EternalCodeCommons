package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.Task;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

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
}
