/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.yila.functional;

import dev.yila.functional.failure.Failure;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncResult <T> implements Result<T> {

    private final CompletableFuture<Result<T>> completableFuture;
    private final Executor executor;

    private AsyncResult(Executor executor, CompletableFuture<T> future, Class<? extends Throwable> throwableClass) {
        this.executor = executor;
        this.completableFuture = future.handleAsync((result, throwable) -> {
            if (throwable != null) {
                Throwable cause = throwable.getCause();
                if (throwableClass.isAssignableFrom(cause.getClass())) {
                    return DirectResult.failure(cause);
                } else {
                    throw new CompletionException(cause);
                }
            }
            return DirectResult.ok(result);
        }, executor);
    }

    private AsyncResult(Executor executor, Supplier<T> supplier) {
        this.executor = executor;
        this.completableFuture = CompletableFuture.supplyAsync(supplier, executor)
                .handleAsync((result, throwable) -> {
            if (throwable != null) {
                return DirectResult.failure(throwable.getCause());
            }
            return DirectResult.ok(result);
        }, executor);
    }

    private AsyncResult(Executor executor, CompletableFuture<Result<T>> future) {
        this.executor = executor;
        this.completableFuture = future.handleAsync((value, throwable) -> {
            if (throwable != null) {
                return DirectResult.failure(throwable);
            }
            return value;
        }, executor);
    }

    public static <T> AsyncResult<T> create(Executor executor, Supplier<T> supplier) {
        return new AsyncResult<>(executor, supplier);
    }

    public static <T, K extends Throwable> AsyncResult<T> createChecked(Executor executor, ThrowingSupplier<T, K> throwingSupplier, Class<K> throwableClass) {
        return new AsyncResult<>(executor, CompletableFuture.supplyAsync(() -> {
            try {
                return throwingSupplier.get();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        }, executor), throwableClass);
    }

    public static <T, F, S> Result<T> inParallel(Executor executor, BiFunction<F, S, Result<T>> joinFunction, Supplier<F> first, Supplier<S> second) {
        return inParallel(executor, joinFunction, first, second, 30, TimeUnit.SECONDS);
    }

    public static <T, F, S> Result<T> inParallel(Executor executor, BiFunction<F, S, Result<T>> joinFunction, Supplier<F> first, Supplier<S> second, long timeOut, TimeUnit timeUnit) {
        ScheduledExecutorService scheduler = TimeoutScheduler.getInstance();
        CompletableFuture<Void> timeoutFuture = new CompletableFuture<>();
        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            timeoutFuture.completeExceptionally(new TimeoutException("AsyncResult inParallel timeOut"));
        }, timeOut, timeUnit);

        CompletableFuture<F> cfFirst = CompletableFuture.supplyAsync(first, executor);
        CompletableFuture<S> cfSecond = CompletableFuture.supplyAsync(second, executor);

        CompletableFuture<Result<T>> result = cfFirst.thenCombine(cfSecond, joinFunction);

        timeoutFuture.whenComplete((r, ex) -> {
            if (ex != null) {
                result.completeExceptionally(ex);
            }
        });

        result.whenComplete((r, ex) -> {
           if (!timeoutTask.isDone()) {
               timeoutTask.cancel(true);
           }
        });

        return new AsyncResult<>(executor, result);
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
        CompletableFuture<Result<R>> cf = this.completableFuture
                .thenApply(r -> r.map(function));
        return new AsyncResult<>(this.executor, cf);
    }

    @Override
    public <R, K extends Throwable> Result<R> map(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        CompletableFuture<Result<R>> cf = this.completableFuture
                .thenApply(r -> r.map(function, throwableClass));
        return new AsyncResult<>(this.executor, cf);
    }

    @Override
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        CompletableFuture<Result<R>> cf = this.completableFuture
                .thenApply(r -> r.flatMap(function));
        return new AsyncResult<>(this.executor, cf);
    }

    @Override
    public <R> Result<R> flatMap(Fun<T, R> fun) {
        CompletableFuture<Result<R>> cf = this.completableFuture
                .thenApply(r -> r.flatMap(fun));
        return new AsyncResult<>(this.executor, cf);
    }

    @Override
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        CompletableFuture<Result<R>> cf = this.completableFuture
                .thenApply(r -> r.map(function, throwableClass));
        return new AsyncResult<>(this.executor, cf);
    }

    @Override
    public Result<T> onSuccess(Consumer<T> consumer) {
        this.completableFuture.thenAccept(result -> result.onSuccess(consumer));
        return this;
    }

    @Override
    public Result<T> onFailure(Consumer<Failure> consumer) {
        this.completableFuture.whenComplete((r, t) -> {
            if (t != null) {
                consumer.accept(Failure.create(t));
            } else {
                r.failure().ifPresent(consumer::accept);
            }
        });
        return this;
    }

    @Override
    public Optional<T> value() {
        return getResult().value();
    }

    private Result<T> getResult() {
        return this.completableFuture.join();
    }
}
