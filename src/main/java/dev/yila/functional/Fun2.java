package dev.yila.functional;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A function with two input arguments.
 * @param <I1> input 1
 * @param <I2> input 2
 * @param <O> output
 */
public class Fun2<I1, I2, O> {

    private final BiFunction<I1, I2, O> function;

    public Fun2(BiFunction<I1, I2, O> function) {
        Objects.requireNonNull(function);
        this.function = function;
    }

    public static <Input1, Input2, Output> Fun2<Input1, Input2, Output> from(BiFunction<Input1, Input2, Output> function) {
        return new Fun2<>(function);
    }

    public Result<O> apply(I1 i1, I2 i2) {
        return DirectResult.ok(function.apply(i1, i2));
    }

    public Fun<I2, O> curry(I1 i1) {
        return Fun.from(i2 -> function.apply(i1, i2));
    }
}
