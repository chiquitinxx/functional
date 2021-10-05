package dev.yila.functional;

@FunctionalInterface
public interface ThrowingSupplierException<T, E extends Throwable> {
    T get() throws E;
}
