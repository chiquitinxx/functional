package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PairTest {

    @Test
    void nullAreNotAllowedAsValues() {
        assertThrows(IllegalArgumentException.class, () -> new Pair<>(null, null));
        assertThrows(IllegalArgumentException.class, () -> new Pair<>(1, null));
        assertThrows(IllegalArgumentException.class, () -> new Pair<>(null, 2));
    }

    @Test
    void usePair() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);
        assertEquals(5, pair.getRight());
        assertEquals("hello", pair.getLeft());
    }

    @Test
    void applyFunctionToPair() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);
        Integer result = pair.apply((left, right) -> right * 7);
        assertEquals(35, result);
    }

    @Test
    void equalPair() {
        Pair<String, Integer> hello5 = new Pair<>("hello", 5);
        Pair<String, Integer> hello6 = new Pair<>("hello", 6);
        Pair<String, Integer> bye5 = new Pair<>("bye", 5);
        Pair<String, Integer> sameHello5 = new Pair<>("hello", 5);

        assertNotEquals(hello5, hello6);
        assertNotEquals(hello5, bye5);
        assertNotEquals(bye5, hello6);

        assertEquals(hello5, sameHello5);
    }

    @Test
    void hashcode() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);

        assertEquals(pair.hashCode(), "hello".hashCode() + Integer.valueOf(5).hashCode());
    }

    @Test
    void pairToString() {
        Pair<String, Integer> hello5 = new Pair<>("hello", 5);

        assertEquals("Pair(hello, 5)", hello5.toString());
    }
}
