package com.eternalcode.commons.bukkit.position;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Disclaimer - Bukkit {@link org.bukkit.Location} storage may cause a memory leak, because it is a wrapper for
 * coordinates and {@link org.bukkit.World} reference. If you need to store location use {@link Position} and
 * {@link PositionAdapter} to convert it to {@link org.bukkit.Location} when needed.
 */
public record Position(double x, double y, double z, float yaw, float pitch, String world) {

    public static final String NONE_WORLD = "__NONE__";

    private static final Pattern PARSE_FORMAT = Pattern.compile(
        "Position\\{x=(?<x>-?[\\d.]+), y=(?<y>-?[\\d.]+), z=(?<z>-?[\\d.]+), yaw=(?<yaw>-?[\\d.]+), pitch=(?<pitch>-?[\\d.]+), world='(?<world>.+)'}");

    public Position(double x, double y, double z, String world) {
        this(x, y, z, 0F, 0F, world);
    }

    public Position(double x, double z, String world) {
        this(x, 0.0, z, 0F, 0F, world);
    }

    public static Position parse(String parse) {
        Matcher matcher = PARSE_FORMAT.matcher(parse);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid position format: " + parse);
        }

        return new Position(
            Double.parseDouble(matcher.group("x")),
            Double.parseDouble(matcher.group("y")),
            Double.parseDouble(matcher.group("z")),
            Float.parseFloat(matcher.group("yaw")),
            Float.parseFloat(matcher.group("pitch")),
            matcher.group("world")
        );
    }

    @Override
    public String toString() {
        return "Position{" +
            "x=" + this.x +
            ", y=" + this.y +
            ", z=" + this.z +
            ", yaw=" + this.yaw +
            ", pitch=" + this.pitch +
            ", world='" + this.world + '\'' +
            '}';
    }

    public boolean isNoneWorld() {
        return this.world.equals(NONE_WORLD);
    }
}
