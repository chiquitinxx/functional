package dev.yila.functional.failure;

public class DescriptionFailure implements Failure {

    public static DescriptionFailure create(String description) {
        return new DescriptionFailure(description);
    }

    private final String description;

    DescriptionFailure(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
