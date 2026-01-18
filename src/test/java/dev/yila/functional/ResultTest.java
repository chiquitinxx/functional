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

import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.DescriptionFailure;
import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ResultTest {

    static final String CODE = "someCode";
    static final String DESCRIPTION = "some description";

    @Test
    void successResult() {
        Result<Integer> result = number(5);
        assertFalse(result.hasFailure());
        result.onSuccess(number -> assertEquals(5, number));
        result.onFailure(r -> fail("never executed"));
        assertEquals(5, result.getOrThrow());
        assertEquals(5, result.orElse(r -> 0));
    }

    @Test
    void throwableFailureResult() {
        AtomicInteger atomic = new AtomicInteger(0);
        Result<Integer> result = failure(new RuntimeException("nope"), RuntimeException.class);
        result.onFailure((f) -> atomic.incrementAndGet());
        assertTrue(result.failure().get().toString().endsWith("nope"));
        assertEquals(1, atomic.get());
    }

    @Test
    void codeDescriptionFailureResult() {
        Result<Integer> result = failure(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        assertTrue(result.hasFailure());
        result.onSuccess(number -> fail("never executed"));
        result.onFailure(f -> assertEquals(result.failure().get(), f));
        assertEquals(0, result.orElse(r -> 0));
        assertThrows(NoSuchElementException.class, result::getOrThrow);
    }

    @Test
    void descriptionFailureResult() {
        Result<Integer> result = failure(DescriptionFailure.create(DESCRIPTION));
        assertEquals(DESCRIPTION, result.failure().get().toString());
    }

    @Test
    void chainNumberResults() {
        Result<Integer> result = number(5);
        Result<Integer> multiplyBy3 = result.map(number -> number * 3);
        assertEquals(15, multiplyBy3.getOrThrow());
        assertEquals(25, result.flatMap(number -> number(number * 5)).getOrThrow());
        assertEquals(30, multiplyBy3.flatMap(number -> number(number * 2)).getOrThrow());
    }

    @Test
    void flatMapWithCheckedException() {
        ThrowingFunction<Integer, Result<Integer>, RuntimeException> function =
                (input) -> number(input + 2);
        Result<Integer> result = number(6)
                .flatMap(function, RuntimeException.class);
        assertEquals(8, result.getOrThrow());
    }

    @Test
    void mapWithCheckedException() {
        ThrowingFunction<Integer, Integer, RuntimeException> function =
                (input) -> input + 2;
        Result<Integer> result = number(6)
                .map(function, RuntimeException.class);
        assertEquals(8, result.getOrThrow());
    }

    @Test
    void checkedExceptionInFlatMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException("Fail");
        };
        Result<Integer> result = number(3)
                .flatMap(throwingFunction, RuntimeException.class);
        assertTrue(result.failure().get().toString().endsWith("java.lang.RuntimeException: Fail"));
    }

    @Test
    void checkedExceptionInMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException("Fail");
        };
        Result<Integer> result = number(3)
                .map(throwingFunction, RuntimeException.class);
        assertTrue(result.failure().get().toString().endsWith("ThrowableFailure: java.lang.RuntimeException: Fail"));
    }

    @Test
    void sequenceResults() {
        Result<Integer> sequenceFailure = Result.sequence(list -> list.get(0),
                number(5),
                failure(CodeDescriptionFailure.create(CODE, DESCRIPTION)));
        assertTrue(sequenceFailure.hasFailure());
        Result<Integer> sequence = Result.sequence(list ->
                        list.stream().reduce(0, Integer::sum),
                number(5),
                number(4),
                number(3));
        assertEquals(12, sequence.getOrThrow());
    }

    @Test
    void nothingHappensMapFailure() {
        Result<Integer> result = failure(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        Result<Integer> mapResult = result.map(number -> number * 2);
        assertTrue(mapResult.hasFailure());
        assertEquals("DirectResult(FAILURE): someCode: some description", result.toString());
    }

    @Test
    void nothingHappensFlatMapFailure() {
        Result<Integer> result = failure(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        Result<Integer> mapResult = result.flatMap(number -> number(number * 2));
        assertTrue(mapResult.hasFailure());
        assertEquals(result.failure(), mapResult.failure());
    }

    @Test
    void throwableFailure() {
        String exceptionMessage = "fail :(";
        Result<?> result = failure(new RuntimeException(exceptionMessage), RuntimeException.class);
        Throwable throwable = result.failure().get().toThrowable();
        assertEquals(exceptionMessage, throwable.getMessage());
    }

    @Test
    void simpleFailure() {
        Result result = failure(new SimpleFailure());
        assertTrue(result.failure().get().toString().startsWith("dev.yila.functional.ResultTest$SimpleFailure@"));
    }

    @Test
    void toOptional() {
        Result<String> good = string("good");
        Result failure = failure(CodeDescriptionFailure.create("failure", "result"));

        assertEquals("good", good.value().get());
        assertFalse(failure.value().isPresent());
        assertEquals("failure: result", failure.failure().get().toString());
    }

    @Test
    void mapFun() {
        Fun<String, String> upper = Fun.from(String::toUpperCase);

        Result<String> toUpper = string("hello").map(upper);

        assertEquals("HELLO", toUpper.getOrThrow());
    }

    @Test
    void mapFunAfterFailure() {
        Fun<Integer, Integer> upper = Fun.from(n -> n * 2);

        Result<Integer> twoTimes = failure(Failure.create("error")).map(upper);

        assertTrue(twoTimes.hasFailure());
    }

    @Test
    void mapFunFailure() {
        ThrowingFunction<String, String, NullPointerException> function = s -> {
            throw new NullPointerException("null");
        };
        Fun<String, String> upper = Fun.from(function, NullPointerException.class);

        Result<String> result = string("hello").map(upper);

        assertTrue(result.hasFailure());
    }

    static class SimpleFailure implements Failure {}

    abstract Result<Integer> number(Integer integer);
    abstract Result<String> string(String string);
    abstract Result<Optional<String>> optional(Optional<String> optional);
    abstract Result<Integer> failure(Failure failure);
    abstract <E extends Throwable> Result<Integer> failure(E throwable, Class<E> clazz);
}
