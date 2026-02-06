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
 * Represents a failure in a functional operation. Failures can be created
 * from exceptions, error messages, or error codes with messages.
 */
public interface Failure {

    /**
     * Creates a failure from an exception.
     * 
     * @param t the exception to wrap
     * @return a new failure
     */
    static Failure create(Exception t) {
        return new ExceptionFailure(t);
    }

    /**
     * Creates a failure with a descriptive message.
     * 
     * @param message the error message
     * @return a new failure
     */
    static Failure create(String message) {
        return new DescriptionFailure(message);
    }

    /**
     * Creates a failure with an error code and message.
     * 
     * @param code the error code
     * @param message the error message
     * @return a new failure
     */
    static Failure create(String code, String message) {
        return new CodeDescriptionFailure(code, message);
    }

    /**
     * Converts this failure to an Exception.
     * 
     * @return an exception representation of this failure
     */
    default Exception toException() {
        if (this instanceof ExceptionFailure) {
            return ((ExceptionFailure)this).getException();
        } else {
            return new Exception(this.toString());
        }
    }
}
