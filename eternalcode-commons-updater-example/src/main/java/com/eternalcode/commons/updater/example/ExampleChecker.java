package com.eternalcode.commons.updater.example;

import com.eternalcode.commons.updater.UpdateResult;

public class ExampleChecker {

    private static final String OLD_ETERNALCOMBAT_VERSION = "1.3.3";
    private static final String SNAPSHOT_VERSION = "1.3.3-SNAPSHOT";

    public static void main(String[] args) {
        ExampleUpdateService updateService = new ExampleUpdateService();

        System.out.println("=== Testing with STABLE version (1.3.3) ===");
        UpdateResult stableResult = updateService.checkModrinth("EternalCombat", OLD_ETERNALCOMBAT_VERSION);
        printResult(stableResult);

        System.out.println("\n=== Testing with SNAPSHOT version (1.3.3-SNAPSHOT) ===");
        UpdateResult snapshotResult = updateService.checkModrinth("EternalCombat", SNAPSHOT_VERSION);
        printResult(snapshotResult);
    }

    private static void printResult(UpdateResult result) {
        System.out.println("Current version: " + result.currentVersion());
        System.out.println("Latest version: " + result.latestVersion());
        System.out.println("Update available: " + result.isUpdateAvailable());
        if (result.isUpdateAvailable()) {
            System.out.println("Download: " + result.downloadUrl());
            System.out.println("Release page: " + result.releaseUrl());
        }
    }
}
