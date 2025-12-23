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

/**
 * A functional interface similar to Function but allows throwing checked exceptions.
 * 
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of the exception that may be thrown
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    /**
     * Applies this function to the given argument.
     * 
     * @param input the function argument
     * @return the function result
     * @throws E if the function throws an exception
     */
    R apply(T input) throws E;
}
