package com.eternalcode.commons.updater;

public record UpdateResult(Version currentVersion, Version latestVersion, String downloadUrl, String releaseUrl) {

    public static UpdateResult empty(Version currentVersion) {
        return new UpdateResult(currentVersion, currentVersion, null, null);
    }

    public boolean isUpdateAvailable() {
        return this.latestVersion.isNewerThan(this.currentVersion);
    }

}
