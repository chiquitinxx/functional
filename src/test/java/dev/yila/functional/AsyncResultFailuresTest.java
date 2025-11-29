package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsyncResultFailuresTest {

    @Test
    void mapShouldPreserveFailure() {
        Failure originalFailure = Failure.create("Original Failure");
        Result<String> failedResult = AsyncResult.create(CompletableFuture.completedFuture("success"))
                .flatMap(s -> DirectResult.failure(originalFailure));

        assertTrue(failedResult.hasFailure(), "Should have failure");
        assertEquals(originalFailure, failedResult.failure().get(), "Should have original failure before map");

        Result<Integer> mappedResult = failedResult.map(String::length);

        assertTrue(mappedResult.hasFailure(), "Should have failure after map");
        assertEquals(originalFailure, mappedResult.failure().get(), "Should have original failure after map");
    }

    @Test
    void exceptionAfterMapReturnsFailure() {
        Result<String> failedResult = AsyncResult.create(CompletableFuture.completedFuture("success"))
                .flatMap(s -> {throw new RuntimeException("yup");});

        assertTrue(failedResult.hasFailure(), "Should have failure");
        assertEquals("yup", failedResult.failure().get().toThrowable()
                .getCause().getMessage(), "Should have original failure before map");
    }
}
