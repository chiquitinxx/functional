package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.LazyResultException;
import dev.yila.functional.failure.ThrowableFailure;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LazyResult executes suppliers and consecutive functions asynchronously.
 * The result is lazy, and only begins computing after result() or start() is called.
 * @param <T>
 */
public class LazyResult<T> {

    public static <V> LazyResult<V> create(Supplier<V> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("null is not a valid supplier to create LazyResult");
        }
        return new LazyResult<>(supplier);
    }

    public static LazyResult failure(Failure failure) {
        return new LazyResult(failure);
    }

    private final Supplier<CompletableFuture<T>> supplier;
    private final Failure failure;
    private Result<T> result;
    private CompletableFuture<Result<T>> completableFuture;

    private LazyResult(Supplier<T> supplier) {
        this.supplier = () -> CompletableFuture.supplyAsync(supplier);
        this.failure = null;
    }

    private LazyResult(Supplier<CompletableFuture<T>> supplier, Failure failure) {
        this.supplier = supplier;
        this.failure = failure;
    }

    private LazyResult(Failure failure) {
        this.failure = failure;
        this.supplier = null;
    }

    public Result<T> result() {
        return getResult();
    }

    public <V> LazyResult<V> map(Function<T, V> function) {
        if (function == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.map");
        }
        if (this.failure != null) {
            return LazyResult.failure(this.failure);
        } else {
            return joinWithFunction(function);
        }
    }

    public <V> LazyResult<V> flatMap(Function<T, LazyResult<V>> lazyResultFunction) {
        if (lazyResultFunction == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.flatMap");
        }
        if (this.failure != null) {
            return LazyResult.failure(this.failure);
        } else {
            return joinWithLazyResultFunction(lazyResultFunction);
        }
    }

    public synchronized CompletableFuture<Result<T>> start() {
        if (completableFuture == null) {
            completableFuture = executeSupplier();
        }
        return completableFuture;
    }

    private <V> LazyResult<V> joinWithFunction(Function<T, V> function) {
        return new LazyResult<>(() -> this.supplier.get().thenApplyAsync(function), this.failure);
    }

    private <V> LazyResult<V> joinWithLazyResultFunction(Function<T, LazyResult<V>> lazyResultFunction) {
        return new LazyResult<V>(() -> this.supplier.get().thenApplyAsync(lazyResultFunction)
                .thenApplyAsync(this::extract), this.failure);
    }

    private <V> V extract(LazyResult<V> lazyResult) {
        Result<V> result = lazyResult.getResult();
        if (result.hasFailure()) {
            throw new LazyResultException(result.failure().get());
        } else {
            return result.getOrThrow();
        }
    }

    private Result<T> getResult() {
        synchronized (this) {
            if (this.result == null) {
                if (this.failure != null) {
                    this.result = Result.failure(this.failure);
                } else {
                    this.result = start().join();
                }
            }
        }
        return this.result;
    }

    private CompletableFuture<Result<T>> executeSupplier() {
        return this.supplier.get().handleAsync((result, throwable) -> {
            if (throwable != null) {
                CompletionException completionException = (CompletionException) throwable;
                if (throwable.getCause() instanceof LazyResultException) {
                    return Result.failure(((LazyResultException) throwable.getCause()).getFailure());
                } else {
                    return Result.failure(new ThrowableFailure(completionException.getCause()));
                }
            }
            return Result.ok(result);
        });
    }
}
