package com.eternalcode.commons.shared.bukkit.position;

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
        assertEquals("world", position.getWorld());
    }

    @Test
    void getX() {
        assertEquals(1.0d, position.getX());
    }

    @Test
    void getY() {
        assertEquals(2.0d, position.getY());
    }

    @Test
    void getZ() {
        assertEquals(3.0d, position.getZ());
    }

    @Test
    void getYaw() {
        assertEquals(4.0f, position.getYaw());
    }

    @Test
    void getPitch() {
        assertEquals(5.0f, position.getPitch());
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
        assertTrue(position.equals(position));

        // Symmetric test
        assertTrue(position.equals(position2));
        assertTrue(position2.equals(position));

        assertFalse(position.equals(position3));
        assertFalse(position3.equals(position));

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
