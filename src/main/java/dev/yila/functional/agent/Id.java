package dev.yila.functional.agent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Id<T> {

    private volatile CompletableFuture<T> completableFuture;

    Id(T initialValue) {
        this.completableFuture = CompletableFuture.completedFuture(initialValue);
    }

    T getValue() {
        return completableFuture.join();
    }

    void apply(Function<T, T> function) {
        this.completableFuture = completableFuture.thenApplyAsync(function);
    }
}
