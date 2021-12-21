package dev.yila.functional;

import dev.yila.functional.failure.MatcherNotFoundFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Matcher<I, O> {

    public static <Input, Output> Matcher<Input, Output> create(Class<Input> input, Class<Output> outputClass) {
        return new Matcher<>(input, outputClass);
    }

    public static <Input> Function<Input, Boolean> EQ(Input integer) {
        return input -> input.equals(integer);
    }

    private List<Pair<Function<I, Boolean>, Function<I, O>>> matchers;
    private Optional<Function<I, O>> matchingFunction;
    private Function<I, Result<O>> defaultCase = (input) -> Result.failure(new MatcherNotFoundFailure(input));

    private Matcher(Class<I> inputClass, Class<O> outputClass) {
        this.matchers = new ArrayList<>();
    }

    public Matcher<I, O> on(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        matchers.add(Pair.of(matchFunction, outputFunction));
        return this;
    }

    public Matcher<I, O> orElse(Function<I, O> function) {
        matchers.add(Pair.of(input -> true, function));
        return this;
    }

    public Result<O> get(I input) {
        if (input == null) {
            throw new IllegalArgumentException("null is not allowed for as input");
        }
        return getMatchingFunction(input)
                .map(fun -> Result.ok(fun.apply(input)))
                .orElseGet(() -> defaultCase.apply(input));
    }

    private Optional<Function<I, O>> getMatchingFunction(I input) {
        synchronized (this) {
            if (matchingFunction == null) {
                matchingFunction = matchers.stream()
                        .filter(pair -> pair.getLeft().apply(input))
                        .findFirst()
                        .map(Pair::getRight);
            }
        }
        return matchingFunction;
    }
}
