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
package dev.yila.functional.failure;

/**
 * A failure that wraps an Exception.
 * <p>
 * This failure type is used when a functional operation fails due to an exception.
 * The wrapped exception can be retrieved using {@link #getException()} or converted
 * back using {@link Failure#toException()}.
 * <p>
 * Example usage:
 * <pre>{@code
 * try {
 *     // some operation that might throw
 * } catch (IOException e) {
 *     Result<String> result = DirectResult.failure(ExceptionFailure(e));
 * }
 * }</pre>
 *
 * @see Failure#create(Exception)
 */
public class ExceptionFailure implements Failure {

    private final Exception exception;

    /**
     * Creates a new ExceptionFailure wrapping the specified exception.
     *
     * @param exception the exception to wrap
     * @throws NullPointerException if exception is null
     */
    public ExceptionFailure(Exception exception) {
        this.exception = exception;
    }

    /**
     * Returns the wrapped exception.
     *
     * @return the wrapped exception
     */
    public Exception getException() {
        return this.exception;
    }

    @Override
    public String toString() {
        return "ExceptionFailure: " + exception.getClass().getName() + ": " + exception.getMessage();
    }
}
