package com.eternalcode.commons.shared.scheduler;

public interface Task {

    void cancel();

    boolean isCanceled();

    boolean isAsync();

}
