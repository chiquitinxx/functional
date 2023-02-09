package dev.yila.functional.failure;

import java.util.List;

public class LazyResultException extends RuntimeException {

    private final Failure failure;

    public LazyResultException(Failure failure) {
        this.failure = failure;
    }

    public Failure getFailure() {
        return failure;
    }
}
