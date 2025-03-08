package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmutableListTest {

    @Test
    public void usingList() {
        ImmutableList<String> list = ImmutableList.create("hello", "world", "!");

        assertEquals("hello", list.head());
        assertEquals(ImmutableList.create("world", "!"), list.tail().getOrThrow());
        assertEquals(ImmutableList.create("!"), list.tail().getOrThrow().tail().getOrThrow());
        assertEquals("[hello, world, !]", list.toString());
    }
}
