package dev.yila.functional.failure;

public interface Failure {

    static Failure create(Throwable t) {
        return new ThrowableFailure(t);
    }

    default Throwable toThrowable() {
        if (this instanceof ThrowableFailure) {
            return ((ThrowableFailure)this).getThrowable();
        } else {
            return new Throwable(this.toString());
        }
    }
}
