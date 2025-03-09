package dev.yila.functional.failure;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultipleFailures implements Failure {

    private final List<Failure> failures;

    public MultipleFailures(List<Failure> failures) {
        this.failures = failures;
    }

    public MultipleFailures(Failure... failures) {
        this.failures = Arrays.asList(failures);
    }

    @Override
    public String toString() {
        return showFailures(Failure::toString);
    }

    public List<Failure> getFailures() {
        return failures;
    }

    @Override
    public Throwable toThrowable() {
        return this.failures.stream().skip(1)
                .map(Failure::toThrowable)
                .reduce(this.failures.get(0).toThrowable(), (all, current) -> {
                    all.addSuppressed(current);
                    return all;
                });
    }

    private String showFailures(Function<Failure, String> failureToString) {
        return "[" + this.failures.stream()
                .map(failureToString)
                .collect(Collectors.joining(", ")) +
                "]";
    }
}
