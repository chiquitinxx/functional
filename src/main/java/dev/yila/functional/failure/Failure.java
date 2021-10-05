package dev.yila.functional.failure;

public interface Failure {

    static Failure create(String code, String description) {
        return new BasicFailure(code, description);
    }

    default String getCode() {
        return this.getClass().getName();
    }

    default String asString() {
        if (this instanceof ThrowableFailure throwableError) {
            Throwable throwable = throwableError.getThrowable();
            return this.getCode() + ":" + throwable.getClass().getCanonicalName() + ":" +
                    throwable.getMessage();
        } else {
            return this.toString();
        }
    }
}
