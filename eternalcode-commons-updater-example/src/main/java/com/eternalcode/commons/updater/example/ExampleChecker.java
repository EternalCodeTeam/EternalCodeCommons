package com.eternalcode.commons.updater.example;

import com.eternalcode.commons.updater.UpdateResult;

public class ExampleChecker {

    private static final String OLD_ETERNALCOMBAT_VERSION = "1.3.3";

    public static void main(String[] args) {
        ExampleUpdateService service = new ExampleUpdateService();

        UpdateResult modrinthResult = service.checkModrinth("EternalCombat", OLD_ETERNALCOMBAT_VERSION);
        System.out.println("Modrinth update available: " + modrinthResult.isUpdateAvailable());
        if (modrinthResult.isUpdateAvailable()) {
            System.out.println("Latest: " + modrinthResult.latestVersion());
            System.out.println("Download: " + modrinthResult.downloadUrl());
            System.out.println("Release page: " + modrinthResult.releaseUrl());
        }
    }
}
