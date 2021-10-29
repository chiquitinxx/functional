package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.ThrowableFailure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Result<T> {

    /**
     * Create a new success result.
     * @param value
     * @param <T>
     * @return
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
     * @return
     */
    public static <T> Result<T> failure(Failure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("Fail can not be null.");
        }
        return new Result<>(null, Collections.singletonList(failure));
    }

    /**
     * Create a new failure result.
     * @param failures
     * @param <T>
     * @return
     */
    public static <T> Result<T> failures(List<Failure> failures) {
        if (failures == null || failures.size() < 1) {
            throw new IllegalArgumentException("Failures list can not be null.");
        }
        return new Result<>(null, Collections.unmodifiableList(failures));
    }

    /**
     * Create a Result from a supplier.
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> Result<T> create(Supplier<T> supplier) {
        return Result.ok(supplier.get());
    }

    public static <T, K extends Throwable> Result<T> createChecked(ThrowingSupplierException<T, K> throwingSupplier, Class<K> throwableClass) {
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

    public static <T> Result<T> flatCreate(Supplier<Result<T>> supplier) {
        return supplier.get();
    }

    private final T value;
    private final List<Failure> failures;

    public static Result<List> join(Result... results) {
        List okResults = Arrays.stream(results)
                .filter(result -> !result.hasFailures())
                .map(Result::get)
                .collect(Collectors.toList());
        if (okResults.size() == results.length) {
            return Result.ok(okResults);
        } else {
            return Result.failures(joinFailures(Arrays.asList(results)));
        }
    }

    /**
     * Check the current result has failures.
     * @return
     */
    public boolean hasFailures() {
        return failures != null;
    }

    /**
     * Get success result value. Throws an exception if hasFailures.
     * @return
     */
    public T get() {
        if (hasFailures()) {
            throw new NoSuchElementException("Value not present");
        }
        return value;
    }

    public T orElse(Function<Result<T>, T> function) {
        if (this.hasFailures()) {
            return function.apply(this);
        }
        return value;
    }

    public List<Failure> getFailures() {
        return this.failures;
    }

    public String getFailuresToString() {
        return "[" + this.failures.stream()
                .map(Failure::toString)
                .collect(Collectors.joining(", ")) +
                "]";
    }

    /**
     * Failure codes as comma separated list.
     * @return
     */
    public String getFailuresCode() {
        return "[" + this.failures.stream()
                .map(Failure::getCode)
                .collect(Collectors.joining(", ")) +
                "]";
    }

    /**
     * Map current result with a function that returns a new result.
     * @param function will be executed if current result is success.
     * @param <R>
     * @return
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> function) {
        if (hasFailures()) {
            return (Result<R>) this;
        } else {
            return function.apply(this.value);
        }
    }

    public <R> Result<R> map(Function<T, R> function) {
        if (hasFailures()) {
            return (Result<R>) this;
        } else {
            return Result.ok(function.apply(this.value));
        }
    }

    public Result<T> onSuccess(Consumer<T> consumer) {
        if (!hasFailures()) {
            consumer.accept(this.value);
        }
        return this;
    }

    public Result<T> onFailures(Consumer<Result<T>> consumer) {
        if (hasFailures()) {
            consumer.accept(this);
        }
        return this;
    }

    public boolean notHasFailure(Class<? extends Failure> failureClass) {
        return !hasFailures() || failures.stream().noneMatch(failureClass::isInstance);
    }

    public boolean hasFailure(Class<? extends Failure> failureClass) {
        return hasFailures() && failures.stream().anyMatch(failureClass::isInstance);
    }

    @Override
    public String toString() {
        return "Result(" + (this.hasFailures() ? "FAILURES" : "OK") + "):" +
                (this.hasFailures() ? this.getFailuresToString() : value.toString());
    }

    private Result(T value, List<Failure> failures) {
        this.value = value;
        this.failures = failures;
    }

    private static List<Failure> joinFailures(List<Result<?>> results) {
        return results.stream()
                .filter(Result::hasFailures)
                .flatMap(result -> result.getFailures().stream())
                .collect(Collectors.toList());
    }
}
