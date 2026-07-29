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
 * A failure that indicates no matcher case was found for a given input value.
 * <p>
 * This failure is returned by {@link dev.yila.functional.Matcher#resultFor(Object)}
 * when no match cases match the input and no default case was provided.
 * <p>
 * Example usage:
 * <pre>{@code
 * Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
 *     .on(i -> i > 0, i -> "positive");
 * 
 * Result<String> result = matcher.resultFor(-1);
 * // result contains a MatcherNotFoundFailure with value -1
 * }</pre>
 *
 * @see dev.yila.functional.Matcher
 */
public class MatcherNotFoundFailure implements Failure {

    private final Object value;

    /**
     * Creates a new MatcherNotFoundFailure for the specified unmatched value.
     *
     * @param value the value that didn't match any case in the matcher
     */
    public MatcherNotFoundFailure(Object value) {
        this.value = value;
    }

    /**
     * Returns the value that didn't match any case.
     *
     * @return the unmatched value
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Not found a matcher for value: " + getValue().toString();
    }
}
