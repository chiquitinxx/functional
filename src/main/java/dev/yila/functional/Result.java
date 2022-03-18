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

/**
 * Class to store the result or the failures
 * @param <T>
 */
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
     * Create a new failure result from a throwable.
     * @param throwable
     * @param <T>
     * @return
     */
    public static <T> Result<T> failure(Throwable throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("Fail can not be null.");
        }
        return new Result<>(null, Collections.singletonList(new ThrowableFailure(throwable)));
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

    /**
     * Create a Result from a supplier with a checked exception
     * @param throwingSupplier
     * @param throwableClass
     * @param <T>
     * @param <K>
     * @return
     */
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

    /**
     * Create a Result from a Supplier that return a Result
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> Result<T> flatCreate(Supplier<Result<T>> supplier) {
        return supplier.get();
    }

    private final T value;
    private final List<Failure> failures;

    /**
     * Join multiple Results in one Result
     * @param results
     * @return
     */
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

    /**
     * Return the success result or the execution of the function if it has failures
     * @param function
     * @return
     */
    public T orElse(Function<Result<T>, T> function) {
        if (this.hasFailures()) {
            return function.apply(this);
        }
        return value;
    }

    /**
     * Get failures or null if is success
     * @return
     */
    public List<Failure> getFailures() {
        return this.failures;
    }

    /**
     * Get failures as String
     * @return
     */
    public String getFailuresToString() {
        return showFailures(Failure::toString);
    }

    /**
     * Failure codes as comma separated list.
     * @return
     */
    public String getFailuresCode() {
        return showFailures(Failure::getCode);
    }

    private String showFailures(Function<Failure, String> failureToString) {
        return "[" + this.failures.stream()
                .map(failureToString)
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

    public <R, K extends Throwable> Result<R> flatMap(ThrowingFunctionException<T, R, K> function, Class<K> throwableClass) {
        if (hasFailures()) {
            return (Result<R>) this;
        } else {
            return createChecked(() -> function.apply(this.value), throwableClass);
        }
    }

    /**
     * New result with the execution of the function if is success
     * @param function
     * @param <R>
     * @return
     */
    public <R> Result<R> map(Function<T, R> function) {
        if (hasFailures()) {
            return (Result<R>) this;
        } else {
            return Result.ok(function.apply(this.value));
        }
    }

    /**
     * Execute the consumer if the result is success
     * @param consumer
     * @return
     */
    public Result<T> onSuccess(Consumer<T> consumer) {
        if (!hasFailures()) {
            consumer.accept(this.value);
        }
        return this;
    }

    /**
     * Execute the consumer if the result has failures
     * @param consumer
     * @return
     */
    public Result<T> onFailures(Consumer<Result<T>> consumer) {
        if (hasFailures()) {
            consumer.accept(this);
        }
        return this;
    }

    /**
     * Check if the result not has a failure of the provided class
     * @param failureClass
     * @return
     */
    public boolean notHasFailure(Class<? extends Failure> failureClass) {
        return !hasFailures() || failures.stream().noneMatch(failureClass::isInstance);
    }

    /**
     * Check if the result has a failure of the provided class
     * @param failureClass
     * @return
     */
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

    /**
     * Get failures as a Throwable
     * @return
     */
    public Throwable getFailuresAsThrowable() {
        if (hasFailures()) {
            return this.failures.stream().skip(1)
                    .map(this::failureToThrowable)
                    .reduce(failureToThrowable(this.failures.get(0)), (all, current) -> {
                        all.addSuppressed(current);
                        return all;
                    });
        } else {
            throw new NoSuchElementException("Result has not failures");
        }
    }

    private Throwable failureToThrowable(Failure failure) {
        if (failure instanceof ThrowableFailure) {
            return ((ThrowableFailure)failure).getThrowable();
        } else {
            return new Throwable(failure.toString());
        }
    }
}
