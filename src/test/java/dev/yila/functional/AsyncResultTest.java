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

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
        assertEquals(runtimeException, failure.toException());
    }

    @Test
    void createCheckedAsync() {
        ExceptionSupplier<String, TestException> supplier = () -> "hello";
        Result<String> result = AsyncResult.createChecked(ThreadPool.get(), supplier, TestException.class);

        assertEquals("hello", result.getOrThrow());
    }

    @Test
    void checkedAsyncThrowingExpectedException() {
        ExceptionSupplier<String, TestException> supplier = () -> {
            throw new TestException();
        };
        Result<String> result = AsyncResult.createChecked(ThreadPool.get(), supplier, TestException.class);

        assertEquals(TestException.class, result.failure().get().toException().getClass());
    }

    @Test
    void checkedAsyncThrowingUnExpectedException() {
        ExceptionSupplier<String, TestException> supplier = () -> {
            throw new RuntimeException("hey");
        };

        AtomicInteger atomicInteger = new AtomicInteger(0);
        AsyncResult<String> result = AsyncResult.createChecked(ThreadPool.get(), supplier, TestException.class);
        result.onFailure(f -> atomicInteger.incrementAndGet());

        assertThrows(RuntimeException.class, result::getOrThrow, "hey");
        assertEquals(1, atomicInteger.get());
    }

    @Test
    void withTimeoutSuccess() {
        AsyncResult<Integer> result = AsyncResult.create(ThreadPool.get(), () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 5;
        });

        Result<Integer> timedResult = result.withTimeout(500, TimeUnit.MILLISECONDS);
        assertEquals(5, timedResult.getOrThrow());
    }

    @Test
    void withTimeoutFailure() {
        AsyncResult<Integer> result = AsyncResult.create(ThreadPool.get(), () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 5;
        });

        Result<Integer> timedResult = result.withTimeout(100, TimeUnit.MILLISECONDS);
        assertTrue(timedResult.hasFailure());
        Failure failure = timedResult.failure().get();
        assertInstanceOf(TimeoutException.class, failure.toException());
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
    <E extends Exception> Result<Integer> failure(E exception, Class<E> clazz) {
        ExceptionSupplier<Integer, E> supplier = () -> {
            throw exception;
        };
        return AsyncResult.createChecked(ThreadPool.get(), supplier, clazz);
    }
}
