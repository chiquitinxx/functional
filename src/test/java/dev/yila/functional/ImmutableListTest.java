package dev.yila.functional;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImmutableListTest {

    @Test
    public void invalidImmutableLists() {
        assertThrows(IllegalArgumentException.class, ImmutableList::create);
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.create(1, null));
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
}
