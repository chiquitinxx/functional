package dev.yila.functional;

import dev.yila.functional.failure.Failure;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncResult <T> implements Result<T> {

    private final CompletableFuture<DirectResult<T>> completableFuture;

    private AsyncResult(CompletableFuture<T> future) {
        this.completableFuture = future.handleAsync((result, throwable) -> {
            if (throwable != null) {
                return DirectResult.failure(throwable);
            }
            return DirectResult.ok(result);
        });
    }

    public static <T> AsyncResult<T> create(CompletableFuture<T> future) {
        return new AsyncResult<>(future);
    }

    @Override
    public boolean hasFailure() {
        return getResult().hasFailure();
    }

    @Override
    public T getOrThrow() {
        return getResult().getOrThrow();
    }

    @Override
    public T orElse(Function<Result<T>, T> function) {
        return getResult().orElse(function);
    }

    @Override
    public Optional<Failure> failure() {
        return getResult().failure();
    }

    @Override
    public <R> Result<R> map(Function<T, R> function) {
        return getResult().map(function);
    }

    @Override
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        return getResult().flatMap(function);
    }

    @Override
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        return getResult().flatMap(function, throwableClass);
    }

    @Override
    public Result<T> onSuccess(Consumer<T> consumer) {
        return getResult().onSuccess(consumer);
    }

    @Override
    public Result<T> onFailure(Consumer<Result<T>> consumer) {
        return getResult().onFailure(consumer);
    }

    @Override
    public Optional<T> value() {
        return getResult().value();
    }

    private Result<T> getResult() {
        return this.completableFuture.join();
    }
}
