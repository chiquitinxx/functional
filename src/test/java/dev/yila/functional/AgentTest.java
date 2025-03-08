package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgentTest {

    @Test
    public void useAgent() {
        Agent<Integer> agent = Agent.create(0);
        assertEquals(0, Agent.get(agent));
        Agent.update(agent, old -> old + 2);
        assertEquals(2, Agent.get(agent));
        Agent.update(agent, old -> old + 3);
        assertEquals(5, Agent.get(agent));
    }
}
