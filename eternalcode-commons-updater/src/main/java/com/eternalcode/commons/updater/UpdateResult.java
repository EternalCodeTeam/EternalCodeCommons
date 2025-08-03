package com.eternalcode.commons.updater;

public record UpdateResult(Version currentVersion, Version latestVersion, String downloadUrl, String releaseUrl) {

    public boolean isUpdateAvailable() {
        return this.latestVersion.isNewerThan(this.currentVersion);
    }
}
