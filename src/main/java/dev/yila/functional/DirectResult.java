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
import dev.yila.functional.failure.MultipleFailures;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to store the direct result value or the failure.
 * This class is immutable and returns a new instance after any modifying operation.
 * @param <T> Type of the value
 */
public class DirectResult<T> implements Result<T> {

    /**
     * Create a new success result.
     * @param value
     * @return
     * @param <T> Type of the value
     */
    public static <T> DirectResult<T> ok(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        return new DirectResult<>(value, null);
    }

    /**
     * Create a new failure result.
     * @param failure
     * @return
     * @param <T>
     */
    public static <T> DirectResult<T> failure(Failure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("Failure can not be null.");
        }
        return new DirectResult<>(null, failure);
    }

    /**
     * Create a new failure result.
     * @param message
     * @return
     * @param <T>
     */
    public static <T> DirectResult<T> failure(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message can not be empty.");
        }
        return new DirectResult<>(null, Failure.create(message));
    }

    /**
     * Create a new failure result from a throwable.
     * @param throwable
     * @return
     * @param <T>
     */
    public static <T> DirectResult<T> failure(Throwable throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("Fail can not be null.");
        }
        return new DirectResult<>(null, Failure.create(throwable));
    }

    /**
     * Create a new failure result.
     * @param failures
     * @return
     * @param <T>
     */
    public static <T> DirectResult<T> failures(List<Failure> failures) {
        if (failures == null || failures.isEmpty()) {
            throw new IllegalArgumentException("Failures list can not be null.");
        }
        return new DirectResult<>(null, new MultipleFailures(failures));
    }

    /**
     * Create a Result from a supplier with a checked exception
     * @param throwingSupplier
     * @param throwableClass
     * @return
     * @param <T>
     * @param <K>
     */
    public static <T, K extends Throwable> DirectResult<T> createChecked(ThrowingSupplier<T, K> throwingSupplier, Class<K> throwableClass) {
        try {
            return new DirectResult<>(throwingSupplier.get(), null);
        } catch (Throwable throwable) {
            if (throwableClass.isAssignableFrom(throwable.getClass())) {
                return DirectResult.failure(throwable);
            } else {
                throw new RuntimeException(throwable);
            }
        }
    }

    private final T value;
    private final Failure failure;

    @Override
    public boolean hasFailure() {
        return failure != null;
    }

    @Override
    public T getOrThrow() {
        if (hasFailure()) {
            throw new NoSuchElementException("Value not present, failure: " + failure.toString());
        }
        return value;
    }

    @Override
    public Optional<Failure> failure() {
        return Optional.ofNullable(this.failure);
    }

    @Override
    public <R> Result<R> map(Function<T, R> function) {
        Objects.requireNonNull(function);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return DirectResult.ok(function.apply(this.value));
        }
    }

    @Override
    public <R, K extends Throwable> Result<R> map(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return createChecked(() -> function.apply(this.getOrThrow()), throwableClass);
        }
    }

    @Override
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        Objects.requireNonNull(function);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return function.apply(this.value);
        }
    }

    @Override
    public <R> Result<R> map(Fun<T, R> fun) {
        Objects.requireNonNull(fun);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return fun.apply(this.value);
        }
    }

    @Override
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, Result<R>, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            try {
                return function.apply(this.value);
            } catch (Throwable throwable) {
                if (throwableClass.isAssignableFrom(throwable.getClass())) {
                    return DirectResult.failure(throwable);
                } else {
                    throw new RuntimeException(throwable);
                }
            }
        }
    }

    @Override
    public Result<T> onSuccess(Consumer<T> consumer) {
        if (!hasFailure()) {
            consumer.accept(this.value);
        }
        return this;
    }

    @Override
    public Result<T> onFailure(Consumer<Failure> consumer) {
        if (hasFailure()) {
            consumer.accept(this.failure);
        }
        return this;
    }

    @Override
    public String toString() {
        return "DirectResult(" + (this.hasFailure() ? "FAILURE" : "OK") + "): " +
                (this.hasFailure() ? this.failure.toString() : value.toString());
    }

    @Override
    public Optional<T> value() {
        if (hasFailure()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    /**
     * Sequence multiple Results in one DirectResult.
     * @param successSequence function to combine the list of results
     * @param results list of results
     * @return DirectResult
     * @param <T> Type of the value
     */
    @SafeVarargs
    public static <T> DirectResult<T> sequence(Function<List<T>, T> successSequence, Result<T>... results) {
        List<Failure> failures = Arrays.stream(results)
                .filter(Result::hasFailure)
                .map(result -> result.failure().get())
                .collect(Collectors.toList());
        if (failures.isEmpty()) {
            T res = successSequence.apply(Arrays.stream(results)
                    .map(Result::getOrThrow)
                    .collect(Collectors.toList()));
            return DirectResult.ok(res);
        } else {
            return DirectResult.failures(failures);
        }
    }

    /**
     * Unwrap optional
     * @param result
     * @return
     * @param <U>
     */
    public static <U> DirectResult<U> join(Result<Optional<U>> result) {
        if (result.hasFailure()) {
            return DirectResult.failure(result.failure().get());
        } else {
            Optional<U> value = result.getOrThrow();
            return DirectResult.createChecked(value::get, NoSuchElementException.class);
        }
    }

    private DirectResult(T value, Failure failure) {
        this.value = value;
        this.failure = failure;
    }
}
