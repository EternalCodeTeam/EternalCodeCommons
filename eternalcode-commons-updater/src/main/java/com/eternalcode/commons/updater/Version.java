package com.eternalcode.commons.updater;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
    private final String value;
    private final int[] parts;

    public Version(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        this.value = version.trim();
        this.parts = parseVersion(this.value);
    }

    private int[] parseVersion(String version) {
        String cleaned = version.startsWith("v") ? version.substring(1) : version;

        int dashIndex = cleaned.indexOf('-');
        if (dashIndex > 0) {
            cleaned = cleaned.substring(0, dashIndex);
        }

        String[] stringParts = cleaned.split("\\.");
        int[] intParts = new int[stringParts.length];

        for (int i = 0; i < stringParts.length; i++) {
            try {
                intParts[i] = Integer.parseInt(stringParts[i]);
            }
            catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid version format: " + version);
            }
        }

        return intParts;
    }

    @Override
    public int compareTo(@NotNull Version other) {
        if (other == null) {
            return 1;
        }

        int maxLength = Math.max(this.parts.length, other.parts.length);

        for (int i = 0; i < maxLength; i++) {
            int thisPart = i < this.parts.length ? this.parts[i] : 0;
            int otherPart = i < other.parts.length ? other.parts[i] : 0;

            int result = Integer.compare(thisPart, otherPart);
            if (result != 0) {
                return result;
            }
        }

        return 0;
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
        return java.util.Arrays.hashCode(parts);
    }

    @Override
    public String toString() {
        return value;
    }
}
