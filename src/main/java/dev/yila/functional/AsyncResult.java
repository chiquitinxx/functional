package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.LazyResultException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncResult <T, F extends Failure> implements Result<T, F> {

    private final CompletableFuture<DirectResult<T, F>> completableFuture;

    private AsyncResult(CompletableFuture<T> future) {
        this.completableFuture = future.handleAsync((result, throwable) -> {
            if (throwable != null) {
                return DirectResult.failure(throwable);
            }
            return DirectResult.ok(result);
        });
    }

    public static <T, F extends Failure> AsyncResult<T, F> create(CompletableFuture<T> future) {
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
    public T orElse(Function<Result<T, F>, T> function) {
        return getResult().orElse(function);
    }

    @Override
    public Optional<F> failure() {
        return getResult().failure();
    }

    @Override
    public <R> Result<R, F> map(Function<T, R> function) {
        return getResult().map(function);
    }

    @Override
    public <R> Result<R, F> flatMap(Function<T, Result<R, F>> function) {
        return getResult().flatMap(function);
    }

    @Override
    public <R, K extends Throwable> Result<R, F> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        return getResult().flatMap(function, throwableClass);
    }

    @Override
    public Result<T, F> onSuccess(Consumer<T> consumer) {
        return getResult().onSuccess(consumer);
    }

    @Override
    public Result<T, F> onFailure(Consumer<Result<T, F>> consumer) {
        return getResult().onFailure(consumer);
    }

    @Override
    public Optional<T> value() {
        return getResult().value();
    }

    private Result<T, F> getResult() {
        return this.completableFuture.join();
    }
}
