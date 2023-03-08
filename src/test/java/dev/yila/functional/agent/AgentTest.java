package dev.yila.functional.agent;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {

    @Test
    void storeAndGetSomething() {
        Store<String> something = Agent.create("something");
        assertEquals("something", Agent.get(something));
    }

    @Test
    void storeCompletableFuture() {
        Store<String> something = Agent.create(CompletableFuture.completedFuture("something"));
        assertEquals("something", Agent.get(something));
    }

    @Test
    void storeFailingCompletableFuture() {
        Store<String> something = Agent.create(CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException();
        }));
        assertThrows(CompletionException.class, () -> Agent.get(something));
    }

    @Test
    void cannotCreateAgentWithNullSupplier() {
        assertThrows(IllegalArgumentException.class, () -> Agent.create((String) null));
        assertThrows(IllegalArgumentException.class, () -> Agent.create((CompletableFuture) null));
    }

    @Test
    void idsAreDifferent() {
        Store<String> string = Agent.create("something");
        Store<String> other = Agent.create("something");
        assertNotSame(string, other);
        assertNotEquals(string, other);
    }

    @Test
    void updateAgent() {
        Store<String> string = Agent.create("something");
        Store<String> updated = Agent.update(string, old -> old + " new");

        assertSame(string, updated);
        assertEquals("something new", Agent.get(string));
    }

    @RepeatedTest(5)
    void updateConcurrently() {
        Store<Integer> number = Agent.create(0);
        IntStream.range(1, 100).parallel()
                .forEach(n -> Agent.update(number, old -> old + n));

        assertEquals(4950, (int) Agent.get(number));
    }

    @Test
    void storeAndUpdateMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        Store<Map<String, String>> mapStore = Agent.create(map);
        Agent.update(mapStore, m -> {
            m.put("key", "new");
            return m;
        });

        Map<String, String> storedMap = Agent.get(mapStore);
        assertEquals("new", storedMap.get("key"));
    }

    @Test
    void storeAndUpdateImmutable() {
        BigDecimal bigDecimal = new BigDecimal("5");
        Store<BigDecimal> bigdecimal = Agent.create(bigDecimal);
        Agent.update(bigdecimal, number -> number.pow(2));

        BigDecimal updated = Agent.get(bigdecimal);
        assertEquals(25, updated.intValue());
        assertNotSame(bigDecimal, updated);
    }

    @Test
    void notBlockingCreatingStore() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Agent.create(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
                atomicInteger.set(1);
                return "hello";
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        }));

        assertEquals(0, atomicInteger.get());
    }
    @Test
    void notBlockingUpdatingStore() {
        Store<String> string = Agent.create("hello");
        AtomicInteger atomicInteger = new AtomicInteger(0);

        Agent.update(string, (value) -> {
            try {
                Thread.sleep(100);
                atomicInteger.set(1);
                return value.toUpperCase();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        });
        assertEquals(0, atomicInteger.get());
    }
}
