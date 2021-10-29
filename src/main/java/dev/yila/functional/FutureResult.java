package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.ThrowableFailure;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class FutureResult<T> {

    public static <V> FutureResult<V> create(CompletableFuture<V> future) {
        return new FutureResult<>(future);
    }

    public static <V> FutureResult<V> create(Supplier<V> supplier) {
        return new FutureResult<>(CompletableFuture.supplyAsync(supplier));
    }

    public static FutureResult failure(Failure failure) {
        return new FutureResult(Collections.singletonList(failure));
    }

    public static FutureResult failures(List<Failure> failures) {
        return new FutureResult(failures);
    }

    private final CompletableFuture<T> future;
    private final List<Failure> failures;
    private Result<T> result;

    private FutureResult(CompletableFuture<T> future) {
        this.future = future;
        this.failures = null;
    }

    private FutureResult(List<Failure> failures) {
        this.failures = failures;
        this.future = null;
    }

    public T get() {
        return getResult().get();
    }

    public boolean hasFailures() {
        return getResult().hasFailures();
    }

    public List<Failure> getFailures() {
        return getResult().getFailures();
    }

    public <V> FutureResult<V> map(Function<T, V> function) {
        if (this.failures != null) {
            return FutureResult.failures(this.failures);
        } else {
            return FutureResult.create(this.future.thenApply(function));
        }
    }

    private Result<T> getResult() {
        synchronized (this) {
            if (this.result == null) {
                if (this.failures != null) {
                    this.result = Result.failures(this.failures);
                } else {
                    CompletableFuture<Result<T>> executed =
                            future.handle((result, throwable) -> {
                                if (throwable != null) {
                                    if (throwable instanceof CompletionException) {
                                        CompletionException completionException = (CompletionException) throwable;
                                        return Result.failure(new ThrowableFailure(completionException.getCause()));
                                    }
                                    return Result.failure(new ThrowableFailure(throwable));
                                }
                                return Result.ok(result);
                            });
                    this.result = executed.join();
                }
            }
            return this.result;
        }
    }
}
