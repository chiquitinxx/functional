package dev.yila.functional.failure;

public class MatcherNotFoundFailure implements Failure {

    private final Object value;

    public MatcherNotFoundFailure(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Not found a matcher for value: " + getValue().toString();
    }
}
