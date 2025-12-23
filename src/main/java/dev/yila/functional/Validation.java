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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for validating values using multiple validation functions.
 * Each validation can return a specific failure if the validation fails.
 */
public class Validation {

    /**
     * Validates a value against multiple validation functions.
     * Each validation consists of a failure and a predicate function.
     * If any validation fails, the corresponding failure is returned.
     * 
     * @param value the value to validate
     * @param validations pairs of (failure, validation function)
     * @param <T> the type of the value being validated
     * @param <F> the type of failure that extends Failure
     * @return a Result containing the validated value if all validations pass,
     *         or the first failure if any validation fails
     * @throws IllegalArgumentException if value is null or no validations are provided
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
     * Validates a value against a single validation function.
     * 
     * @param value the value to validate
     * @param failure the failure to return if validation fails
     * @param function the validation function that returns true if validation passes
     * @param <T> the type of the value being validated
     * @param <F> the type of failure that extends Failure
     * @return a Result containing the validated value if validation passes,
     *         or the specified failure if validation fails
     * @throws IllegalArgumentException if any parameter is null
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
