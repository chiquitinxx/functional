package dev.yila.functional.failure;

public class ThrowableFailure implements Failure {

    private final Throwable throwable;

    public ThrowableFailure(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "ThrowableFailure: " + throwable.getClass().getName() + ": " + throwable.getMessage();
    }
}
