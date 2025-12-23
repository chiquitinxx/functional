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
 */
public class CodeDescriptionFailure implements Failure {

    /**
     * Creates a new CodeDescriptionFailure.
     * 
     * @param code the error code
     * @param description the error description
     * @return a new CodeDescriptionFailure
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
