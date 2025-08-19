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

    private final CompletableFuture<DirectResult<T>> completableFuture;

    private AsyncResult(CompletableFuture<T> future, Class<? extends Throwable> throwableClass) {
        this.completableFuture = future.handleAsync((result, throwable) -> {
            if (throwable != null) {
                Throwable cause = throwable.getCause().getCause();
                if (throwableClass.isAssignableFrom(cause.getClass())) {
                    return DirectResult.failure(cause);
                } else {
                    throw new RuntimeException(cause);
                }
            }
            return DirectResult.ok(result);
        });
    }

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

    public static <T, K extends Throwable> AsyncResult<T> createChecked(ThrowingSupplier<T, K> throwingSupplier, Class<K> throwableClass) {
        return createChecked(throwingSupplier, throwableClass, ForkJoinPool.commonPool());
    }

    public static <T, K extends Throwable> AsyncResult<T> createChecked(ThrowingSupplier<T, K> throwingSupplier, Class<K> throwableClass, Executor executor) {
        return new AsyncResult<>(CompletableFuture.supplyAsync(() -> {
            try {
                return throwingSupplier.get();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }, executor), throwableClass);
    }

    /**
     * Execute two tasks in parallel
     * Default timeout is 30 seconds
     * @param joinFunction
     * @param first
     * @param second
     * @return
     * @param <T>
     * @param <F>
     * @param <S>
     */
    public static <T, F, S> Result<T> inParallel(BiFunction<F, S, T> joinFunction, Supplier<F> first, Supplier<S> second) {
        return inParallel(joinFunction, first, second, 30, TimeUnit.SECONDS);
    }

    public static <T, F, S> Result<T> inParallel(BiFunction<F, S, T> joinFunction, Supplier<F> first, Supplier<S> second, long timeOut, TimeUnit timeUnit) {
        return inParallel(joinFunction, first, second, timeOut, timeUnit, ForkJoinPool.commonPool());
    }

    public static <T, F, S> Result<T> inParallel(BiFunction<F, S, T> joinFunction, Supplier<F> first, Supplier<S> second, long timeOut, TimeUnit timeUnit, Executor executor) {
        CompletableFuture timeoutFuture = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            timeoutFuture.completeExceptionally(new TimeoutException("AsyncResult inParallel timeOut"));
        }, timeOut, timeUnit);

        CompletableFuture<F> cfFirst = CompletableFuture.supplyAsync(first, executor)
                .applyToEither(timeoutFuture, Function.identity());
        CompletableFuture<S> cfSecond = CompletableFuture.supplyAsync(second, executor)
                .applyToEither(timeoutFuture, Function.identity());
        CompletableFuture result = cfFirst.thenCombine(cfSecond, joinFunction);
        result.whenComplete((r, ex) -> {
            timeoutFuture.cancel(false);
            scheduler.shutdown();
            if (ex != null) {
                result.completeExceptionally((Throwable) ex);
            }
            result.complete(r);
        });
        return new AsyncResult<>(result);
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
    public <R, K extends Throwable> Result<R> map(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        return getResult().map(function, throwableClass);
    }

    @Override
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        return getResult().flatMap(function);
    }

    @Override
    public <R> Result<R> flatMap(Fun<T, R> fun) {
        return getResult().flatMap(fun);
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
    public Result<T> onFailure(Consumer<Failure> consumer) {
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
