package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.FutureResultException;
import dev.yila.functional.failure.ThrowableFailure;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * FutureResult executes suppliers and consecutive functions asynchronously.
 * The result is lazy, and only start computing after result() is called.
 * @param <T>
 */
public class FutureResult<T> {

    public static <V> FutureResult<V> create(Supplier<V> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("null is not a valid supplier to create FutureResult");
        }
        return new FutureResult<>(supplier);
    }

    public static FutureResult failure(Failure failure) {
        return new FutureResult(Collections.singletonList(failure));
    }

    public static FutureResult failures(List<Failure> failures) {
        return new FutureResult(failures);
    }

    private final Supplier<CompletableFuture<T>> supplier;
    private final List<Failure> failures;
    private Result<T> result;

    private FutureResult(Supplier<T> supplier) {
        this.supplier = () -> CompletableFuture.supplyAsync(supplier);
        this.failures = null;
    }

    private FutureResult(Supplier<CompletableFuture<T>> supplier, List<Failure> failures) {
        this.supplier = supplier;
        this.failures = failures;
    }

    private FutureResult(List<Failure> failures) {
        this.failures = failures;
        this.supplier = null;
    }

    public Result<T> result() {
        return getResult();
    }

    public <V> FutureResult<V> map(Function<T, V> function) {
        if (function == null) {
            throw new IllegalArgumentException("null is not a valid function to use FutureResult.map");
        }
        if (this.failures != null) {
            return FutureResult.failures(this.failures);
        } else {
            return joinWithFunction(function);
        }
    }

    public <V> FutureResult<V> flatMap(Function<T, FutureResult<V>> futureResultFunction) {
        if (futureResultFunction == null) {
            throw new IllegalArgumentException("null is not a valid function to use FutureResult.flatMap");
        }
        if (this.failures != null) {
            return FutureResult.failures(this.failures);
        } else {
            return joinWithFutureResultFunction(futureResultFunction);
        }
    }

    private <V> FutureResult<V> joinWithFunction(Function<T, V> function) {
        return new FutureResult<>(() -> this.supplier.get().thenApply(function), this.failures);
    }

    private <V> FutureResult<V> joinWithFutureResultFunction(Function<T, FutureResult<V>> futureResultFunction) {
        return new FutureResult<V>(() -> this.supplier.get().thenApply(futureResultFunction)
                .thenApply(this::extract), this.failures);
    }

    private <V> V extract(FutureResult<V> futureResult) {
        Result<V> result = futureResult.getResult();
        if (result.hasFailures()) {
            throw new FutureResultException(result.getFailures());
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
                    this.result = executeSupplier().join();
                }
            }
            return this.result;
        }
    }

    private CompletableFuture<Result<T>> executeSupplier() {
        return this.supplier.get().handle((result, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof CompletionException) {
                    CompletionException completionException = (CompletionException) throwable;
                    if (throwable.getCause() instanceof FutureResultException) {
                        return Result.failures(((FutureResultException) throwable.getCause()).getFailures());
                    } else {
                        return Result.failure(new ThrowableFailure(completionException.getCause()));
                    }
                }
                return Result.failure(new ThrowableFailure(throwable));
            }
            return Result.ok(result);
        });
    }
}
