package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrioTest {

    @Test
    void nullAreNotAllowedAsValues() {
        assertThrows(IllegalArgumentException.class, () -> new Trio<>(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Trio<>(1, null, 2));
        assertThrows(IllegalArgumentException.class, () -> new Trio<>(1, 2, null));
        assertThrows(IllegalArgumentException.class, () -> new Trio<>(null, 2, 3));
    }

    @Test
    void useTrio() {
        Trio<String, Integer, String> trio = new Trio<>("hello", 5, "world");
        assertEquals(5, trio.getSecond());
        assertEquals("hello", trio.getFirst());
        assertEquals("world", trio.getThird());
    }

    @Test
    void equalTrio() {
        Trio trio = Trio.of(1, 2, 3);
        assertEquals(Trio.of("hello", 5, "world"),
                new Trio<>("hello", 5, "world"));
        assertNotEquals("hello", Trio.of(1, 2, 3));
        assertNotEquals(trio, Trio.of(1, 2, 4));
        assertNotEquals(trio, Trio.of(1, 1, 3));
        assertNotEquals(trio, Trio.of(2, 2, 3));
    }

    @Test
    void hashcode() {
        Trio<String, Integer, String> trio = new Trio<>("hello", 5, "world");

        assertEquals(trio.hashCode(), "hello".hashCode() + Integer.valueOf(5).hashCode() + "world".hashCode());
    }

    @Test
    void trioToString() {
        Trio<String, Integer, String> hello5world = new Trio<>("hello", 5, "world");

        assertEquals("Trio(hello, 5, world)", hello5world.toString());
    }
}
