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
 * @param <F> Type of the failure
 */
public class DirectResult<T, F extends Failure> implements Result<T, F> {

    /**
     * Create a new success result.
     * @param value
     * @return
     * @param <T> Type of the value
     * @param <F> Type of the failure
     */
    public static <T, F extends Failure> DirectResult<T, F> ok(T value) {
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
     * @param <F>
     */
    public static <T, F extends Failure> DirectResult<T, F> failure(Failure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("Failure can not be null.");
        }
        return (DirectResult<T, F>) new DirectResult<>(null, failure);
    }

    /**
     * Create a new failure result from a throwable.
     * @param throwable
     * @return
     * @param <T>
     * @param <F>
     */
    public static <T, F extends Failure> DirectResult<T, F> failure(Throwable throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("Fail can not be null.");
        }
        return (DirectResult<T, F>) new DirectResult<>(null, Failure.create(throwable));
    }

    /**
     * Create a new failure result.
     * @param failures
     * @return
     * @param <T>
     * @param <F>
     */
    public static <T, F extends Failure> DirectResult<T, F> failures(List<Failure> failures) {
        if (failures == null || failures.isEmpty()) {
            throw new IllegalArgumentException("Failures list can not be null.");
        }
        return (DirectResult<T, F>) new DirectResult<>(null, new MultipleFailures(failures));
    }

    /**
     * Create a Result from a supplier with a checked exception
     * @param throwingSupplier
     * @param throwableClass
     * @return
     * @param <T>
     * @param <K>
     * @param <F>
     */
    public static <T, K extends Throwable, F extends Failure> DirectResult<T, F> createChecked(ThrowingSupplier<T, K> throwingSupplier, Class<K> throwableClass) {
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
    public static <T, F extends Failure> Result<T, F> validate(T value, F failure, Function<T, Boolean> function) {
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
    public static <T, F extends Failure> Result<T, F> validate(T value, Pair<F, Function<T, Boolean>>... validations) {
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
    private final F failure;

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
    public T orElse(Function<Result<T, F>, T> function) {
        if (this.hasFailure()) {
            return function.apply(this);
        }
        return value;
    }

    @Override
    public Optional<F> failure() {
        return Optional.ofNullable(this.failure);
    }

    @Override
    public <R> Result<R, F> map(Function<T, R> function) {
        Objects.requireNonNull(function);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return DirectResult.ok(function.apply(this.value));
        }
    }

    @Override
    public <R> Result<R, F> flatMap(Function<T, Result<R, F>> function) {
        Objects.requireNonNull(function);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return function.apply(this.value);
        }
    }

    @Override
    public <R, K extends Throwable> Result<R, F> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(throwableClass);
        if (hasFailure()) {
            return DirectResult.failure(this.failure);
        } else {
            return createChecked(() -> function.apply(this.getOrThrow()), throwableClass);
        }
    }

    @Override
    public Result<T, F> onSuccess(Consumer<T> consumer) {
        if (!hasFailure()) {
            consumer.accept(this.value);
        }
        return this;
    }

    @Override
    public Result<T, F> onFailure(Consumer<Result<T, F>> consumer) {
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

    private DirectResult(T value, F failure) {
        this.value = value;
        this.failure = failure;
    }
}
