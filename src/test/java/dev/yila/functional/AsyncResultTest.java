package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class AsyncResultTest extends ResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");
    
    @Test
    void createAsyncResultFromFuture() {
        AsyncResult<Integer, ?> result = AsyncResult.create(CompletableFuture.completedFuture(5));
        assertEquals(5, result.getOrThrow());
    }

    @Test
    void asyncFailedWithRuntimeException() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        AsyncResult<String, ?> result = AsyncResult.create(completableFuture);
        completableFuture.completeExceptionally(runtimeException);
        assertTrue(result.hasFailure());
        Failure failure = result.failure().get();
        assertEquals(runtimeException, failure.toThrowable());
    }

    @Override
    Result<Integer, Failure> number(Integer integer) {
        return AsyncResult.create(CompletableFuture.completedFuture(integer));
    }

    @Override
    Result<String, Failure> string(String string) {
        return AsyncResult.create(CompletableFuture.completedFuture(string));
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
