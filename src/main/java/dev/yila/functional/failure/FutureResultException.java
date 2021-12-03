package dev.yila.functional.failure;

import java.util.List;

public class FutureResultException extends RuntimeException {

    private final List<Failure> failures;

    public FutureResultException(List<Failure> failures) {
        this.failures = failures;
    }

    public List<Failure> getFailures() {
        return failures;
    }
}
