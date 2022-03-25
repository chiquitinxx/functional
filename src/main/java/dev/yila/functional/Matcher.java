package dev.yila.functional;

import dev.yila.functional.failure.MatcherNotFoundFailure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Matcher<I, O> {

    public static <Input, Output> Matcher<Input, Output> create(Class<Input> input, Class<Output> outputClass) {
        return new Matcher<>(input, outputClass);
    }

    public static <Input, Output> Matcher<Input, Output> from(Function<Input, Boolean> matchFunction, Function<Input, Output> outputFunction) {
        return new Matcher<>(matchFunction, outputFunction);
    }

    public static <Input> Function<Input, Boolean> EQ(Input input) {
        return value -> value.equals(input);
    }

    public static <Input> Function<Input, Boolean> NEQ(Input input) {
        return value -> !value.equals(input);
    }

    private final List<Pair<Function<I, Boolean>, Function<I, O>>> matchers;
    private Function<I, Result<O>> defaultCase = (input) -> Result.failure(new MatcherNotFoundFailure(input));

    private Matcher(Class<I> inputClass, Class<O> outputClass) {
        this.matchers = Collections.EMPTY_LIST;
    }

    private Matcher(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        this.matchers = Collections.singletonList(new Pair<>(matchFunction, outputFunction));
    }

    private Matcher(List<Pair<Function<I, Boolean>, Function<I, O>>> matchers, Pair<Function<I, Boolean>, Function<I, O>> pair) {
        this.matchers = Stream.concat(matchers.stream(), Stream.of(pair)).collect(Collectors.toList());
    }

    public Matcher<I, O> on(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        return new Matcher<>(this.matchers, Pair.of(matchFunction, outputFunction));
    }

    public Matcher<I, O> orElse(Function<I, O> function) {
        return new Matcher<>(this.matchers, Pair.of(input -> true, function));
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
        return matchers.stream()
                    .filter(pair -> pair.getLeft().apply(input))
                    .findFirst()
                    .map(Pair::getRight);
    }
}
