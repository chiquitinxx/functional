package dev.yila.functional.failure;

public class BasicFailure implements Failure {

    private final String code;
    private final String description;

    public BasicFailure(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.code + ": " + this.description;
    }
}
