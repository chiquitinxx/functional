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
import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class LazyResultTest extends ResultTest {

    private final RuntimeException runtimeException = new RuntimeException("exception");
    private final CodeDescriptionFailure failure = CodeDescriptionFailure.create("code", "description");

    @Test
    void createLazyResultFromSupplier() {
        LazyResult<String> result = LazyResult.create(() -> "again");
        assertEquals("again", result.getOrThrow());
    }

    @Test
    void lazyFailedWithRuntimeException() {
        LazyResult<String> result = LazyResult.create(() -> {
            throw runtimeException;
        });
        assertTrue(result.hasFailure());
        Failure failure = result.failure().get();
        assertEquals(runtimeException, failure.toThrowable());
    }

    @Test
    void unexpectedExceptionInCheckedMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException();
        };
        ThrowableFailure failure = (ThrowableFailure) number(3)
                .map(throwingFunction, NullPointerException.class)
                .failure().get();
        assertEquals("java.lang.RuntimeException", failure.getThrowable().getMessage());
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
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> upperHello = hello.map(String::toUpperCase);
        assertNotSame(hello, upperHello);
        assertEquals("HELLO", upperHello.getOrThrow());
    }

    @Test
    void exceptionInMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> failureLazy = hello.map(input -> {
            throw runtimeException;
        });
        assertNotSame(hello, failureLazy);
        assertTrue(failureLazy.hasFailure());
        assertSame(runtimeException, failureLazy.failure().get().toThrowable());
    }

    @Test
    void multipleMapFunctions() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        Result<Integer> stringLength = hello
                .map(String::toUpperCase)
                .map(String::toLowerCase)
                .map(String::length);
        assertEquals(5, stringLength.getOrThrow());
    }

    @Test
    void flatMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> doubleHello = hello.flatMap(s -> LazyResult.create(() -> s + s));
        assertNotSame(hello, doubleHello);
        assertEquals("hellohello", doubleHello.getOrThrow());
    }

    @Test
    void nothingHappensUntilValueIsRequested() {
        AtomicInteger counter = new AtomicInteger(0);
        Result<Integer> start = number(4);
        Result<Integer> after = start.map(num -> {
            counter.incrementAndGet();
            return num * 2;
        });
        Result<Integer> last = after.flatMap(num -> {
            counter.incrementAndGet();
            return after.map(n -> n * num);
        });
        assertEquals(0, counter.get());
        assertEquals(64, last.getOrThrow());
        assertEquals(3, counter.get());
    }

    @Test
    void failureAfterMap() {
        Result<String> fail = DirectResult.failure(failure);
        Result<String> afterFail = fail.flatMap(s -> LazyResult.create(() -> s + s));
        assertNotSame(fail, afterFail);
        assertSame(failure, afterFail.failure().get());
        Result<String> lastFail = afterFail.map(s -> s + s);
        assertSame(failure, lastFail.failure().get());
    }

    @Test
    void failureInFlatMapFunction() {
        LazyResult<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> failureHello = hello
                .flatMap(s -> DirectResult.failure(failure));
        assertNotSame(hello, failureHello);
        assertSame(failure, failureHello.failure().get());
    }

    @Test
    void exceptionInFlatMapFunction() {
        Result<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> exceptionHello = hello.flatMap(s -> LazyResult.create(() -> {
            throw runtimeException;
        }));
        assertNotSame(hello, exceptionHello);
        assertSame(runtimeException, exceptionHello.failure().get().toThrowable());
    }

    @Test
    void failureOnCheckedFlatMap() {
        Result<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> exceptionHello = hello.flatMap(s -> {
            throw new NullPointerException();
        }, NullPointerException.class);
        assertTrue(exceptionHello.failure().get().toThrowable() instanceof NullPointerException);
    }

    @Test
    void successOnCheckedFlatMap() {
        Result<String> hello = LazyResult.create(() -> ("hello"));
        Result<String> exceptionHello = hello.flatMap(String::toUpperCase, NullPointerException.class);
        assertEquals("HELLO", exceptionHello.getOrThrow());
    }

    @Test
    void time() {
        Supplier<String> supplier = () -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return "hello";
        };
        LocalDateTime before = LocalDateTime.now();
        Result<String> first = LazyResult.create(supplier);
        Result<String> second = LazyResult.create(supplier);
        Result<String> third = LazyResult.create(supplier);

        Result<String> sequence = Result.sequence((list) -> list.stream()
                .reduce("", String::concat), first, second, third);

        assertEquals("hellohellohello", sequence.getOrThrow());
        long millis = ChronoUnit.MILLIS.between(before, LocalDateTime.now());
        System.out.println("Millis: " + millis);
        assertTrue(ChronoUnit.MILLIS.between(before, LocalDateTime.now()) < 250);
    }

    private int add(int number) {
        return add(number);
    }

    @Test
    void generateStackOverflowError() {
        Result<Integer> hello = LazyResult.create(() -> add(3));
        assertTrue(hello.failure().get().toThrowable() instanceof StackOverflowError);
    }

    @Test
    void multipleMapAndFlatFunctions() {
        Result<String> hello = LazyResult.create(() -> ("hello"));
        Result<Integer> stringLength = hello
                .flatMap(s -> LazyResult.create(s::toUpperCase))
                .map(String::toLowerCase)
                .flatMap(Fun.from(s -> s + "yo"))
                .flatMap(s -> LazyResult.create(s::length))
                .map(size -> size * size);
        assertEquals(49, stringLength.getOrThrow());
    }

    @Test
    void canNotUseNullAsSupplierOfFunctions() {
        assertThrows(IllegalArgumentException.class, () -> LazyResult.create(null));
        Result<String> hello = LazyResult.create(() -> ("hello"));
        assertThrows(IllegalArgumentException.class, () -> hello.map(null));
        assertThrows(NullPointerException.class, () -> hello.flatMap((Function)null));
        assertThrows(NullPointerException.class, () -> hello.flatMap((Fun)null));
    }

    @Test
    void canStartTheExecution() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Result<Integer> hello = LazyResult
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
        Result<Integer> hello = LazyResult
                .create(() -> ("hello"))
                .map(String::length);
        assertEquals(hello.getOrThrow(), hello.getOrThrow());
    }

    @Override
    Result<Integer> number(Integer integer) {
        return LazyResult.create(() -> integer);
    }

    @Override
    Result<String> string(String string) {
        return LazyResult.create(() -> string);
    }

    @Override
    Result<Optional<String>> optional(Optional<String> optional) {
        return LazyResult.create(() -> optional);
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
