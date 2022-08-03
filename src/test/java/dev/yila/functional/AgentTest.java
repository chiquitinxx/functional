package dev.yila.functional;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {

    @Test
    void storeAndGetSomething() {
        Agent.Id<String> id = Agent.create(() -> "something");
        assertEquals("something", Agent.get(id));
    }

    @Test
    void cannotCreateAgentWithNullSupplier() {
        assertThrows(IllegalArgumentException.class, () -> Agent.create(null));
    }

    @Test
    void idsAreDifferent() {
        Agent.Id<String> id = Agent.create(() -> "something");
        Agent.Id<String> other = Agent.create(() -> "something");
        assertNotSame(id, other);
        assertNotEquals(id, other);
    }

    @Test
    void updateAgent() {
        Agent.Id<String> id = Agent.create(() -> "something");
        Agent.Id<String> updated = Agent.update(id, old -> old + " new");

        assertSame(id, updated);
        assertEquals("something new", Agent.get(id));
    }

    @Test
    void getValueOfUnknownAgent() {
        assertThrows(IllegalArgumentException.class, () -> Agent.get(new Agent.Id()));
    }

    @RepeatedTest(5)
    void updateConcurrently() {
        Agent.Id<Integer> id = Agent.create(() -> 0);
        IntStream.range(1, 100).parallel()
                .forEach(number -> Agent.update(id, old -> old + number));

        assertEquals(4950, (int) Agent.get(id));
    }

    @Test
    void storeAndUpdateMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        Agent.Id<Map<String, String>> id = Agent.create(() -> map);
        Agent.update(id, m -> {
            m.put("key", "new");
            return m;
        });

        Map<String, String> storedMap = Agent.get(id);
        assertEquals("new", storedMap.get("key"));
    }

    @Test
    void storeAndUpdateImmutable() {
        BigDecimal bigDecimal = new BigDecimal("5");
        Agent.Id<BigDecimal> id = Agent.create(() -> bigDecimal);
        Agent.update(id, number -> number.pow(2));

        BigDecimal updated = Agent.get(id);
        assertEquals(25, updated.intValue());
        assertNotSame(bigDecimal, updated);
    }
}
