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
 * A failure that contains an error code and description.
 * <p>
 * This failure type is suitable for cases where both a machine-readable error code
 * and a human-readable description are needed, such as REST API error responses
 * or validation errors.
 * <p>
 * Example usage:
 * <pre>{@code
 * Result<String> result = DirectResult.failure(
 *     CodeDescriptionFailure.create("VALIDATION_ERROR", "Email address is invalid")
 * );
 * }</pre>
 *
 * @see Failure#create(String, String)
 */
public class CodeDescriptionFailure implements Failure {

    /**
     * Creates a new CodeDescriptionFailure with the specified error code and description.
     *
     * @param code the error code (e.g., "VALIDATION_ERROR", "NOT_FOUND", "CONFLICT")
     * @param description the error description message
     * @return a new CodeDescriptionFailure
     * @throws NullPointerException if code or description is null
     */
    public static CodeDescriptionFailure create(String code, String description) {
        return new CodeDescriptionFailure(code, description);
    }

    private final String code;
    private final String description;

    /**
     * Constructs a new CodeDescriptionFailure.
     *
     * @param code the error code
     * @param description the error description
     * @throws NullPointerException if code or description is null
     */
    CodeDescriptionFailure(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.code + ": " + this.description;
    }
}
