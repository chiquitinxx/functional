package dev.yila.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ImmutableListTest {

    @Test
    public void invalidImmutableLists() {
        assertThrows(IllegalArgumentException.class, ImmutableList::create);
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.create(1, null));
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.from(null));
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.from(Collections.singletonList(null)));
    }

    @Test
    public void usingList() {
        ImmutableList<String> list = ImmutableList.create("hello", "world", "!");

        assertEquals("hello", list.head());
        assertEquals(ImmutableList.create("world", "!"), list.tail().getOrThrow());
        assertEquals(ImmutableList.create("!"), list.tail().getOrThrow().tail().getOrThrow());
        assertEquals("[hello, world, !]", list.toString());
    }

    @Test
    public void distinctLists() {
        ImmutableList<String> list = ImmutableList.create("hello", "world");

        assertFalse(list.equals("hello"));
        assertNotEquals(list, ImmutableList.create("world"));
        assertNotEquals(list, ImmutableList.create("hello"));
        assertNotEquals(list, ImmutableList.create("hello", "world", "!"));
    }

    @Test
    public void emptyTail() {
        ImmutableList<String> list = ImmutableList.create("hello");
        assertTrue(list.tail().failure().get() instanceof EmptyTailFailure);
    }

    @ParameterizedTest
    @ValueSource(ints = { 100, 1_000, 10_000, 100_000, 1_000_000, 4_000_000 })
    public void longList(int length) {
        List<Integer> numbers = Stream.iterate(1, x -> x + 1)
                .limit(length)
                .collect(Collectors.toList());
        ImmutableList<Integer> list = ImmutableList.from(numbers);
        Result<ImmutableList<Integer>> rest = list.tail();
        while (!rest.hasFailure()) {
            list = rest.getOrThrow();
            rest = list.tail();
        }
        assertEquals(length, list.head());
    }

    @Test
    public void listSize() {
        assertEquals(1, ImmutableList.create("hello").size());
        assertEquals(2, ImmutableList.create("hello", "world").size());
        assertEquals(1, ImmutableList.create("hello", "world").tail().getOrThrow().size());
    }
}
