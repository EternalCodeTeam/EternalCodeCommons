package com.eternalcode.commons.updater.impl;

import com.eternalcode.commons.updater.UpdateChecker;
import com.eternalcode.commons.updater.UpdateResult;
import com.eternalcode.commons.updater.Version;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public final class ModrinthUpdateChecker implements UpdateChecker {

    private static final String API_BASE_URL = "https://api.modrinth.com/v2";
    private static final String MODRINTH_BASE_URL = "https://modrinth.com/mod";
    private static final String USER_AGENT = "UpdateChecker/1.0";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client;

    public ModrinthUpdateChecker() {
        this.client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    }

    @Override
    public UpdateResult check(String projectId, Version currentVersion) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be null or empty");
        }

        try {
            String url = API_BASE_URL + "/project/" + projectId + "/version";

            HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT).timeout(TIMEOUT).build();

            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return createEmptyResult(currentVersion);
            }

            String json = response.body();
            if (json == null || json.trim().isEmpty()) {
                return createEmptyResult(currentVersion);
            }

            Optional<String> versionNumber = extractJsonValue(json, "version_number");
            Optional<String> downloadUrl = extractJsonValue(json, "url");

            if (versionNumber.isEmpty()) {
                return createEmptyResult(currentVersion);
            }

            String releaseUrl = MODRINTH_BASE_URL + "/" + projectId + "/version/" + versionNumber.get();
            Version latestVersion = new Version(versionNumber.get());

            return new UpdateResult(currentVersion, latestVersion, downloadUrl.orElse(null), releaseUrl);
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to check Modrinth updates for project: " + projectId, exception);
        }
    }

    private UpdateResult createEmptyResult(Version currentVersion) {
        return new UpdateResult(currentVersion, currentVersion, null, null);
    }

    private Optional<String> extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return Optional.empty();
        }

        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);

        if (start == -1) {
            return Optional.empty();
        }

        start += pattern.length();
        int end = json.indexOf("\"", start);

        if (end == -1) {
            return Optional.empty();
        }

        String value = json.substring(start, end);
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
}
