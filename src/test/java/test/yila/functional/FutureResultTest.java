package test.yila.functional;

import dev.yila.functional.FutureResult;
import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FutureResultTest {

    @Test
    void createFutureResultFromCompletableFuture() {
        FutureResult<String> result = FutureResult.create(CompletableFuture.completedFuture("hello"));
        assertEquals("hello", result.get());
    }

    @Test
    void createFutureResultFromSupplier() {
        FutureResult<String> result = FutureResult.create(() -> "again");
        assertEquals("again", result.get());
    }

    @Test
    void createAFailResult() {
        FutureResult result = FutureResult.failure(Failure.create("code", "description"));
        assertTrue(result.hasFailures());
        assertEquals(1, result.getFailures().size());
        assertThrows(NoSuchElementException.class, result::get);
    }

    @Test
    void futureFailed() {
        Throwable throwable = new Exception("exception");
        CompletableFuture future = new CompletableFuture();
        future.completeExceptionally(throwable);
        FutureResult<String> result = FutureResult.create(future);
        assertTrue(result.hasFailures());
        assertEquals(throwable, ((ThrowableFailure) result.getFailures().get(0)).getThrowable());
    }

    @Test
    void exceptionExecutingTheFuture() {
        RuntimeException runtimeException = new RuntimeException("exception");
        FutureResult<String> result = FutureResult.create(CompletableFuture.supplyAsync(() -> {
            throw runtimeException;
        }));
        assertTrue(result.hasFailures());
        assertEquals(runtimeException, ((ThrowableFailure) result.getFailures().get(0)).getThrowable());
    }

    @Test
    void mapFunctionInFutureResult() {
        FutureResult<String> hello = FutureResult.create(CompletableFuture.completedFuture("hello"));
        FutureResult<String> upperHello = hello.map(String::toUpperCase);
        assertNotSame(hello, upperHello);
        assertEquals("HELLO", upperHello.get());
    }
}
