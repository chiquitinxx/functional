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
     * This is a blocking operation.
     * @return boolean
     */
    boolean hasFailure();

    /**
     * Get success result value. Throws an exception if hasFailures.
     * Avoid use this method, embrace more functional style with orElse or chaining.
     * This is a blocking operation.
     * @return T
     */
    T getOrThrow();

    /**
     * Return the success result or the execution of the function if it has failures.
     * This is a blocking operation.
     * @param function
     * @return T
     */
    T orElse(Function<Result<T>, T> function);

    /**
     * Get the failure if exists. It is a blocking operation.
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
    <R> Result<R> map(Fun<T, R> fun);

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
     * This is a blocking operation.
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
                .parallel()
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
            return DirectResult.failure(result.failure().get());
        } else {
            Optional<U> value = result.getOrThrow();
            return DirectResult.createChecked(value::get, NoSuchElementException.class);
        }
    }
}
