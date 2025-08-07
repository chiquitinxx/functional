package dev.yila.functional;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function with one argument.
 * @param <I> input
 * @param <O> output
 */
public class Fun<I, O> {

    private final Function<I, O> function;
    private final ThrowingFunction<I, O, ? extends Throwable> throwingFunction;
    private final Class<? extends Throwable> allowedException;

    private Fun(Function<I, O> function) {
        Objects.requireNonNull(function);
        this.function = function;
        this.allowedException = null;
        this.throwingFunction = null;
    }

    private Fun(ThrowingFunction<I, O, ? extends Throwable> throwingFunction, Class<? extends Throwable> allowedException) {
        Objects.requireNonNull(throwingFunction);
        Objects.requireNonNull(allowedException);
        this.function = null;
        this.allowedException = allowedException;
        this.throwingFunction = throwingFunction;
    }

    public static <Input,Output> Fun<Input, Output> from(Function<Input, Output> function) {
        return new Fun<>(function);
    }

    public static <Input,Output, Ex extends Throwable> Fun<Input, Output> from(ThrowingFunction<Input, Output, Ex> function, Class<Ex> throwableClass) {
        return new Fun<>(function, throwableClass);
    }

    public static <Input,Middle,Output> Fun<Input, Output> compose(Fun<Input, Middle> first, Fun<Middle, Output> second) {
        return new Fun<>(first.function.andThen(second.function));
    }

    public Result<O> apply(I i) {
        if (allowedException != null) {
            try {
                return DirectResult.ok(throwingFunction.apply(i));
            } catch (Throwable throwable) {
                if (allowedException.isAssignableFrom(throwable.getClass())) {
                    return DirectResult.failure(throwable);
                } else {
                    throw new RuntimeException(throwable);
                }
            }
        } else {
            return DirectResult.ok(function.apply(i));
        }
    }
}
