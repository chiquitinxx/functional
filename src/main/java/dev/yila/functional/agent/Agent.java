package dev.yila.functional.agent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Utility class to create and operate Store classes.
 */
public class Agent {

    private Agent() {}

    /**
     * Store value and returns reference to it.
     * @param value to store, immutable if possible
     * @return Id
     */
    public static <T> Store<T> create(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Null value is not allowed for Agent.");
        }
        Store<T> store = new Store<>(value);
        return store;
    }

    /**
     * Store completable future and returns reference to result.
     * @param completableFuture
     * @return
     * @param <T>
     */
    public static <T> Store<T> create(CompletableFuture<T> completableFuture) {
        if (completableFuture == null) {
            throw new IllegalArgumentException("Null value is not allowed for Agent.");
        }
        Store<T> store = new Store<>(completableFuture);
        return store;
    }

    /**
     * Get immutable value stored.
     * @param store
     * @return
     * @param <T>
     */
    public static <T> T get(Store<T> store) {
        return (T) store.getValue();
    }

    /**
     * Change value saved in the store.
     * @param store
     * @param function
     * @return
     */
    public static <T> Store<T> update(Store<T> store, Function<T, T> function) {
        synchronized (store) {
            store.apply(function);
        }
        return store;
    }
}
