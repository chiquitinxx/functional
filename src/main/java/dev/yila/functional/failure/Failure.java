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
 * Represents a failure in a functional operation.
 * <p>
 * Failures provide a structured way to represent and handle errors in functional
 * programming contexts. Unlike exceptions, failures are values that can be composed,
 * transformed, and passed around like any other value.
 * <p>
 * This library provides several failure implementations:
 * <ul>
 *   <li>{@link DescriptionFailure} - a simple failure with a descriptive message</li>
 *   <li>{@link CodeDescriptionFailure} - a failure with an error code and message</li>
 *   <li>{@link ExceptionFailure} - a failure wrapping an exception</li>
 *   <li>{@link MultipleFailures} - a failure containing multiple individual failures</li>
 * </ul>
 * <p>
 * Failures can be created from:
 * <ul>
 *   <li>Exceptions (using {@link #create(Exception)})</li>
 *   <li>Error messages (using {@link #create(String)})</li>
 *   <li>Error codes with messages (using {@link #create(String, String)})</li>
 * </ul>
 *
 * @see DescriptionFailure
 * @see CodeDescriptionFailure
 * @see ExceptionFailure
 * @see MultipleFailures
 */
public interface Failure {

    /**
     * Creates a failure from an exception.
     * <p>
     * The resulting failure will wrap the exception and can be converted back
     * to an exception using {@link #toException()}.
     *
     * @param t the exception to wrap
     * @return a new ExceptionFailure containing the exception
     * @throws NullPointerException if the exception is null
     */
    static Failure create(Exception t) {
        return new ExceptionFailure(t);
    }

    /**
     * Creates a failure with a descriptive message.
     * <p>
     * Use this for simple error cases where only a human-readable message is needed.
     *
     * @param message the error message describing the failure
     * @return a new DescriptionFailure with the message
     * @throws NullPointerException if the message is null
     */
    static Failure create(String message) {
        return new DescriptionFailure(message);
    }

    /**
     * Creates a failure with an error code and message.
     * <p>
     * Use this for error cases where both a machine-readable code and a
     * human-readable message are needed, such as API error responses.
     *
     * @param code the error code (e.g., "VALIDATION_ERROR", "NOT_FOUND")
     * @param message the error message describing the failure
     * @return a new CodeDescriptionFailure with the code and message
     * @throws NullPointerException if code or message is null
     */
    static Failure create(String code, String message) {
        return new CodeDescriptionFailure(code, message);
    }

    /**
     * Converts this failure to an Exception.
     * <p>
     * For {@link ExceptionFailure}, returns the wrapped exception directly.
     * For other failure types, wraps the failure's string representation in a new Exception.
     * <p>
     * This is useful when you need to throw an exception from a failure,
     * such as in the {@link Result#getOrThrow()} method.
     *
     * @return an exception representation of this failure
     */
    default Exception toException() {
        return switch (this) {
            case ExceptionFailure ef -> ef.getException();
            default -> new Exception(this.toString());
        };
    }
}
