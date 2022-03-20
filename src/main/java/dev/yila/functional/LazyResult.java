package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.LazyResultException;
import dev.yila.functional.failure.ThrowableFailure;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LazyResult executes suppliers and consecutive functions asynchronously.
 * The result is lazy, and only start computing after result() is called.
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
        return new LazyResult(Collections.singletonList(failure));
    }

    public static LazyResult failures(List<Failure> failures) {
        return new LazyResult(failures);
    }

    private final Supplier<CompletableFuture<T>> supplier;
    private final List<Failure> failures;
    private Result<T> result;
    private CompletableFuture<Result<T>> completableFuture;

    private LazyResult(Supplier<T> supplier) {
        this.supplier = () -> CompletableFuture.supplyAsync(supplier);
        this.failures = null;
    }

    private LazyResult(Supplier<CompletableFuture<T>> supplier, List<Failure> failures) {
        this.supplier = supplier;
        this.failures = failures;
    }

    private LazyResult(List<Failure> failures) {
        this.failures = failures;
        this.supplier = null;
    }

    public Result<T> result() {
        return getResult();
    }

    public <V> LazyResult<V> map(Function<T, V> function) {
        if (function == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.map");
        }
        if (this.failures != null) {
            return LazyResult.failures(this.failures);
        } else {
            return joinWithFunction(function);
        }
    }

    public <V> LazyResult<V> flatMap(Function<T, LazyResult<V>> lazyResultFunction) {
        if (lazyResultFunction == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.flatMap");
        }
        if (this.failures != null) {
            return LazyResult.failures(this.failures);
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
        return new LazyResult<>(() -> this.supplier.get().thenApplyAsync(function), this.failures);
    }

    private <V> LazyResult<V> joinWithLazyResultFunction(Function<T, LazyResult<V>> lazyResultFunction) {
        return new LazyResult<V>(() -> this.supplier.get().thenApplyAsync(lazyResultFunction)
                .thenApplyAsync(this::extract), this.failures);
    }

    private <V> V extract(LazyResult<V> lazyResult) {
        Result<V> result = lazyResult.getResult();
        if (result.hasFailures()) {
            throw new LazyResultException(result.getFailures());
        } else {
            return result.get();
        }
    }

    private Result<T> getResult() {
        synchronized (this) {
            if (this.result == null) {
                if (this.failures != null) {
                    this.result = Result.failures(this.failures);
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
                    return Result.failures(((LazyResultException) throwable.getCause()).getFailures());
                } else {
                    return Result.failure(new ThrowableFailure(completionException.getCause()));
                }
            }
            return Result.ok(result);
        });
    }
}
