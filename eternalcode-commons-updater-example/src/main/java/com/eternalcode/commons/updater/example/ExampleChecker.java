package com.eternalcode.commons.updater.example;

import com.eternalcode.commons.updater.UpdateResult;

public class ExampleChecker {
    public static void main(String[] args) {
        ExampleUpdateService service = new ExampleUpdateService();

        UpdateResult modrinthResult = service.checkModrinth("EternalCombat", "2.2.0");
        System.out.println("Modrinth update available: " + modrinthResult.isUpdateAvailable());
        if (modrinthResult.isUpdateAvailable()) {
            System.out.println("Latest: " + modrinthResult.latestVersion());
            System.out.println("Download: " + modrinthResult.downloadUrl());
            System.out.println("Release page: " + modrinthResult.releaseUrl());
        }
    }
}
