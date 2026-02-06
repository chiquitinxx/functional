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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A failure that contains multiple individual failures.
 */
public class MultipleFailures implements Failure {

    private final List<Failure> failures;

    /**
     * Creates a new MultipleFailures from a list of failures.
     * 
     * @param failures the list of failures
     */
    public MultipleFailures(List<Failure> failures) {
        this.failures = failures;
    }

    /**
     * Creates a new MultipleFailures from multiple failure objects.
     * 
     * @param failures the failure objects
     */
    public MultipleFailures(Failure... failures) {
        this.failures = Arrays.asList(failures);
    }

    @Override
    public String toString() {
        return showFailures(Failure::toString);
    }

    /**
     * Returns the list of individual failures.
     * 
     * @return the list of failures
     */
    public List<Failure> getFailures() {
        return failures;
    }

    @Override
    public Exception toException() {
        return this.failures.stream().skip(1)
                .map(Failure::toException)
                .reduce(this.failures.get(0).toException(), (all, current) -> {
                    all.addSuppressed(current);
                    return all;
                });
    }

    private String showFailures(Function<Failure, String> failureToString) {
        return "[" + this.failures.stream()
                .map(failureToString)
                .collect(Collectors.joining(", ")) +
                "]";
    }
}
