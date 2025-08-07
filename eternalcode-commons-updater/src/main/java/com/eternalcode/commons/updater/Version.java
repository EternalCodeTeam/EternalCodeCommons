package com.eternalcode.commons.updater;

public class Version implements Comparable<Version> {

    private final String value;

    public Version(String version) {
        this.value = version.trim();
    }

    @Override
    public int compareTo(Version version) {
        return this.value.compareTo(version.value);
    }

    public boolean isNewerThan(Version version) {
        return this.value.compareTo(version.value) > 0;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Version other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return this.value;
    }
}
