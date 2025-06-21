package dev.yila.functional;

import dev.yila.functional.failure.Failure;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Validation {

    /**
     * Validate a value with one or more functions
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
        return Result.sequence(list -> list.get(0), results.toArray(new Result[results.size()]));
    }

    /**
     * Validate a value with one function
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
}
