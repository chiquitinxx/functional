package dev.yila.functional;

import dev.yila.functional.failure.Failure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Matcher<I, O> {

    public static <Input, Output> Matcher<Input, Output> create(Input input, Class<Output> outputClass) {
        return new Matcher<>(input, outputClass);
    }

    private final I input;
    private List<Pair<Function<I, Boolean>, Function<I, O>>> matchers;
    private Supplier<Result<O>> defaultCase = () -> Result.failure(Failure.create("x", "y"));

    private Matcher(I input, Class<O> outputClass) {
        this.input = input;
        this.matchers = new ArrayList<>();
    }

    public Matcher<I, O> on(Function<I, Boolean> matchFunction, Function<I, O> outputFunction) {
        matchers.add(Pair.of(matchFunction, outputFunction));
        return this;
    }

    public Result<O> orElse(Function<I, O> function) {
        return matchers.stream()
                .filter(pair -> pair.getLeft().apply(input))
                .findFirst()
                .map(pair -> Result.ok(pair.getRight().apply(input)))
                .orElseGet(() -> Result.ok(function.apply(input)));
    }
}
