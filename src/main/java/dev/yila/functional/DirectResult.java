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

    /**
     *
     * @param value
     * @param failure
     * @param function
     * @return
     * @param <T>
     * @param <F>
     */
    public static <T, F extends Failure> Result<T> validate(T value, F failure, Function<T, Boolean> function) {
        if (failure == null) {
            throw new IllegalArgumentException("Failure can not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null.");
        }
        if (function == null) {
            throw new IllegalArgumentException("Function can not be null.");
        }
        if (function.apply(value)) {
            return DirectResult.ok(value);
        }
        return DirectResult.failure(failure);
    }

    /**
     *
     * @param value
     * @param validations
     * @return
     * @param <T>
     * @param <F>
     */
    @SafeVarargs
    public static <T, F extends Failure> Result<T> validate(T value, Pair<F, Function<T, Boolean>>... validations) {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null.");
        }
        if (validations == null || validations.length == 0) {
            throw new IllegalArgumentException("At least one validation is needed.");
        }
        List<Result> results = Arrays.stream(validations)
                .map(pair -> validate(value, pair.getLeft(), pair.getRight()))
                .collect(Collectors.toList());
        return Result.join(list -> list.get(0), results.toArray(new Result[results.size()]));
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
    public T orElse(Function<Result<T>, T> function) {
        if (this.hasFailure()) {
            return function.apply(this);
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
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        Objects.requireNonNull(function);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return function.apply(this.value);
        }
    }

    @Override
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return createChecked(() -> function.apply(this.getOrThrow()), throwableClass);
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
    public Result<T> onFailure(Consumer<Result<T>> consumer) {
        if (hasFailure()) {
            consumer.accept(this);
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

    private DirectResult(T value, Failure failure) {
        this.value = value;
        this.failure = failure;
    }
}
