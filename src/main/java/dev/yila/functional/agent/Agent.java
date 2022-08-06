package dev.yila.functional.agent;

import java.util.function.Function;

public class Agent {

    private Agent() {}

    /**
     * Store object and returns reference to get it.
     * @param value to store, immutable if possible
     * @return Id
     */
    public static <T> Id<T> create(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Null value is not allowed for Agent.");
        }
        Id<T> id = new Id<>(value);
        return id;
    }

    /**
     * Get immutable object stored for an id.
     * @param id
     * @return
     * @param <T>
     */
    public static <T> T get(Id<T> id) {
        return (T) id.getValue();
    }

    /**
     * Change object stored for the id.
     * @param id
     * @param function
     * @return
     */
    public static <T> Id<T> update(Id<T> id, Function<T, T> function) {
        synchronized (id) {
            id.apply(function);
        }
        return id;
    }
}
