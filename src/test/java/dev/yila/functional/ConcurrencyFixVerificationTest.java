/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyFixVerificationTest {

    @Test
    void testInParallelCancellationOnTimeout() throws InterruptedException {
        Supplier<String> longRunningTask = () -> {
            try {
                Thread.sleep(5000); // Sleep longer than timeout
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "finished";
        };

        Result<String> result = AsyncResult.inParallel(
                ThreadPool.get(),
                (a, b) -> DirectResult.ok(a + b),
                longRunningTask,
                () -> "quick",
                100, TimeUnit.MILLISECONDS // Short timeout
        );

        assertTrue(result.hasFailure(), "Result should have failed due to timeout");
        assertTrue(result.failure().get().toThrowable().getMessage().contains("timeOut"), "Failure should be timeout");
    }

    @Test
    void testCreateCheckedHandlesUnexpectedExceptionSafely() {
        RuntimeException unexpected = new RuntimeException("Unexpected runtime error");
        
        // createChecked expecting TestException, but getting RuntimeException
        Result<String> result = AsyncResult.createChecked(
                ThreadPool.get(),
                () -> { throw unexpected; },
                TestException.class
        );

        // Should NOT throw CompletionException here
        assertTrue(result.hasFailure(), "Should return a failure");
        assertDoesNotThrow(result::hasFailure, "hasFailure() should not throw exception");
        
        Failure failure = result.failure().get();
        assertEquals(unexpected, failure.toThrowable(), "The failure should wrap the runtime exception");
    }

    @Test
    void testOnFailureNullSafety() {
        CountDownLatch latch = new CountDownLatch(1);
        // Create an AsyncResult that produces null (which is not allowed by DirectResult.ok, causing an exception)
        Result<String> result = AsyncResult.create(ThreadPool.get(), () -> null);
        
        // This should run the consumer because the result effectively failed (IAE)
        result.onFailure(f -> latch.countDown());
        
        try {
            boolean called = latch.await(200, TimeUnit.MILLISECONDS);
            assertTrue(called, "Consumer should be called on failure, but it wasn't (likely NPE in onFailure)");
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
    }

    static class TestException extends Exception {}
}
