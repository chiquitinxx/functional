package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LazyResultTest extends ResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");
    private final CodeDescriptionFailure failure = CodeDescriptionFailure.create("code", "description");

    @Test
    void createLazyResultFromSupplier() {
        LazyResult<String, ?> result = LazyResult.create(() -> "again");
        assertEquals("again", result.getOrThrow());
    }

    @Test
    void lazyFailedWithRuntimeException() {
        LazyResult<String, Failure> result = LazyResult.create(() -> {
            throw runtimeException;
        });
        assertTrue(result.hasFailure());
        Failure failure = result.failure().get();
        assertEquals(runtimeException, failure.toThrowable());
    }

    @Test
    void unexpectedExceptionInCheckedFlatMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException();
        };
        ThrowableFailure failure = (ThrowableFailure) number(3)
                        .flatMap(throwingFunction, NullPointerException.class)
                        .failure().get();
        assertEquals("java.lang.RuntimeException", failure.getThrowable().getMessage());
    }

    @Test
    void mapFunction() {
        LazyResult<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> upperHello = hello.map(String::toUpperCase);
        assertNotSame(hello, upperHello);
        assertEquals("HELLO", upperHello.getOrThrow());
    }

    @Test
    void exceptionInMapFunction() {
        LazyResult<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> failureLazy = hello.map(input -> {
            throw runtimeException;
        });
        assertNotSame(hello, failureLazy);
        assertTrue(failureLazy.hasFailure());
        assertSame(runtimeException, failureLazy.failure().get().toThrowable());
    }

    @Test
    void multipleMapFunctions() {
        LazyResult<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<Integer, Failure> stringLength = hello
                .map(String::toUpperCase)
                .map(String::toLowerCase)
                .map(String::length);
        assertEquals(5, stringLength.getOrThrow());
    }

    @Test
    void flatMapFunction() {
        LazyResult<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> doubleHello = hello.flatMap(s -> LazyResult.create(() -> s + s));
        assertNotSame(hello, doubleHello);
        assertEquals("hellohello", doubleHello.getOrThrow());
    }

    @Test
    void failureAfterMap() {
        Result<String, Failure> fail = DirectResult.failure(failure);
        Result<String, Failure> afterFail = fail.flatMap(s -> LazyResult.create(() -> s + s));
        assertNotSame(fail, afterFail);
        assertSame(failure, afterFail.failure().get());
        Result<String, Failure> lastFail = afterFail.map(s -> s + s);
        assertSame(failure, lastFail.failure().get());
    }

    @Test
    void failureInFlatMapFunction() {
        LazyResult<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> failureHello = hello
                .flatMap(s -> DirectResult.failure(failure));
        assertNotSame(hello, failureHello);
        assertSame(failure, failureHello.failure().get());
    }

    @Test
    void exceptionInFlatMapFunction() {
        Result<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> exceptionHello = hello.flatMap(s -> LazyResult.create(() -> {
            throw runtimeException;
        }));
        assertNotSame(hello, exceptionHello);
        assertSame(runtimeException, exceptionHello.failure().get().toThrowable());
    }

    @Test
    void failureOnCheckedFlatMap() {
        Result<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> exceptionHello = hello.flatMap(s -> {
            throw new NullPointerException();
        }, NullPointerException.class);
        assertTrue(exceptionHello.failure().get().toThrowable() instanceof NullPointerException);
    }

    @Test
    void successOnCheckedFlatMap() {
        Result<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<String, Failure> exceptionHello = hello.flatMap(s -> s.toUpperCase(), NullPointerException.class);
        assertEquals("HELLO", exceptionHello.getOrThrow());
    }

    private int add(int number) {
        return add(number);
    }

    @Test
    void generateStackOverflowError() {
        Result<Integer, Failure> hello = LazyResult.create(() -> add(3));
        assertTrue(hello.failure().get().toThrowable() instanceof StackOverflowError);
    }

    @Test
    void multipleMapAndFlatFunctions() {
        Result<String, Failure> hello = LazyResult.create(() -> ("hello"));
        Result<Integer, Failure> stringLength = hello
                .flatMap(s -> LazyResult.create(s::toUpperCase))
                .map(String::toLowerCase)
                .flatMap(s -> LazyResult.create(s::length))
                .map(size -> size * size);
        assertEquals(25, stringLength.getOrThrow());
    }

    @Test
    void canNotUseNullAsSupplierOfFunctions() {
        assertThrows(IllegalArgumentException.class, () -> LazyResult.create(null));
        Result<String, Failure> hello = LazyResult.create(() -> ("hello"));
        assertThrows(IllegalArgumentException.class, () -> hello.map(null));
        assertThrows(NullPointerException.class, () -> hello.flatMap(null));
    }

    @Test
    void canStartTheExecution() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Result<Integer, Failure> hello = LazyResult
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
        assertEquals(0, atomicInteger.get());
        assertEquals(5, hello.getOrThrow());
        assertEquals(1, atomicInteger.get());
    }

    @Test
    void startAtSameTime() {
        Result<Integer, Failure> hello = LazyResult
                .create(() -> ("hello"))
                .map(String::length);
        assertEquals(hello.getOrThrow(), hello.getOrThrow());
    }

    @Override
    Result<Integer, Failure> number(Integer integer) {
        return LazyResult.create(() -> integer);
    }

    @Override
    Result<String, Failure> string(String string) {
        return LazyResult.create(() -> string);
    }

    @Override
    Result<Integer, Failure> failure(Failure failure) {
        return DirectResult.failure(failure);
    }

    @Override
    Result<Integer, Failure> failure(Throwable throwable) {
        return DirectResult.failure(throwable);
    }
}
