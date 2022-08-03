package dev.yila.functional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class Agent {

    private Agent() {}

    private static Map<Id<?>, Supplier<?>> agents = new ConcurrentHashMap<>();

    /**
     * Store object and returns reference to get it.
     * @param supplier of immutable object
     * @return Id
     */
    public static <T> Id<T> create(Supplier<T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Null supplier is not allowed for Agent.");
        }
        Id<T> id = new Id<>();
        agents.put(id, supplier);
        return id;
    }

    /**
     * Get immutable object stored for an id.
     * @param id
     * @return
     * @param <T>
     */
    public static <T> T get(Id<T> id) {
        return (T) agents.getOrDefault(id, () -> {
            throw new IllegalArgumentException("Invalid Id");
        }).get();
    }

    /**
     * Change object stored for the id.
     * @param id
     * @param function
     * @return
     */
    public static <T> Id<T> update(Id<T> id, Function<T, T> function) {
        synchronized (id) {
            T previousValue = (T) agents.get(id).get();
            T newValue = function.apply(previousValue);
            agents.put(id, () -> newValue);
        }
        return id;
    }

    static class Id<T> {
        Id() {}
    }
}
