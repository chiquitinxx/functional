package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MultipleFailures;
import dev.yila.functional.failure.ThrowableFailure;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class to store the result value or the failure
 * @param <T> Type of result value
 */
public class Result<T> {

    /**
     * Create a new success result.
     * @param value
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> ok(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        return new Result<>(value, null);
    }

    /**
     * Create a new failure result.
     * @param failure
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> failure(Failure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("Fail can not be null.");
        }
        return new Result<>(null, failure);
    }

    /**
     * Create a new failure result from a throwable.
     * @param throwable
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> failure(Throwable throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("Fail can not be null.");
        }
        return new Result<>(null, new ThrowableFailure(throwable));
    }

    /**
     * Create a new failure result.
     * @param failures
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> failures(List<Failure> failures) {
        if (failures == null || failures.size() < 1) {
            throw new IllegalArgumentException("Failures list can not be null.");
        }
        return new Result<>(null, new MultipleFailures(failures));
    }

    /**
     * Create a Result from a supplier.
     * @param supplier
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> create(Supplier<T> supplier) {
        return Result.ok(supplier.get());
    }

    /**
     * Create a Result from a supplier with a checked exception
     * @param throwingSupplier
     * @param throwableClass
     * @param <T>
     * @param <K>
     * @return Result<T>
     */
    public static <T, K extends Throwable> Result<T> createChecked(ThrowingSupplier<T, K> throwingSupplier, Class<K> throwableClass) {
        try {
            return Result.ok(throwingSupplier.get());
        } catch (Throwable throwable) {
            if (throwableClass.isAssignableFrom(throwable.getClass())) {
                return Result.failure(new ThrowableFailure(throwable));
            } else {
                throw new RuntimeException(throwable);
            }
        }
    }

    /**
     * Create a Result from a Supplier that return a Result
     * @param supplier
     * @param <T>
     * @return Result<T>
     */
    public static <T> Result<T> flatCreate(Supplier<Result<T>> supplier) {
        return supplier.get();
    }

    private final T value;
    private final Failure failure;

    /**
     * Join multiple Results in one Result
     * @param results
     * @return Result<List>
     */
    public static Result<List> join(Result... results) {
        List okResults = Arrays.stream(results)
                .filter(result -> !result.hasFailure())
                .map(Result::getOrThrow)
                .collect(Collectors.toList());
        if (okResults.size() == results.length) {
            return Result.ok(okResults);
        } else {
            return Result.failures(joinFailures(Arrays.asList(results)));
        }
    }

    /**
     * Check the current result has failures.
     * @return boolean
     */
    public boolean hasFailure() {
        return failure != null;
    }

    /**
     * Get success result value. Throws an exception if hasFailures.
     * Avoid use this method, embrace more functional style with orElse or chaining.
     * @return T
     */
    public T getOrThrow() {
        if (hasFailure()) {
            throw new NoSuchElementException("Value not present, failure: " + failure.toString());
        }
        return value;
    }

    /**
     * Return the success result or the execution of the function if it has failures
     * @param function
     * @return T
     */
    public T orElse(Function<Result<T>, T> function) {
        if (this.hasFailure()) {
            return function.apply(this);
        }
        return value;
    }

    /**
     * Get failures or null if is success
     * @return List<Failure>
     */
    public Optional<Failure> failure() {
        return Optional.ofNullable(this.failure);
    }

    /**
     * Flatten map current result with a function that returns a new result.
     * @param function will be executed if current result is success.
     * @param <R>
     * @return Result<R>
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        if (hasFailure()) {
            return (Result<R>) this;
        } else {
            return function.apply(this.value);
        }
    }

    /**
     * Flatten map current result with a throwing function that returns a new result.
     * @param function
     * @param throwableClass
     * @return Result<R>
     * @param <R>
     * @param <K>
     */
    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass) {
        if (hasFailure()) {
            return (Result<R>) this;
        } else {
            return createChecked(() -> function.apply(this.value), throwableClass);
        }
    }

    /**
     * New result with the execution of the function if is success
     * @param function
     * @param <R>
     * @return Result<R>
     */
    public <R> Result<R> map(Function<T, R> function) {
        if (hasFailure()) {
            return (Result<R>) this;
        } else {
            return Result.ok(function.apply(this.value));
        }
    }

    /**
     * Execute the consumer if the result is success
     * @param consumer
     * @return Result<T>
     */
    public Result<T> onSuccess(Consumer<T> consumer) {
        if (!hasFailure()) {
            consumer.accept(this.value);
        }
        return this;
    }

    /**
     * Execute the consumer if the result has failures
     * @param consumer
     * @return Result<T>
     */
    public Result<T> onFailure(Consumer<Result<T>> consumer) {
        if (hasFailure()) {
            consumer.accept(this);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Result(" + (this.hasFailure() ? "FAILURE" : "OK") + "): " +
                (this.hasFailure() ? this.failure.toString() : value.toString());
    }

    /**
     * Get value in the result.
     * @return
     */
    public Optional<T> value() {
        if (hasFailure()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private Result(T value, Failure failure) {
        this.value = value;
        this.failure = failure;
    }

    private static List<Failure> joinFailures(List<Result<?>> results) {
        return results.stream()
                .filter(Result::hasFailure)
                .map(result -> result.failure().get())
                .collect(Collectors.toList());
    }
}
