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

import java.util.Objects;
import java.util.Optional;
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
        return new LazyResult<>(() -> {
            try {
                return DirectResult.ok(supplier.get());
            } catch (Throwable t) {
                return DirectResult.failure(t);
            }
        });
    }

    private final Supplier<Result<T>> _supplier;
    private volatile Result<T> result;

    private LazyResult(Supplier<Result<T>> supplier) {
        this._supplier = supplier;
    }

    @Override
    public <V> Result<V> map(Function<T, V> function) {
        if (function == null) {
            throw new IllegalArgumentException("null is not a valid function to use LazyResult.map");
        }
        return new LazyResult<>(() -> execute().map(function));
    }

    @Override
    public <R, K extends Throwable> Result<R> map(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        return new LazyResult<>(() -> execute().map(function, throwableClass));
    }

    @Override
    public Result<T> onSuccess(Consumer<T> consumer) {
        return execute().onSuccess(consumer);
    }

    @Override
    public Result<T> onFailure(Consumer<Failure> consumer) {
        return execute().onFailure(consumer);
    }

    @Override
    public Optional<T> value() {
        return execute().value();
    }

    @Override
    public <V> Result<V> flatMap(Function<T, Result<V>> function) {
        Objects.requireNonNull(function);
        return new LazyResult<>(() -> execute().flatMap(function));
    }

    @Override
    public <V> Result<V> map(Fun<T, V> fun) {
        Objects.requireNonNull(fun);
        return new LazyResult<>(() -> execute().map(fun));
    }

    @Override
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, Result<R>, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        return new LazyResult<>(() -> execute().flatMap(function, throwableClass));
    }

    @Override
    public boolean hasFailure() {
        return execute().hasFailure();
    }

    @Override
    public T getOrThrow() {
        return execute().getOrThrow();
    }

    @Override
    public Optional<Failure> failure() {
        return execute().failure();
    }

    private Result<T> execute() {
        if (result == null) {
            synchronized (this) {
                if (result == null) {
                    try {
                        result = this._supplier.get();
                    } catch (Exception e) {
                        result = DirectResult.failure(e);
                    }
                }
            }
        }
        return result;
    }
}
