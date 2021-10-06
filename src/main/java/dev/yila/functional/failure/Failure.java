package dev.yila.functional.failure;

public interface Failure {

    static Failure create(String code, String description) {
        return new BasicFailure(code, description);
    }

    default String getCode() {
        return this.getClass().getName();
    }
}
