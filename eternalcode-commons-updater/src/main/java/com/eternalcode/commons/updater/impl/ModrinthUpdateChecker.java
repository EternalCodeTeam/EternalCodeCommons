package com.eternalcode.commons.updater.impl;

import com.eternalcode.commons.Lazy;
import com.eternalcode.commons.updater.UpdateChecker;
import com.eternalcode.commons.updater.UpdateResult;
import com.eternalcode.commons.updater.Version;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ModrinthUpdateChecker implements UpdateChecker {

    private static final String API_BASE_URL = "https://api.modrinth.com/v2";
    private static final String MODRINTH_BASE_URL = "https://modrinth.com/plugin";
    private static final String USER_AGENT = "UpdateChecker/1.0";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final Lazy<HttpClient> client = new Lazy<>(() -> HttpClient.newBuilder().connectTimeout(TIMEOUT).build());

    @Override
    public UpdateResult check(String projectId, Version currentVersion) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be null or empty");
        }

        try {
            String url = API_BASE_URL + "/project/" + projectId + "/version";

            HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT).timeout(TIMEOUT).build();

            HttpResponse<String> response = this.client.get().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return createEmptyResult(currentVersion);
            }

            String json = response.body();
            if (json == null || json.trim().isEmpty()) {
                return createEmptyResult(currentVersion);
            }

            return parseVersionResponse(json, currentVersion, projectId);
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to check Modrinth updates for project: " + projectId, exception);
        }
    }

    private UpdateResult parseVersionResponse(String json, Version currentVersion, String projectId) {
        try {
            JSONArray versions = new JSONArray(json);

            if (versions.isEmpty()) {
                return createEmptyResult(currentVersion);
            }

            JSONObject latestVersionObj = versions.getJSONObject(0);

            String versionNumber = latestVersionObj.optString("version_number", null);
            if (versionNumber == null || versionNumber.trim().isEmpty()) {
                return createEmptyResult(currentVersion);
            }

            String downloadUrl = null;
            if (latestVersionObj.has("files")) {
                JSONArray files = latestVersionObj.getJSONArray("files");
                if (!files.isEmpty()) {
                    JSONObject firstFile = files.getJSONObject(0);
                    downloadUrl = firstFile.optString("url", null);
                }
            }

            String releaseUrl = MODRINTH_BASE_URL + "/" + projectId + "/version/" + versionNumber;
            Version latestVersion = new Version(versionNumber);

            return new UpdateResult(currentVersion, latestVersion, downloadUrl, releaseUrl);
        }
        catch (JSONException exception) {
            return createEmptyResult(currentVersion);
        }
    }

    private UpdateResult createEmptyResult(Version currentVersion) {
        return new UpdateResult(currentVersion, currentVersion, null, null);
    }
}
