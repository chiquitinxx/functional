package dev.yila.functional.agent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Store values that can not be updated concurrently.
 * Creating and update values in the store is done asynchronously.
 * Only blocks when you get the value stored.
 * @param <T>
 */
public class Store<T> {

    private volatile CompletableFuture<T> completableFuture;

    Store(T initialValue) {
        this.completableFuture = CompletableFuture.completedFuture(initialValue);
    }

    Store(CompletableFuture<T> completableFuture) {
        this.completableFuture = completableFuture;
    }

    T getValue() {
        return completableFuture.join();
    }

    void apply(Function<T, T> function) {
        this.completableFuture = completableFuture.thenApplyAsync(function);
    }
}
