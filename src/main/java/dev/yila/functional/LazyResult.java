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

    public static <V> LazyResult<V> failure(Failure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("null is not a valid failure to create LazyResult");
        }
        return new LazyResult<>(failure);
    }

    private final Supplier<CompletableFuture<T>> supplier;
    private final Failure failure;
    private Result<T> result;
    private CompletableFuture<T> completableFuture;

    private LazyResult(Supplier<T> supplier) {
        this.supplier = () -> CompletableFuture.supplyAsync(supplier);
        this.failure = null;
    }

    private <S> LazyResult(CompletableFuture<T> completableFuture) {
        this.supplier = () -> completableFuture;
        this.failure = null;
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
        if (hasFailure()) {
            return LazyResult.failure(this.failure);
        } else {
            return new LazyResult<>(this.execute().thenApplyAsync(function));
        }
    }

    public <V> LazyResult<V> flatMap(Function<T, LazyResult<V>> lazyResultFunction) {
        if (lazyResultFunction == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.flatMap");
        }
        if (hasFailure()) {
            return LazyResult.failure(this.failure);
        } else {
            return new LazyResult<>(this.execute()
                    .thenComposeAsync(d -> lazyResultFunction.apply(d).execute()));
        }
    }

    public synchronized CompletableFuture<Result<T>> start() {
        return toCompletableResult(execute());
    }

    private synchronized CompletableFuture<T> execute() {
        if (hasFailure()) {
            return CompletableFuture.supplyAsync(() -> {
                throw new LazyResultException(this.failure);
            });
        } else {
            if (completableFuture == null) {
                completableFuture = this.supplier.get();
            }
            return completableFuture;
        }
    }

    private boolean hasFailure() {
        return this.failure != null;
    }

    private Result<T> getResult() {
        synchronized (this) {
            if (this.result == null) {
                if (hasFailure()) {
                    this.result = Result.failure(this.failure);
                } else {
                    this.result = start().join();
                }
            }
        }
        return this.result;
    }

    private static <U> CompletableFuture<Result<U>> toCompletableResult(CompletableFuture<U> completableFuture) {
        return completableFuture.handleAsync((result, throwable) -> {
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
