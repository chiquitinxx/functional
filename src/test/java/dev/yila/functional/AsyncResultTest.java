package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class AsyncResultTest extends ResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");

    @Test
    void createAsyncResultFromFuture() {
        AsyncResult<Integer> result = AsyncResult.create(CompletableFuture.completedFuture(5));
        assertEquals(5, result.getOrThrow());
    }

    @Test
    void asyncFailedWithRuntimeException() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        AsyncResult<String> result = AsyncResult.create(completableFuture);
        completableFuture.completeExceptionally(runtimeException);
        assertTrue(result.hasFailure());
        Failure failure = result.failure().get();
        assertEquals(runtimeException, failure.toThrowable());
    }

    @Test
    void time() {
        Supplier<String> supplier = () -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return "hello";
        };
        LocalDateTime before = LocalDateTime.now();
        Result<String> first = AsyncResult.create(CompletableFuture.supplyAsync(supplier));
        Result<String> second = AsyncResult.create(CompletableFuture.supplyAsync(supplier));
        Result<String> third = AsyncResult.create(CompletableFuture.supplyAsync(supplier));

        Result<String> sequence = Result.sequence((list) -> list.stream()
                .reduce("", String::concat), first, second, third);

        assertEquals("hellohellohello", sequence.getOrThrow());
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 200);
    }

    @Override
    Result<Integer> number(Integer integer) {
        return AsyncResult.create(CompletableFuture.completedFuture(integer));
    }

    @Override
    Result<String> string(String string) {
        return AsyncResult.create(CompletableFuture.completedFuture(string));
    }

    @Override
    Result<Optional<String>> optional(Optional<String> optional) {
        return AsyncResult.create(CompletableFuture.completedFuture(optional));
    }

    @Override
    Result<Integer> failure(Failure failure) {
        return DirectResult.failure(failure);
    }

    @Override
    Result<Integer> failure(Throwable throwable) {
        return DirectResult.failure(throwable);
    }
}
