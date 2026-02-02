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

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgentTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Test
    public void useAgent() {
        Agent<Integer> agent = Agent.create(executor, 0);
        assertEquals(0, Agent.get(agent).getOrThrow());
        Agent.update(agent, old -> old + 2);
        assertEquals(2, Agent.get(agent).getOrThrow());
        Agent.update(agent, old -> old + 3);
        assertEquals(5, Agent.get(agent).getOrThrow());
    }

    @Test
    public void sequentialGet() {
        Agent<Integer> agent = Agent.create(executor, 0);
        Agent.update(agent, old -> old + 2);
        Agent.update(agent, old -> old + 3);
        Result<Integer> current = Agent.get(agent);
        Agent.update(agent, old -> old + 8);
        assertEquals(5, current.getOrThrow());
        assertEquals(13, Agent.get(agent).getOrThrow());
    }

    @Test
    public void concurrentChanges() {
        int threads = 1000;
        Agent<Integer> agent = Agent.create(executor, 0);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> Agent.update(agent, old -> old + 1)).start();
        }
        await().until(() -> threads == Agent.get(agent).getOrThrow());
    }

    @Test
    public void resilientToExceptions() {
        Agent<Integer> agent = Agent.create(executor, 0);
        Agent.update(agent, i -> { throw new RuntimeException("Boom"); });
        Agent.update(agent, i -> i + 1);
        assertEquals(1, Agent.get(agent).getOrThrow());
    }

    @Test
    public void capacityLimit() {
        Agent<Integer> agent = Agent.create(executor, 0, 2);
        Agent.update(agent, i -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }
            return i + 1;
        });
        Agent.update(agent, i -> i + 1);
        try {
            Agent.update(agent, i -> i + 1);
        } catch (IllegalStateException e) {
            assertEquals("Agent mailbox is full", e.getMessage());
        }
        
        await().ignoreException(IllegalStateException.class)
                .until(() -> Agent.get(agent).getOrThrow() == 2);
    }
}
