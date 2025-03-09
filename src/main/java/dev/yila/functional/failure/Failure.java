package dev.yila.functional.failure;

public interface Failure {

    static Failure create(Throwable t) {
        return new ThrowableFailure(t);
    }

    static Failure create(String message) {
        return new DescriptionFailure(message);
    }

    static Failure create(String code, String message) {
        return new CodeDescriptionFailure(code, message);
    }

    default Throwable toThrowable() {
        if (this instanceof ThrowableFailure) {
            return ((ThrowableFailure)this).getThrowable();
        } else {
            return new Throwable(this.toString());
        }
    }
}
