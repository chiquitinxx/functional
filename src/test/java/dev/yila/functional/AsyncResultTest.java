package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

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

    @Test
    void time() {
        Supplier<String> supplier = () -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return "hello";
        };
        LocalDateTime before = LocalDateTime.now();
        Result<String, Failure> first = AsyncResult.create(CompletableFuture.supplyAsync(supplier));
        Result<String, Failure> second = AsyncResult.create(CompletableFuture.supplyAsync(supplier));
        Result<String, Failure> third = AsyncResult.create(CompletableFuture.supplyAsync(supplier));

        Result<String, Failure> join = Result.join((list) -> list.stream()
                .reduce("", String::concat), first, second, third);

        assertEquals("hellohellohello", join.getOrThrow());
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 150);
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
