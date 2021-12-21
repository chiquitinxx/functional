package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MatcherNotFoundFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Matcher<I, O> {

    public static <Input, Output> Matcher<Input, Output> create(Input input, Class<Output> outputClass) {
        return new Matcher<>(input, outputClass);
    }

    private final I input;
    private final Class<O> outputClass;
    private List<Pair<Function<I, Boolean>, Function<I, O>>> matchers;
    private Optional<Function<I, O>> matchingFunction;
    private Function<I, Result<O>> defaultCase = (input) -> Result.failure(new MatcherNotFoundFailure(input));

    private Matcher(I input, Class<O> outputClass) {
        if (input == null) {
            throw new IllegalArgumentException("null is not allowed for as input of a Matcher");
        }
        this.input = input;
        this.outputClass = outputClass;
        this.matchers = new ArrayList<>();
    }

    public Matcher<I, O> on(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        matchers.add(Pair.of(matchFunction, outputFunction));
        return this;
    }

    public Result<O> orElse(Function<I, O> function) {
        return getMatchingFunction()
                .map(fun -> Result.ok(fun.apply(input)))
                .orElseGet(() -> Result.ok(function.apply(input)));
    }

    public Result<O> get() {
        return getMatchingFunction()
                .map(fun -> Result.ok(fun.apply(input)))
                .orElseGet(() -> defaultCase.apply(input));
    }

    private Optional<Function<I, O>> getMatchingFunction() {
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
