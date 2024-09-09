package com.eternalcode.commons.scheduler;

import org.bukkit.plugin.Plugin;

public interface Task {

    void cancel();

    boolean isCanceled();

    boolean isAsync();

    Plugin getPlugin();

    boolean isRunning();

    boolean isRepeating();

}
