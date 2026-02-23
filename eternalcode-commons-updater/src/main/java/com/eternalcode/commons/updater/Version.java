package com.eternalcode.commons.updater;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {

    private static final int DEFAULT_VERSION_COMPONENT_VALUE = 0;

    private final String value;
    private final int[] versionComponents;

    public Version(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        this.value = version.trim();
        this.versionComponents = parseVersion(this.value);
    }

    private static String cleanVersion(String version) {
        String cleaned = version.startsWith("v") ? version.substring(1) : version;
        int dashIndex = cleaned.indexOf('-');
        if (dashIndex > 0) {
            return cleaned.substring(0, dashIndex);
        }

        return cleaned;
    }

    private int[] parseVersion(String version) {
        String cleaned = cleanVersion(version);
        String[] rawVersionComponents = cleaned.split("\\.");
        int[] versionComponents = new int[rawVersionComponents.length];

        for (int i = 0; i < rawVersionComponents.length; i++) {
            try {
                versionComponents[i] = Integer.parseInt(rawVersionComponents[i]);
            }
            catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid version format: " + version);
            }
        }

        return versionComponents;
    }

    @Override
    public int compareTo(@NotNull Version other) {
        int maxLength = Math.max(this.versionComponents.length, other.versionComponents.length);

        for (int i = 0; i < maxLength; i++) {
            int thisComponent = getComponentAtIndex(i, this);
            int otherComponent = getComponentAtIndex(i, other);

            int result = Integer.compare(thisComponent, otherComponent);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    private int getComponentAtIndex(int index, Version version) {
        return index < version.versionComponents.length
            ? version.versionComponents[index]
            : DEFAULT_VERSION_COMPONENT_VALUE;
    }

    public boolean isNewerThan(Version other) {
        return this.compareTo(other) > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Version version = (Version) obj;
        return this.compareTo(version) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(versionComponents);
    }

    public boolean isSnapshot() {
        return this.value.contains("-SNAPSHOT");
    }

    @Override
    public String toString() {
        return value;
    }
}
