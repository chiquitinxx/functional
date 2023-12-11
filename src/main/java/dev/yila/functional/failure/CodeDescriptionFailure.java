package dev.yila.functional.failure;

public class CodeDescriptionFailure implements Failure {

    public static CodeDescriptionFailure create(String code, String description) {
        return new CodeDescriptionFailure(code, description);
    }

    private final String code;
    private final String description;

    CodeDescriptionFailure(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.code + ": " + this.description;
    }
}
