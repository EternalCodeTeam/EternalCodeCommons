package com.eternalcode.commons.updater.impl;

import com.eternalcode.commons.updater.UpdateChecker;
import com.eternalcode.commons.updater.UpdateResult;
import com.eternalcode.commons.updater.Version;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public final class ModrinthUpdateChecker implements UpdateChecker {

    private static final String API_BASE_URL = "https://api.modrinth.com/v2";
    private static final String MODRINTH_BASE_URL = "https://modrinth.com/plugin";
    private static final String USER_AGENT = "UpdateChecker/1.0";

    private static final Gson GSON = new Gson();

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .build();

    @Override
    public UpdateResult check(String projectId, Version currentVersion) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("Project ID cannot be null or empty");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/project/" + projectId + "/version"))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return UpdateResult.empty(currentVersion);
            }

            return this.parseVersionResponse(response.body(), currentVersion, projectId);
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to check Modrinth updates for project: " + projectId, exception);
        }
    }

    private UpdateResult parseVersionResponse(String json, Version currentVersion, String projectId) {
        try {
            List<ModrinthVersion> versions = GSON.fromJson(
                json, new TypeToken<>() {
                });
            if (versions == null || versions.isEmpty()) {
                return UpdateResult.empty(currentVersion);
            }

            List<ModrinthVersion> filteredVersions = versions;
            if (!currentVersion.isSnapshot()) {
                filteredVersions = versions.stream()
                    .filter(version -> !version.versionNumber().contains("-SNAPSHOT"))
                    .toList();
            }

            if (filteredVersions.isEmpty()) {
                return UpdateResult.empty(currentVersion);
            }

            ModrinthVersion latestVersionData = filteredVersions.get(0);
            String versionNumber = latestVersionData.versionNumber();
            if (versionNumber == null || versionNumber.trim().isEmpty()) {
                return UpdateResult.empty(currentVersion);
            }

            String releaseUrl = MODRINTH_BASE_URL + "/" + projectId + "/version/" + versionNumber;
            String downloadUrl = latestVersionData.files().stream()
                .map(ModrinthFile::url)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(releaseUrl);

            Version latestVersion = new Version(versionNumber);
            return new UpdateResult(currentVersion, latestVersion, downloadUrl, releaseUrl);
        }
        catch (JsonParseException exception) {
            return UpdateResult.empty(currentVersion);
        }
    }

    private record ModrinthVersion(@SerializedName("version_number") String versionNumber, List<ModrinthFile> files) {
    }

    private record ModrinthFile(String url) {
    }
}
