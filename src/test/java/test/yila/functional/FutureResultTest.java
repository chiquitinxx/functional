package test.yila.functional;

import dev.yila.functional.FutureResult;
import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class FutureResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");
    private final Failure failure = Failure.create("code", "description");

    @Test
    void createFutureResultFromSupplier() {
        FutureResult<String> result = FutureResult.create(() -> "again");
        assertEquals("again", result.result().get());
    }

    @Test
    void createAFailFutureResult() {
        FutureResult result = FutureResult.failure(failure);
        assertTrue(result.result().hasFailures());
        assertEquals(1, result.result().getFailures().size());
        assertThrows(NoSuchElementException.class, result.result()::get);
    }

    @Test
    void futureFailedWithRuntimeException() {
        FutureResult<String> result = FutureResult.create(() -> {
            throw runtimeException;
        });
        assertTrue(result.result().hasFailures());
        List<Failure> failures = result.result().getFailures();
        assertEquals(runtimeException, ((ThrowableFailure) failures.get(0)).getThrowable());
    }

    @Test
    void multipleFailures() {
        List<Failure> failures = Collections.singletonList(failure);
        FutureResult result = FutureResult.failures(failures);
        assertTrue(result.map((input) -> false).result().hasFailures());
    }

    @Test
    void mapFunction() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<String> upperHello = hello.map(String::toUpperCase);
        assertNotSame(hello, upperHello);
        assertEquals("HELLO", upperHello.result().get());
    }

    @Test
    void exceptionInMapFunction() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<String> failureFuture = hello.map(input -> {
            throw runtimeException;
        });
        assertNotSame(hello, failureFuture);
        assertTrue(failureFuture.result().hasFailures());
        assertSame(runtimeException, ((ThrowableFailure) failureFuture.result().getFailures().get(0)).getThrowable());
    }

    @Test
    void multipleMapFunctions() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<Integer> stringLength = hello
                .map(String::toUpperCase)
                .map(String::toLowerCase)
                .map(String::length);
        assertEquals(5, stringLength.result().get());
    }

    @Test
    void flatMapFunction() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<String> doubleHello = hello.flatMap(s -> FutureResult.create(() -> s + s));
        assertNotSame(hello, doubleHello);
        assertEquals("hellohello", doubleHello.result().get());
    }

    @Test
    void flatMapAfterFailureNotExecuted() {
        FutureResult<String> fail = FutureResult.failure(failure);
        FutureResult<String> afterFail = fail.flatMap(s -> FutureResult.create(() -> s + s));
        assertNotSame(fail, afterFail);
        assertSame(failure, afterFail.result().getFailures().get(0));
    }

    @Test
    void failureInFlatMapFunction() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<String> failureHello = hello.flatMap(s -> FutureResult.failure(failure));
        assertNotSame(hello, failureHello);
        assertSame(failure, failureHello.result().getFailures().get(0));
    }

    @Test
    void exceptionInFlatMapFunction() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<String> exceptionHello = hello.flatMap(s -> FutureResult.create(() -> {
            throw runtimeException;
        }));
        assertNotSame(hello, exceptionHello);
        assertSame(runtimeException, ((ThrowableFailure) exceptionHello.result().getFailures().get(0)).getThrowable());
    }

    @Test
    void multipleMapAndFlatFunctions() {
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        FutureResult<Integer> stringLength = hello
                .flatMap(s -> FutureResult.create(s::toUpperCase))
                .map(String::toLowerCase)
                .flatMap(s -> FutureResult.create(s::length))
                .map(size -> size * size);
        assertEquals(25, stringLength.result().get());
    }

    @Test
    void canNotUseNullAsSupplierOfFunctions() {
        assertThrows(IllegalArgumentException.class, () -> FutureResult.create(null));
        FutureResult<String> hello = FutureResult.create(() -> ("hello"));
        assertThrows(IllegalArgumentException.class, () -> hello.map(null));
        assertThrows(IllegalArgumentException.class, () -> hello.flatMap(null));
    }
}
