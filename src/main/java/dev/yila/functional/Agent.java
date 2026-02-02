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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Agent that store a value, and modification of the value are thread safe.
 * @param <T>
 */
public class Agent<T> {

    static final Integer DEFAULT_MAX_CAPACITY = 10_000;

    /**
     *
     * @param executor
     * @param initialValue
     * @return
     * @param <T>
     */
    public static <T> Agent<T> create(Executor executor, T initialValue) {
        return new Agent<>(executor, initialValue, DEFAULT_MAX_CAPACITY);
    }

    /**
     *
     * @param executor
     * @param initialValue
     * @param maxCapacity
     * @return
     * @param <T>
     */
    public static <T> Agent<T> create(Executor executor, T initialValue, int maxCapacity) {
        return new Agent<>(executor, initialValue, maxCapacity);
    }

    /**
     *
     * @param agent
     * @return
     * @param <T>
     */
    public static <T> AsyncResult<T> get(Agent<T> agent) {
        return agent.getValue();
    }

    /**
     *
     * @param agent
     * @param function
     * @param <T>
     */
    public static <T> void update(Agent<T> agent, Function<T, T> function) {
        agent.addFunction(function);
    }

    private final Executor executor;
    private final int maxCapacity;
    private volatile T value;
    private final ConcurrentLinkedQueue<Function<T, T>> mailbox = new ConcurrentLinkedQueue<>();
    private final AtomicInteger wip = new AtomicInteger(0);
    private final AtomicInteger size = new AtomicInteger(0);

    private Agent(Executor executor, T initialValue, int maxCapacity) {
        this.executor = executor;
        this.value = initialValue;
        this.maxCapacity = maxCapacity;
    }

    private AsyncResult<T> getValue() {
        CompletableFuture<T> future = new CompletableFuture<>();
        addFunction(current -> {
            future.complete(current);
            return current;
        });
        return AsyncResult.of(executor, future);
    }

    private void addFunction(Function<T, T> updateFunction) {
        if (size.incrementAndGet() > maxCapacity) {
            size.decrementAndGet();
            throw new IllegalStateException("Agent mailbox is full");
        }
        mailbox.offer(updateFunction);
        drain();
    }

    private void drain() {
        if (wip.getAndIncrement() == 0) {
            executor.execute(this::runLoop);
        }
    }

    private void runLoop() {
        int missed = 1;
        do {
            Function<T, T> update;
            while ((update = mailbox.poll()) != null) {
                try {
                    this.value = update.apply(this.value);
                } catch (Exception e) {
                    // Ignore exception
                } finally {
                    size.decrementAndGet();
                }
            }
            missed = wip.addAndGet(-missed);
        } while (missed != 0);
    }
}
