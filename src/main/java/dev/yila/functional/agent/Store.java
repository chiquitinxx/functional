package dev.yila.functional.agent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
