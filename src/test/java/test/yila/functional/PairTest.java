package test.yila.functional;

import dev.yila.functional.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PairTest {

    @Test
    void usePair() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);
        assertEquals(5, pair.getRight());
        assertEquals("hello", pair.getLeft());
    }
}
