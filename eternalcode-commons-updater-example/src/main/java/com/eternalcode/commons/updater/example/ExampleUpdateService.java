package com.eternalcode.commons.updater.example;

import com.eternalcode.commons.updater.UpdateResult;
import com.eternalcode.commons.updater.Version;
import com.eternalcode.commons.updater.impl.ModrinthUpdateChecker;

public final class ExampleUpdateService {
    private final ModrinthUpdateChecker modrinthChecker = new ModrinthUpdateChecker();

    public UpdateResult checkModrinth(String projectId, String currentVersion) {
        return modrinthChecker.check(projectId, new Version(currentVersion));
    }
}
