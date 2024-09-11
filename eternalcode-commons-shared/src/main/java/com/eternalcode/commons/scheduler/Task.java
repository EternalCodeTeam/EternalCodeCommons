package com.eternalcode.commons.scheduler;

public interface Task {

    void cancel();

    boolean isCanceled();

    boolean isAsync();

    boolean isRunning();

    boolean isRepeating();

}
