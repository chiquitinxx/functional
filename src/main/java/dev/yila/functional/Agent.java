package dev.yila.functional;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Agent that store a value, and modification of the value are thread safe.
 * @param <T>
 */
public class Agent<T> {

    /**
     *
     * @param initialValue
     * @return
     * @param <T>
     */
    public static <T> Agent<T> create(T initialValue) {
        return new Agent<>(initialValue);
    }

    /**
     *
     * @param agent
     * @return
     * @param <T>
     */
    public static <T> T get(Agent<T> agent) {
        return agent.getValue();
    }

    /**
     *
     * @param agent
     * @param function
     * @param <T>
     */
    public static <T> void update(Agent<T> agent, Function<T, T> function) {
        agent.changeValue(function);
    }

    private final Lock lock;
    private T value;

    private Agent(T initialValue) {
        this.value = initialValue;
        this.lock = new ReentrantLock();
    }

    private T getValue() {
        lock.lock();
        try {
            return this.value;
        } finally {
            lock.unlock();
        }
    }

    private void changeValue(Function<T, T> function) {
        lock.lock();
        try {
            this.value = function.apply(this.value);
        } finally {
            lock.unlock();
        }
    }
}
