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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class AsyncResultTest extends ResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");

    @Test
    void createAsyncResultFromSupplier() {
        AsyncResult<Integer> result = AsyncResult.create(ThreadPool.get(), () -> 5);
        assertEquals(5, result.getOrThrow());
    }

    @Test
    void asyncFailedWithRuntimeException() {
        AsyncResult<String> result = AsyncResult.create(ThreadPool.get(), () -> {
            throw runtimeException;
        });
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
        Result<String> first = AsyncResult.create(ThreadPool.get(), supplier);
        Result<String> second = AsyncResult.create(ThreadPool.get(), supplier);
        Result<String> third = AsyncResult.create(ThreadPool.get(), supplier);

        Result<String> sequence = Result.sequence((list) -> list.stream()
                .reduce("", String::concat), first, second, third);

        assertEquals("hellohellohello", sequence.getOrThrow());
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 180);
    }

    @Test
    void executeTwoTaskInParallel() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> "world";

        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + " " + s + "!"), first, second);

        assertEquals("hello world!", result.getOrThrow());
    }

    @Test
    void executeTwoTaskInParallelWithTimeOut() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> "world";

        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + " " + s + "!"), first, second, 1, TimeUnit.SECONDS);

        assertEquals("hello world!", result.getOrThrow());
    }

    @Test
    void exceptionIsControlledExecutingInParallel() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> {
            throw new RuntimeException("world is down");
        };

        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + " " + s + "!"), first, second);

        assertEquals("world is down", result.failure().get().toThrowable().getCause().getMessage());
    }

    @Test
    void exceptionIsControlledExecutingInParallelAtFirst() {
        Supplier<String> second = () -> "hello";
        Supplier<String> first = () -> {
            throw new RuntimeException("world is down");
        };

        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + " " + s + "!"), first, second);

        assertEquals("world is down", result.failure().get().toThrowable().getCause().getMessage());
    }

    @Test
    void exceptionInParallelWithTimeOut() {
        Supplier<String> first = () -> "hello";
        Supplier<String> second = () -> {
            while (true) {}
        };

        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + " " + s + "!"), first, second,
                200, TimeUnit.MILLISECONDS);

        assertEquals("AsyncResult inParallel timeOut", result.failure().get().toThrowable().getMessage());
    }

    @Test
    void validateExecutionInParallel() {
        Supplier<String> supplier = () -> {
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
            return "world";
        };
        LocalDateTime before = LocalDateTime.now();
        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + s), supplier, supplier);

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
        Result<String> run = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + s), supplier, supplier);
        Result<String> result = AsyncResult.inParallel(ThreadPool.get(), (f, s) -> DirectResult.ok(f + s), supplier, run::getOrThrow);

        assertEquals("worldworldworld", result.getOrThrow());
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 130);
    }

    @Test
    void createCheckedAsync() {
        ThrowingSupplier<String, TestException> supplier = () -> "hello";
        Result<String> result = AsyncResult.createChecked(ThreadPool.get(), supplier, TestException.class);

        assertEquals("hello", result.getOrThrow());
    }

    @Test
    void checkedAsyncThrowingExpectedException() {
        ThrowingSupplier<String, TestException> supplier = () -> {
            throw new TestException();
        };
        Result<String> result = AsyncResult.createChecked(ThreadPool.get(), supplier, TestException.class);

        assertEquals(TestException.class, result.failure().get().toThrowable().getClass());
    }

    @Test
    void checkedAsyncThrowingUnExpectedException() {
        ThrowingSupplier<String, TestException> supplier = () -> {
            throw new RuntimeException("hey");
        };

        AtomicInteger atomicInteger = new AtomicInteger(0);
        AsyncResult<String> result = AsyncResult.createChecked(ThreadPool.get(), supplier, TestException.class);
        result.onFailure(f -> atomicInteger.incrementAndGet());

        assertThrows(RuntimeException.class, result::getOrThrow, "hey");
        assertEquals(1, atomicInteger.get());
    }

    static class TestException extends Exception {}

    @Override
    Result<Integer> number(Integer integer) {
        return AsyncResult.create(ThreadPool.get(), () -> integer);
    }

    @Override
    Result<String> string(String string) {
        return AsyncResult.create(ThreadPool.get(), () -> string);
    }

    @Override
    Result<Optional<String>> optional(Optional<String> optional) {
        return AsyncResult.create(ThreadPool.get(), () -> optional);
    }

    @Override
    Result<Integer> failure(Failure failure) {
        return DirectResult.failure(failure);
    }

    @Override
    <E extends Throwable> Result<Integer> failure(E throwable, Class<E> clazz) {
        ThrowingSupplier<Integer, E> supplier = () -> {
            throw throwable;
        };
        return AsyncResult.createChecked(ThreadPool.get(), supplier, clazz);
    }
}
