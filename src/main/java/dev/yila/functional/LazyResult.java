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
import dev.yila.functional.failure.LazyResultException;
import dev.yila.functional.failure.ThrowableFailure;

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
 * @param <T> success result type
 */
public class LazyResult<T> implements Result<T> {

    /**
     * Create a lazy result from a supplier.
     * @param supplier
     * @return
     * @param <V>
     */
    public static <V> LazyResult<V> create(Supplier<V> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("null is not a valid supplier to create LazyResult");
        }
        return new LazyResult<>(supplier);
    }

    private final Supplier<T> supplier;
    private CompletableFuture<DirectResult<T>> completableFuture;

    private LazyResult(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public <V> Result<V> map(Function<T, V> function) {
        if (function == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.map");
        }
        return new LazyResult<>(() -> function.apply(this.supplier.get()));
    }

    @Override
    public <R, K extends Throwable> Result<R> map(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        return new LazyResult<>(() -> {
            try {
                return function.apply(this.supplier.get());
            } catch (Throwable throwable) {
                if (throwableClass.isAssignableFrom(throwable.getClass())) {
                    throw new LazyResultException(new ThrowableFailure(throwable));
                } else {
                    throw new RuntimeException(throwable);
                }
            }
        });
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

    @Override
    public <V> Result<V> flatMap(Function<T, Result<V>> function) {
        Objects.requireNonNull(function);
        return new LazyResult<>(() -> {
            Result<V> value = function.apply(this.supplier.get());
            if (value.hasFailure()) {
                throw new LazyResultException(value.failure().get());
            }
            return value.getOrThrow();
        });
    }

    @Override
    public <V> Result<V> flatMap(Fun<T, V> fun) {
        Objects.requireNonNull(fun);
        return new LazyResult<>(() -> {
            Result<V> value = fun.apply(this.supplier.get());
            if (value.hasFailure()) {
                throw new LazyResultException(value.failure().get());
            }
            return value.getOrThrow();
        });
    }

    @Override
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
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
    public T orElse(Function<Result<T>, T> function) {
        return getResult().orElse(function);
    }

    @Override
    public Optional<Failure> failure() {
        return getResult().failure();
    }

    private synchronized CompletableFuture<DirectResult<T>> execute() {
        if (completableFuture == null) {
            completableFuture = toCompletableResult(this.supplier);
        }
        return completableFuture;
    }

    private Result<T> getResult() {
        return execute().join();
    }

    private static <T, F extends Failure> CompletableFuture<DirectResult<T>> toCompletableResult(Supplier<T> supplier) {
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
