package dev.yila.functional;

@FunctionalInterface
public interface ThrowingFunctionException<T, R, E extends Throwable> {
    R apply(T input) throws E;
}
