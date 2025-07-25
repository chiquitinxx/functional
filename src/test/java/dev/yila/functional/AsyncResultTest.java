package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    void checkExecutionTimeToValidateExecutionInParallel() {
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
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 180);
    }

    @Test
    void executeTwoTaskInParallel() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> "world";

        Result<String> result = AsyncResult.inParallel((f, s) -> f + " " + s + "!", first, second);

        assertEquals("hello world!", result.getOrThrow());
    }

    @Test
    void executeTwoTaskInParallelWithTimeOut() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> "world";

        Result<String> result = AsyncResult.inParallel((f, s) -> f + " " + s + "!", first, second, 1, TimeUnit.SECONDS);

        assertEquals("hello world!", result.getOrThrow());
    }

    @Test
    void exceptionIsControlledExecutingInParallel() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> {
            throw new RuntimeException("world is down");
        };

        Result<String> result = AsyncResult.inParallel((f, s) -> f + " " + s + "!", first, second);

        assertEquals("world is down", result.failure().get().toThrowable().getCause().getMessage());
    }

    @Test
    void exceptionInParallelWithTimeOut() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> {
            while (true) {}
        };

        Result<String> result = AsyncResult.inParallel((f, s) -> f + " " + s + "!", first, second,
                200, TimeUnit.MILLISECONDS);

        assertEquals("AsyncResult inParallel timeOut", result.failure().get().toThrowable().getCause().getMessage());
    }

    @Test
    void validateExecutionInParallel() {
        Supplier<String> supplier = () -> {
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
            return "world";
        };
        LocalDateTime before = LocalDateTime.now();
        Result<String> result = AsyncResult.inParallel((f, s) -> f + s, supplier, supplier);

        assertEquals("worldworld", result.getOrThrow());
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 130);
    }

    @Test
    void validateExecutionInParallelJoiningInParallelExecutions() {
        Supplier<String> supplier = () -> {
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
            return "world";
        };
        LocalDateTime before = LocalDateTime.now();
        Result<String> run = AsyncResult.inParallel((f, s) -> f + s, supplier, supplier);
        Result<String> result = AsyncResult.inParallel((f, s) -> f + s, supplier, run::getOrThrow);

        assertEquals("worldworldworld", result.getOrThrow());
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 130);
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
