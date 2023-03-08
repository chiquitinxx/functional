package dev.yila.functional;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T input) throws E;
}
