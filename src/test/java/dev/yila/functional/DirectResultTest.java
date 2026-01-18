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

import dev.yila.functional.failure.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static dev.yila.functional.DirectResult.join;
import static org.junit.jupiter.api.Assertions.*;

public class DirectResultTest extends ResultTest {

    private static final String CODE = "someCode";
    private static final String DESCRIPTION = "some description";

    @Test
    void resultWithMultipleFailures() {
        List<Failure> failures = new ArrayList<>();
        failures.add(new SomeFailure());
        failures.add(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        Result result = DirectResult.failures(failures);
        Failure multipleFailure = (Failure) result.failure().get();
        assertInstanceOf(MultipleFailures.class, multipleFailure);
        assertEquals("[Some failure, someCode: some description]", multipleFailure.toString());
    }

    @Test
    void checkedExceptionNotThrown() {
        Result<Integer> result = DirectResult.createChecked(() -> 5, RuntimeException.class);
        assertEquals(5, result.getOrThrow());
        assertEquals("DirectResult(OK): 5", result.toString());
    }

    @Test
    void checkedExceptionLaunched() {
        String exceptionMessage = "fail :(";
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class);
        assertEquals("ThrowableFailure: java.lang.RuntimeException: fail :(", result.failure().get().toString());
        ThrowableFailure failure = (ThrowableFailure) result.failure().get();
        assertEquals(exceptionMessage, failure.getThrowable().getMessage());
    }

    @Test
    void checkedExceptionInMapOnFail() {
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException("aaa");
        };
        Result<Integer> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class)
                .map(number -> {
                    throw new RuntimeException("uuu");
                }, RuntimeException.class);
        assertTrue(result.failure().get().toString().contains("aaa"));
    }

    @Test
    void checkedExceptionInFlatMapOnFail() {
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException("aaa");
        };
        Result<Integer> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class)
                .flatMap(number -> {
                    throw new RuntimeException("uuu");
                }, RuntimeException.class);
        assertTrue(result.failure().get().toString().contains("aaa"));
    }

    @Test
    void getFailureAsThrowable() {
        String exceptionMessage = "fail :(";
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class);
        Throwable throwable = result.failure().get().toThrowable();
        assertEquals(exceptionMessage, throwable.getMessage());
    }

    @Test
    void unexpectedCheckedExceptions() {
        String exceptionMessage = "fail :(";
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        assertThrows(RuntimeException.class, () ->
                DirectResult.createChecked(throwingSupplier, TestException.class));
    }

    @Test
    void unexpectedExceptionInCheckedFlatMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException();
        };
        assertThrows(RuntimeException.class, () ->
                number(3)
                        .flatMap(throwingFunction, NullPointerException.class)
                        .failure().get()
        );
    }

    @Test
    void canNotCreateEmptyResults() {
        assertThrows(IllegalArgumentException.class, () -> number(null));
        assertThrows(IllegalArgumentException.class, () -> failure((Throwable) null, Throwable.class));
        assertThrows(IllegalArgumentException.class, () -> failure((Failure) null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failure((String) null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failure(""));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failure("    "));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failures(null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failures(Collections.emptyList()));
    }

    @Test
    void createFailureFromMessage() {
        Result result = DirectResult.failure("error message");
        DescriptionFailure failure = (DescriptionFailure) result.failure().get();
        assertEquals("error message", failure.toString());
    }

    @Test
    void joinFromFailure() {
        Result failure = failure(new SimpleFailure());
        Result<String> result = join(failure);

        assertTrue(result.hasFailure());
        assertTrue(result.failure().get() instanceof SimpleFailure);
    }

    @Test
    void removeEmptyOptional() {
        Result<Optional<String>> empty = optional(Optional.empty());
        Result<String> result = join(empty);

        assertTrue(result.hasFailure());
        assertTrue(result.failure().get().toThrowable() instanceof NoSuchElementException);
    }

    @Test
    void removePresentOptional() {
        Result<Optional<String>> present = optional(Optional.of("hello"));
        DirectResult<String> result = join(present);

        assertEquals("hello", result.getOrThrow());
    }

    @Test
    void sequenceResults() {
        Result<Integer> sequenceFailure = DirectResult.sequence(list -> list.get(0),
                number(5),
                failure(CodeDescriptionFailure.create(CODE, DESCRIPTION)));
        assertTrue(sequenceFailure.hasFailure());
        Result<Integer> sequence = DirectResult.sequence(list ->
                        list.stream().reduce(0, Integer::sum),
                number(5),
                number(4),
                number(3));
        assertEquals(12, sequence.getOrThrow());
    }

    static class TestException extends Exception {}

    static class SomeFailure implements Failure {
        @Override
        public String toString() {
            return "Some failure";
        }
    }

    @Override
    Result<Integer> number(Integer integer) {
        return DirectResult.ok(integer);
    }

    @Override
    Result<String> string(String string) {
        return DirectResult.ok(string);
    }

    @Override
    Result<Optional<String>> optional(Optional<String> optional) {
        return DirectResult.ok(optional);
    }

    @Override
    Result<Integer> failure(Failure failure) {
        return DirectResult.failure(failure);
    }

    @Override
    <E extends Throwable> Result<Integer> failure(E throwable, Class<E> clazz) {
        return DirectResult.failure(throwable);
    }
}
