package com.eternalcode.commons.bukkit.position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(1.0d, 2.0d, 3.0d, 4.0f, 5.0f, "world");
    }

    @Test
    void getWorld() {
        assertEquals("world", position.world());
    }

    @Test
    void getX() {
        assertEquals(1.0d, position.x());
    }

    @Test
    void getY() {
        assertEquals(2.0d, position.y());
    }

    @Test
    void getZ() {
        assertEquals(3.0d, position.z());
    }

    @Test
    void getYaw() {
        assertEquals(4.0f, position.yaw());
    }

    @Test
    void getPitch() {
        assertEquals(5.0f, position.pitch());
    }

    @Test
    void isNoneWorld() {
        assertFalse(position.isNoneWorld());
    }

    @Test
    void testEqualsAndHashCode() {
        Position position2 = new Position(1.0d, 2.0d, 3.0d, 4.0f, 5.0f, "world");
        Position position3 = new Position(1.0d, 2.0d, 3.1d, 4.0f, 5.0f, "world");

        // Reflexive test
        assertEquals(position, position);

        // Symmetric test
        assertEquals(position, position2);
        assertEquals(position2, position);

        assertNotEquals(position, position3);
        assertNotEquals(position3, position);

        // hashCode
        assertEquals(position.hashCode(), position2.hashCode());
        assertNotEquals(position.hashCode(), position3.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Position{x=1.0, y=2.0, z=3.0, yaw=4.0, pitch=5.0, world='world'}";
        assertEquals(expectedString, position.toString());
    }

    @Test
    void parse() {
        Position parsed = Position.parse("Position{x=1.0, y=2.0, z=3.0, yaw=4.0, pitch=5.0, world='world'}");
        assertEquals(position, parsed);
    }

    @Test
    void parseInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Position.parse("Invalid{x=1.0, y=2.0, z=3.0, yaw=4.0, pitch=5.0, world='world'}"));
    }
}
