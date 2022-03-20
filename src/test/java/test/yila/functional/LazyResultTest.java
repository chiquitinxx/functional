package test.yila.functional;

import dev.yila.functional.LazyResult;
import dev.yila.functional.Result;
import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LazyResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");
    private final Failure failure = Failure.create("code", "description");

    @Test
    void createLazyResultFromSupplier() {
        LazyResult<String> result = LazyResult.create(() -> "again");
        assertEquals("again", result.result().get());
    }

    @Test
    void createAFailLazyResult() {
        LazyResult result = LazyResult.failure(failure);
        assertTrue(result.result().hasFailures());
        assertEquals(1, result.result().getFailures().size());
        assertThrows(NoSuchElementException.class, result.result()::get);
    }

    @Test
    void lazyFailedWithRuntimeException() {
        LazyResult<String> result = LazyResult.create(() -> {
            throw runtimeException;
        });
        assertTrue(result.result().hasFailures());
        List<Failure> failures = result.result().getFailures();
        assertEquals(runtimeException, ((ThrowableFailure) failures.get(0)).getThrowable());
    }

    @Test
    void multipleFailures() {
        List<Failure> failures = Collections.singletonList(failure);
        LazyResult result = LazyResult.failures(failures);
        assertTrue(result.map((input) -> false).result().hasFailures());
    }

    @Test
    void mapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<String> upperHello = hello.map(String::toUpperCase);
        assertNotSame(hello, upperHello);
        assertEquals("HELLO", upperHello.result().get());
    }

    @Test
    void exceptionInMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<String> failureLazy = hello.map(input -> {
            throw runtimeException;
        });
        assertNotSame(hello, failureLazy);
        assertTrue(failureLazy.result().hasFailures());
        assertSame(runtimeException, ((ThrowableFailure) failureLazy.result().getFailures().get(0)).getThrowable());
    }

    @Test
    void multipleMapFunctions() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<Integer> stringLength = hello
                .map(String::toUpperCase)
                .map(String::toLowerCase)
                .map(String::length);
        assertEquals(5, stringLength.result().get());
    }

    @Test
    void flatMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<String> doubleHello = hello.flatMap(s -> LazyResult.create(() -> s + s));
        assertNotSame(hello, doubleHello);
        assertEquals("hellohello", doubleHello.result().get());
    }

    @Test
    void flatMapAfterFailureNotExecuted() {
        LazyResult<String> fail = LazyResult.failure(failure);
        LazyResult<String> afterFail = fail.flatMap(s -> LazyResult.create(() -> s + s));
        assertNotSame(fail, afterFail);
        assertSame(failure, afterFail.result().getFailures().get(0));
    }

    @Test
    void failureInFlatMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<String> failureHello = hello.flatMap(s -> LazyResult.failure(failure));
        assertNotSame(hello, failureHello);
        assertSame(failure, failureHello.result().getFailures().get(0));
    }

    @Test
    void exceptionInFlatMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<String> exceptionHello = hello.flatMap(s -> LazyResult.create(() -> {
            throw runtimeException;
        }));
        assertNotSame(hello, exceptionHello);
        assertSame(runtimeException, ((ThrowableFailure) exceptionHello.result().getFailures().get(0)).getThrowable());
    }

    private int add(int number) {
        return add(number);
    }

    @Test
    void generateStackOverflowError() {
        LazyResult<Integer> hello = LazyResult.create(() -> add(3));
        assertTrue(hello.result().getFailuresAsThrowable() instanceof StackOverflowError);
    }

    @Test
    void multipleMapAndFlatFunctions() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        LazyResult<Integer> stringLength = hello
                .flatMap(s -> LazyResult.create(s::toUpperCase))
                .map(String::toLowerCase)
                .flatMap(s -> LazyResult.create(s::length))
                .map(size -> size * size);
        assertEquals(25, stringLength.result().get());
    }

    @Test
    void canNotUseNullAsSupplierOfFunctions() {
        assertThrows(IllegalArgumentException.class, () -> LazyResult.create(null));
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        assertThrows(IllegalArgumentException.class, () -> hello.map(null));
        assertThrows(IllegalArgumentException.class, () -> hello.flatMap(null));
    }

    @Test
    void canStartTheExecution() throws ExecutionException, InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        LazyResult<Integer> hello = LazyResult
                .create(() -> ("hello"))
                .map(string -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    atomicInteger.set(1);
                    return string.length();
                });
        CompletableFuture<Result<Integer>> cf = hello.start();
        assertEquals(0, atomicInteger.get());
        assertEquals(5, cf.get().get());
    }

    @Test
    void startAndGetResultAtSameTime() throws ExecutionException, InterruptedException {
        LazyResult<Integer> hello = LazyResult
                .create(() -> ("hello"))
                .map(String::length);
        CompletableFuture<Result<Integer>> cf = hello.start();
        CompletableFuture<Result<Integer>> cf2 = hello.start();
        Result<Integer> result = hello.result();
        assertSame(result, cf.get());
        assertSame(result, cf2.get());
    }
}
