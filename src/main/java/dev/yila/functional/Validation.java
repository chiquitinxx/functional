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
