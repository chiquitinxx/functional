package test.yila.functional;

import dev.yila.functional.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
