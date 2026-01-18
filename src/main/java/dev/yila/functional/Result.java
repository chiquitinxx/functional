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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * Return the success result or the execution of the supplier if it has failures.
     * @param supplier default value supplier
     * @return T
     */
    default T orElseGet(Supplier<T> supplier) {
        return hasFailure() ? supplier.get() : getOrThrow();
    }

    /**
     * Get the failure if exists.
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
     * Map current result with a Fun that returns a new result.
     * @param fun will be executed if current result is success.
     * @return
     * @param <R>
     */
    <R> Result<R> map(Fun<T, R> fun);

    /**
     * Flatten map current result with a function that returns a new result.
     * @param function will be executed if current result is success.
     * @return
     * @param <R>
     */
    <R> Result<R> flatMap(Function<T, Result<R>> function);

    /**
     * Flatten map current result with a throwing function that returns a new result.
     * @param function
     * @param throwableClass
     * @return
     * @param <R>
     * @param <K>
     */
    <R, K extends Throwable> Result<R> flatMap(ThrowingFunction<T, Result<R>, K> function, Class<K> throwableClass);

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
}
