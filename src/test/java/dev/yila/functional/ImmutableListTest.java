/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.yila.functional;

import dev.yila.functional.failure.EmptyTailFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ImmutableListTest {

    @Test
    public void invalidImmutableLists() {
        assertThrows(IllegalArgumentException.class, ImmutableList::create);
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.create(1, null));
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.from(null));
        assertThrows(IllegalArgumentException.class, () -> ImmutableList.from(Collections.emptyList()));
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

    @Test
    public void isThreadSafe() throws InterruptedException {
        List<Integer> sourceList = new ArrayList<>();
        IntStream.range(0, 1000000).forEach(sourceList::add);
        ImmutableList<Integer> immutableList = ImmutableList.from(sourceList);

        int numberOfThreads = 300;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        Map<Integer, Result<ImmutableList<Integer>>> results = new ConcurrentHashMap<>();

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    latch.await();
                    Result<ImmutableList<Integer>> tail = immutableList.tail();
                    results.put(System.identityHashCode(tail.getOrThrow()), tail);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(numberOfThreads, results.size(), "Multiple different tail objects were created, indicating a race condition.");
        assertEquals(numberOfThreads,
                results.values().stream().filter(r -> r.getOrThrow().head().equals(1)).count());
    }
}
