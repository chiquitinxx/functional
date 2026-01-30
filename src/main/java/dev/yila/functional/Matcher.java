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

import dev.yila.functional.failure.MatcherNotFoundFailure;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A pattern matcher that evaluates input values against a series of conditions
 * and returns corresponding output values. This implements a functional pattern
 * matching approach similar to switch-case but with more flexibility.
 * 
 * @param <I> the type of input values to match
 * @param <O> the type of output values produced by successful matches
 */
public class Matcher<I, O> {

    /**
     * Creates a new empty matcher for the specified input and output types.
     * 
     * @param input the input type class
     * @param outputClass the output type class
     * @param <Input> the input type
     * @param <Output> the output type
     * @return a new empty matcher
     */
    public static <Input, Output> Matcher<Input, Output> create(Class<Input> input, Class<Output> outputClass) {
        return new Matcher<>(input, outputClass);
    }

    /**
     * Creates a new matcher with an initial match case.
     * 
     * @param matchFunction the function that determines if this case matches
     * @param outputFunction the function that produces the output for matching inputs
     * @param <Input> the input type
     * @param <Output> the output type
     * @return a new matcher with the initial case
     */
    public static <Input, Output> Matcher<Input, Output> from(Function<Input, Boolean> matchFunction, Function<Input, Output> outputFunction) {
        return new Matcher<>(matchFunction, outputFunction);
    }

    /**
     * Creates a function that tests for equality with the specified input.
     * 
     * @param input the value to compare against
     * @param <Input> the input type
     * @return a function that returns true if the input equals the specified value
     */
    public static <Input> Function<Input, Boolean> EQ(Input input) {
        return value -> value.equals(input);
    }

    /**
     * Creates a function that tests for inequality with the specified input.
     * 
     * @param input the value to compare against
     * @param <Input> the input type
     * @return a function that returns true if the input does not equal the specified value
     */
    public static <Input> Function<Input, Boolean> NEQ(Input input) {
        return value -> !value.equals(input);
    }

    private final List<Pair<Function<I, Boolean>, Function<I, O>>> matchers;
    private Function<I, DirectResult<O>> defaultCase = (input) -> DirectResult.failure(new MatcherNotFoundFailure(input));

    private Matcher(Class<I> inputClass, Class<O> outputClass) {
        this.matchers = Collections.EMPTY_LIST;
    }

    private Matcher(List<Pair<Function<I, Boolean>, Function<I, O>>> matchers, Function<I, O> function) {
        this.matchers = matchers;
        this.defaultCase = (input) -> DirectResult.ok(function.apply(input));
    }

    private Matcher(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        this.matchers = Collections.singletonList(new Pair<>(matchFunction, outputFunction));
    }

    private Matcher(List<Pair<Function<I, Boolean>, Function<I, O>>> matchers,
                    Pair<Function<I, Boolean>, Function<I, O>> pair,
                    Function<I, DirectResult<O>> defaultCase) {
        this.matchers = Stream.concat(matchers.stream(), Stream.of(pair)).collect(Collectors.toList());
        this.defaultCase = defaultCase;
    }

    /**
     * Adds a new match case to this matcher.
     * 
     * @param matchFunction the function that determines if this case matches
     * @param outputFunction the function that produces the output for matching inputs
     * @return a new matcher with the additional case
     */
    public Matcher<I, O> on(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        return new Matcher<>(this.matchers, Pair.of(matchFunction, outputFunction), this.defaultCase);
    }

    /**
     * Adds a default case that matches any input not matched by previous cases.
     * This case will always be evaluated last.
     * 
     * @param function the function that produces the output for unmatched inputs
     * @return a new matcher with the default case
     */
    public Matcher<I, O> orElse(Function<I, O> function) {
        return new Matcher<>(this.matchers, function);
    }

    /**
     * Evaluates the given input against all match cases and returns the result.
     * 
     * @param input the input value to match
     * @return a DirectResult containing either the output from the first matching case
     *         or a failure if no cases matched
     * @throws IllegalArgumentException if input is null
     */
    public DirectResult<O> resultFor(I input) {
        if (input == null) {
            throw new IllegalArgumentException("null is not allowed for as input");
        }
        return getMatchingFunction(input)
                .map(fun -> DirectResult.ok(fun.apply(input)))
                .orElseGet(() -> defaultCase.apply(input));
    }

    private Optional<Function<I, O>> getMatchingFunction(I input) {
        return matchers.stream()
                    .filter(pair -> pair.getLeft().apply(input))
                    .findFirst()
                    .map(Pair::getRight);
    }
}
