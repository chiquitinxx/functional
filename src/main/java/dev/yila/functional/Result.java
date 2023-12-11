package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MultipleFailures;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to store the result value or the failure.
 * This class is immutable and returns a new instance after any modifying operation.
 * @param <T> Type of result value
 */
public interface Result<T, F extends Failure> {

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
    T orElse(Function<Result<T, F>, T> function);

    /**
     *
     * @return
     */
    Optional<F> failure();

    /**
     * New result with the execution of the function if is success
     * @param function
     * @param <R>
     * @return Result<R>
     */
    <R> Result<R, F> map(Function<T, R> function);

    /**
     * Flatten map current result with a function that returns a new result.
     * @param function will be executed if current result is success.
     * @param <R>
     * @return Result<R>
     */
    <R> Result<R, F> flatMap(Function<T, Result<R, F>> function);

    /**
     * Flatten map current result with a throwing function that returns a new result.
     * @param function
     * @param throwableClass
     * @return Result<R>
     * @param <R>
     * @param <K>
     */
    <R, K extends Throwable> Result<R, F> flatMap(ThrowingFunction<T, R, K> function, Class<K> throwableClass);

    /**
     * Execute the consumer if the result is success
     * @param consumer
     * @return Result<T>
     */
    Result<T, F> onSuccess(Consumer<T> consumer);

    /**
     * Execute the consumer if the result has failures
     * @param consumer
     * @return Result<T>
     */
    Result<T, F> onFailure(Consumer<Result<T, F>> consumer);

    Optional<T> value();

    /**
     * Join multiple Results in one Result
     * @param results
     * @return Result<List>
     */
    static <T, F extends Failure> DirectResult<T, F> join(Function<List<T>, T> joinOks,
                                                          Result<T, F>... results) {
        List<Failure> failures = Arrays.stream(results)
                .filter(Result::hasFailure)
                .map(result -> result.failure().get())
                .collect(Collectors.toList());
        if (failures.isEmpty()) {
            T res = joinOks.apply(Arrays.stream(results)
                    .map(Result::getOrThrow)
                    .collect(Collectors.toList()));
            return DirectResult.ok(res);
        } else {
            return DirectResult.failure(new MultipleFailures(failures));
        }
    }
}
