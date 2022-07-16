package dev.yila.functional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class Agent {

    private static Map<Id, Supplier> agents = new ConcurrentHashMap<>();

    public static Id create(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Null supplier is not allowed for Agent.");
        }
        Id id = new Id();
        agents.put(id, supplier);
        return id;
    }

    public static <T> T get(Id id) {
        return (T) agents.getOrDefault(id, () -> {
            throw new IllegalArgumentException("Invalid Id");
        }).get();
    }

    public static Id update(Id id, Function function) {
        synchronized (id) {
            Object previousValue = agents.get(id).get();
            Object newValue = function.apply(previousValue);
            agents.put(id, () -> newValue);
        }
        return id;
    }
}
