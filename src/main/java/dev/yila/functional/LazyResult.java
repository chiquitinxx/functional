package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.LazyResultException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LazyResult executes suppliers and consecutive functions asynchronously.
 * The result is lazy, and only begins computing after result() or start() is called.
 * @param <T>
 */
public class LazyResult<T, F extends Failure> implements Result<T, F> {

    public static <V, F extends Failure> LazyResult<V, F> create(Supplier<V> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("null is not a valid supplier to create LazyResult");
        }
        return new LazyResult<>(supplier);
    }

    private final Supplier<T> supplier;
    private CompletableFuture<DirectResult<T, F>> completableFuture;

    private LazyResult(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public <V> Result<V, F> map(Function<T, V> function) {
        if (function == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.map");
        }
        return new LazyResult<>(() -> function.apply(this.supplier.get()));
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

    @Override
    public <V> Result<V, F> flatMap(Function<T, Result<V, F>> function) {
        Objects.requireNonNull(function);
        return new LazyResult<>(() -> {
            Result<V, F> value = function.apply(this.supplier.get());
            if (value.hasFailure()) {
                throw new LazyResultException(value.failure().get());
            }
            return value.getOrThrow();
        });
    }

    @Override
    public <R, K extends Throwable> Result<R, F> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        return flatMap((input) -> {
            try {
                return DirectResult.ok(function.apply(input));
            } catch (Throwable throwable) {
                if (throwableClass.isAssignableFrom(throwable.getClass())) {
                    return DirectResult.failure(throwable);
                } else {
                    throw new RuntimeException(throwable);
                }
            }
        });
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

    private synchronized CompletableFuture<DirectResult<T, F>> execute() {
        if (completableFuture == null) {
            completableFuture = toCompletableResult(this.supplier);
        }
        return completableFuture;
    }

    private Result<T, F> getResult() {
        return execute().join();
    }

    private static <T, F extends Failure> CompletableFuture<DirectResult<T, F>> toCompletableResult(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier).handleAsync((result, throwable) -> {
            if (throwable != null) {
                CompletionException completionException = (CompletionException) throwable;
                if (throwable.getCause() instanceof LazyResultException) {
                    return DirectResult.failure(((LazyResultException) throwable.getCause()).getFailure());
                }
                return DirectResult.failure(Failure.create(completionException.getCause()));
            }
            return DirectResult.ok(result);
        });
    }
}
