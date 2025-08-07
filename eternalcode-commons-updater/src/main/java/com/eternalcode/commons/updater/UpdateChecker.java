package com.eternalcode.commons.updater;

public interface UpdateChecker {

    UpdateResult check(String projectId, Version currentVersion);
}
