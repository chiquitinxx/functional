package dev.yila.functional.failure;

public class LazyResultException extends RuntimeException {
    private final Failure failure;

    public LazyResultException(Failure failure) {
        this.failure = failure;
    }

    public Failure getFailure() {
        return this.failure;
    }
}
