package dev.yila.functional;

import java.util.function.Function;

/**
 * Class that sore a value that can mutate.
 * The value can be mutated only using mutate method, that is synchronized.
 * @param <T> of the value
 */
public class Mutation<T> {

    /**
     * Create a mutation
     * @param initialValue
     * @param <T>
     * @return
     */
    public static <T> Mutation<T> create(T initialValue) {
        return new Mutation<>(initialValue);
    }

    private volatile T value;

    private Mutation(T initialValue) {
        this.value = initialValue;
    }

    /**
     * Get the current value
     * @return
     */
    public T get() {
        return this.value;
    }

    /**
     * Make a mutation of the value stored
     * @param function that mutates the value, returning the new value
     * @return
     */
    public synchronized Mutation<T> mutate(Function<T, T> function) {
        this.value = function.apply(this.value);
        return this;
    }
}
