package dev.yila.functional.failure;

import java.util.List;

public class LazyResultException extends RuntimeException {

    private final List<Failure> failures;

    public LazyResultException(List<Failure> failures) {
        this.failures = failures;
    }

    public List<Failure> getFailures() {
        return failures;
    }
}
