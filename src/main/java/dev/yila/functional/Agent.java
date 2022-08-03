package dev.yila.functional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class Agent {

    private Agent() {}

    private static Map<Id, Supplier> agents = new ConcurrentHashMap<>();

    /**
     * Store object and returns reference to get it.
     * @param supplier of immutable object
     * @return Id
     */
    public static Id create(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Null supplier is not allowed for Agent.");
        }
        Id id = new Id();
        agents.put(id, supplier);
        return id;
    }

    /**
     * Get immutable object stored for an id.
     * @param id
     * @return
     * @param <T>
     */
    public static <T> T get(Id id) {
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
    public static Id update(Id id, Function function) {
        synchronized (id) {
            Object previousValue = agents.get(id).get();
            Object newValue = function.apply(previousValue);
            agents.put(id, () -> newValue);
        }
        return id;
    }

    public static <T> Id update(Id id, Function<T, T> function, Class<T> clazz) {
        return update(id, function);
    }
}
