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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * A thread-safe container that implements the Actor model for managing shared mutable state.
 * <p>
 * An Agent holds a value and ensures that all modifications to that value are executed
 * sequentially and safely in a concurrent environment. Updates are queued and processed
 * one at a time using a non-blocking queue, eliminating the need for external synchronization.
 * <p>
 * This implementation is inspired by the Actor model from functional programming, where
 * state modifications are performed by sending messages (functions) to an actor that
 * processes them sequentially.
 * <p>
 * Example usage:
 * <pre>{@code
 * ExecutorService executor = Executors.newFixedThreadPool(4);
 * Agent<Integer> counter = Agent.create(executor, 0);
 *
 * // Send update messages from multiple threads
 * for (int i = 0; i < 1000; i++) {
 *     Agent.update(counter, value -> value + 1);
 * }
 *
 * // Get the current value (async)
 * Result<Integer> result = Agent.get(counter);
 * System.out.println(result.getOrThrow()); // 1000
 * }</pre>
 *
 * @param <T> the type of the value stored in this agent
 */
public class Agent<T> {

    static final Integer DEFAULT_MAX_CAPACITY = 10_000;

    static final Consumer<Exception> DEFAULT_ERROR_HANDLER = e -> {
        System.err.println("Agent update failed: " + e.getMessage());
        e.printStackTrace();
    };

    /**
     * Creates a new Agent with the specified executor and initial value.
     * <p>
     * Uses the default mailbox capacity ({@value #DEFAULT_MAX_CAPACITY}) and the
     * default error handler (logs to stderr).
     *
     * @param executor the executor service used to process update functions
     * @param initialValue the initial value stored in this agent
     * @return a new Agent instance
     * @param <T> the type of the value stored in this agent
     * @throws NullPointerException if executor or initialValue is null
     */
    public static <T> Agent<T> create(Executor executor, T initialValue) {
        Objects.requireNonNull(executor);
        Objects.requireNonNull(initialValue);
        return new Agent<>(executor, initialValue, DEFAULT_MAX_CAPACITY, DEFAULT_ERROR_HANDLER);
    }

    /**
     * Creates a new Agent with the specified executor, initial value, and mailbox capacity.
     * <p>
     * Uses the default error handler (logs to stderr). The mailbox capacity limits
     * the number of pending update functions that can be queued. When the mailbox is
     * full, new updates will be rejected with an {@link IllegalStateException}.
     *
     * @param executor the executor service used to process update functions
     * @param initialValue the initial value stored in this agent
     * @param maxCapacity the maximum number of pending updates in the mailbox
     * @return a new Agent instance
     * @param <T> the type of the value stored in this agent
     * @throws NullPointerException if executor or initialValue is null
     */
    public static <T> Agent<T> create(Executor executor, T initialValue, int maxCapacity) {
        Objects.requireNonNull(executor);
        Objects.requireNonNull(initialValue);
        return new Agent<>(executor, initialValue, maxCapacity, DEFAULT_ERROR_HANDLER);
    }

    /**
     * Creates a new Agent with full customization options.
     * <p>
     * The mailbox capacity limits the number of pending update functions that can be
     * queued. When the mailbox is full, new updates will be rejected with an
     * {@link IllegalStateException}.
     * <p>
     * The error handler is invoked when an update function throws an exception.
     *
     * @param executor the executor service used to process update functions
     * @param initialValue the initial value stored in this agent
     * @param maxCapacity the maximum number of pending updates in the mailbox
     * @param errorHandler the consumer that handles exceptions thrown by update functions
     * @return a new Agent instance
     * @param <T> the type of the value stored in this agent
     * @throws NullPointerException if any parameter is null
     */
    public static <T> Agent<T> create(Executor executor, T initialValue, int maxCapacity,
                                      Consumer<Exception> errorHandler) {
        Objects.requireNonNull(executor);
        Objects.requireNonNull(initialValue);
        Objects.requireNonNull(errorHandler);
        return new Agent<>(executor, initialValue, maxCapacity, errorHandler);
    }

    /**
     * Retrieves the current value from an Agent asynchronously.
     * <p>
     * This is a convenience method that calls {@link #getValue()} on the agent.
     * The result is returned as an {@link AsyncResult} which can be used in
     * functional pipelines.
     *
     * @param agent the agent to get the value from
     * @return an AsyncResult containing the current value
     * @param <T> the type of the value stored in the agent
     * @throws NullPointerException if agent is null
     */
    public static <T> AsyncResult<T> get(Agent<T> agent) {
        return agent.getValue();
    }

    /**
     * Sends an update function to an Agent for asynchronous execution.
     * <p>
     * The update function will be queued and executed sequentially with all other
     * pending updates. The function receives the current value and returns the new value.
     * <p>
     * This is a convenience method that calls {@link #addFunction(UnaryOperator)} on the agent.
     *
     * @param agent the agent to update
     * @param function the update function that transforms the current value to a new value
     * @param <T> the type of the value stored in the agent
     * @throws NullPointerException if agent or function is null
     * @throws IllegalStateException if the agent's mailbox is full
     */
    public static <T> void update(Agent<T> agent, UnaryOperator<T> function) {
        agent.addFunction(function);
    }

    private final Executor executor;
    private final Semaphore capacitySemaphore;
    private final Consumer<Exception> errorHandler;
    private volatile T value;
    private final ConcurrentLinkedQueue<UnaryOperator<T>> mailbox = new ConcurrentLinkedQueue<>();
    private final AtomicInteger wip = new AtomicInteger(0);

    private Agent(Executor executor, T initialValue, int maxCapacity, Consumer<Exception> errorHandler) {
        this.executor = executor;
        this.value = initialValue;
        this.errorHandler = errorHandler;
        this.capacitySemaphore = new Semaphore(maxCapacity);
    }

    private AsyncResult<T> getValue() {
        CompletableFuture<T> future = new CompletableFuture<>();
        addFunction(current -> {
            future.complete(current);
            return current;
        });
        return AsyncResult.of(executor, future);
    }

    private void addFunction(UnaryOperator<T> updateFunction) {
        if (!capacitySemaphore.tryAcquire()) {
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
            UnaryOperator<T> update;
            while ((update = mailbox.poll()) != null) {
                try {
                    this.value = update.apply(this.value);
                } catch (Exception e) {
                    errorHandler.accept(e);
                } finally {
                    capacitySemaphore.release();
                }
            }
            missed = wip.addAndGet(-missed);
        } while (missed != 0);
    }
}
