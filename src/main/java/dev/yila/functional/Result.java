package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MultipleFailures;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to store the result value or the failure.
 * This class is immutable and returns a new instance after any modifying operation.
 * @param <T> Type of result value
 */
public interface Result<T> {

    /**
     * Check the current result has failures.
     * @return boolean
     */
    boolean hasFailure();

    /**
     * Get success result value. Throws an exception if hasFailures.
     * Avoid use this method, embrace more functional style with orElse or chaining.
     * @return T
     */
    T getOrThrow();

    /**
     * Return the success result or the execution of the function if it has failures
     * @param function
     * @return T
     */
    T orElse(Function<Result<T>, T> function);

    /**
     *
     * @return
     */
    Optional<Failure> failure();

    /**
     * New result with the execution of the function if is success.
     * @param function
     * @return
     * @param <R>
     */
    <R> Result<R> map(Function<T, R> function);
    
    /**
     * New result with the execution of the function that can throw a check exception.
     * @param function
     * @param throwableClass
     * @return
     * @param <R>
     * @param <K>
     */
    <R, K extends Throwable> Result<R> map(ThrowingFunction<T, R, K> function, Class<K> throwableClass);

    /**
     * Flatten map current result with a function that returns a new result.
     * @param function will be executed if current result is success.
     * @return
     * @param <R>
     */
    <R> Result<R> flatMap(Function<T, Result<R>> function);

    /**
     * Flatten map current result with a Fun that returns a new result.
     * @param fun will be executed if current result is success.
     * @return
     * @param <R>
     */
    <R> Result<R> flatMap(Fun<T, R> fun);

    /**
     * Flatten map current result with a throwing function that returns a new result.
     * @param function
     * @param throwableClass
     * @return
     * @param <R>
     * @param <K>
     */
    <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass);

    /**
     * Execute the consumer if the result is success
     * @param consumer
     * @return
     */
    Result<T> onSuccess(Consumer<T> consumer);

    /**
     * Execute the consumer if the result has failures
     * @param consumer
     * @return
     */
    Result<T> onFailure(Consumer<Failure> consumer);

    /**
     * Get the value if result is success.
     * @return
     */
    Optional<T> value();

    /**
     * Sequence multiple Results in one Result.
     * @param successSequence
     * @param results
     * @return
     * @param <T>
     */
    @SafeVarargs
    static <T> DirectResult<T> sequence(Function<List<T>, T> successSequence, Result<T>... results) {
        List<Failure> failures = Arrays.stream(results)
                .parallel()
                .filter(Result::hasFailure)
                .map(result -> result.failure().get())
                .collect(Collectors.toList());
        if (failures.isEmpty()) {
            T res = successSequence.apply(Arrays.stream(results)
                    .map(Result::getOrThrow)
                    .collect(Collectors.toList()));
            return DirectResult.ok(res);
        } else {
            return DirectResult.failure(new MultipleFailures(failures));
        }
    }

    /**
     * Unwrap optional
     * @param result
     * @return
     * @param <U>
     */
    static <U> Result<U> join(Result<Optional<U>> result) {
        if (result.hasFailure()) {
            return (Result<U>) result;
        } else {
            Optional<U> value = result.getOrThrow();
            return DirectResult.createChecked(value::get, NoSuchElementException.class);
        }
    }
}
