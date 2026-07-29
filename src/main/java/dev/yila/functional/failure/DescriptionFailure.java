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

import java.util.Objects;

/**
 * A failure that contains a descriptive error message.
 * <p>
 * This is the simplest failure type, suitable for cases where only a
 * human-readable error message is needed.
 * <p>
 * Example usage:
 * <pre>{@code
 * Result<String> result = DirectResult.failure(DescriptionFailure.create("Invalid input"));
 * }</pre>
 *
 * @see Failure#create(String)
 */
public class DescriptionFailure implements Failure {

    /**
     * Creates a new DescriptionFailure with the specified error message.
     *
     * @param description the error description message
     * @return a new DescriptionFailure
     * @throws NullPointerException if description is null
     */
    public static DescriptionFailure create(String description) {
        return new DescriptionFailure(description);
    }

    private final String description;

    /**
     * Constructs a new DescriptionFailure.
     *
     * @param description the error description message
     * @throws NullPointerException if description is null
     */
    DescriptionFailure(String description) {
        Objects.requireNonNull(description);
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
