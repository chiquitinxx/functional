package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MultipleFailures;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    private static final String CODE = "someCode";
    private static final String DESCRIPTION = "some description";

    @Test
    void successResult() {
        Result<Integer> result = Result.ok(5);
        assertFalse(result.hasFailure());
        result.onSuccess(number -> assertEquals(5, number));
        result.onFailure(r -> fail("never executed"));
        assertEquals(5, result.getOrThrow());
        assertEquals(5, result.orElse(r -> 0));
    }

    @Test
    void failureResult() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        assertTrue(result.hasFailure());
        result.onSuccess(number -> fail("never executed"));
        result.onFailure(r -> assertEquals(result.failure().get(), r.failure().get()));
        assertEquals(0, result.orElse(r -> 0));
        assertThrows(NoSuchElementException.class, result::getOrThrow);
    }

    @Test
    void resultWithMultipleFailures() {
        List<Failure> failures = new ArrayList<>();
        failures.add(new SomeFailure());
        failures.add(Failure.create(CODE, DESCRIPTION));
        Result result = Result.failures(failures);
        Failure multipleFailure = (Failure) result.failure().get();
        assertInstanceOf(MultipleFailures.class, multipleFailure);
        assertEquals("[Some failure, someCode: some description]", multipleFailure.toString());
    }

    @Test
    void chainResults() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> multiplyBy3 = result.map(number -> number * 3);
        assertEquals(15, multiplyBy3.getOrThrow());
        assertEquals(25, result.flatMap(number -> Result.ok(number * 5)).getOrThrow());
    }

    @Test
    void joinResults() {
        Result joinFailure = Result.join(Result.ok(5), Result.failure(Failure.create(CODE, DESCRIPTION)));
        assertTrue(joinFailure.hasFailure());
        Result<List> join = Result.join(Result.ok(5), Result.ok(4), Result.ok(3));
        List<Integer> numbers = join.getOrThrow();
        assertEquals(60, numbers.stream().reduce(1, (ac, value) -> ac * value));
    }

    @Test
    void createResults() {
        Result<Integer> result = Result.create(() -> 7);
        assertEquals(7, result.getOrThrow());
        result = Result.flatCreate(() -> Result.ok(6));
        assertEquals(6, result.getOrThrow());
    }

    @Test
    void nothingHappensMapFailure() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        Result<Integer> mapResult = result.map(number -> number * 2);
        assertTrue(mapResult.hasFailure());
        assertSame(result, mapResult);
        assertEquals("Result(FAILURE): someCode: some description", result.toString());
    }

    @Test
    void nothingHappensFlatMapFailure() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        Result<Integer> mapResult = result.flatMap(number -> Result.ok(number * 2));
        assertTrue(mapResult.hasFailure());
        assertSame(result, mapResult);
    }

    @Test
    void checkedExceptionNotThrown() {
        Result<Integer> result = Result.createChecked(() -> 5, RuntimeException.class);
        assertEquals(5, result.getOrThrow());
        assertEquals("Result(OK): 5", result.toString());
    }

    @Test
    void checkedExceptionLaunched() {
        String exceptionMessage = "fail :(";
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer> result = Result.createChecked(throwingSupplier, RuntimeException.class);
        assertEquals("ThrowableFailure: java.lang.RuntimeException: fail :(", result.failure().get().toString());
        ThrowableFailure failure = (ThrowableFailure) result.failure().get();
        assertEquals(exceptionMessage, failure.getThrowable().getMessage());
    }

    @Test
    void checkedExceptionInFlatMapOnFail() {
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException("aaa");
        };
        Result<Integer> result = Result.createChecked(throwingSupplier, RuntimeException.class)
                .flatMap(number -> {
                    throw new RuntimeException("uuu");
                }, RuntimeException.class);
        assertTrue(result.failure().get().toString().contains("aaa"));
    }

    @Test
    void checkedExceptionInFlatMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException("Fail");
        };
        Result<Integer> result = Result.create(() -> 3)
                .flatMap(throwingFunction, RuntimeException.class);
        assertEquals("ThrowableFailure: java.lang.RuntimeException: Fail", result.failure().get().toString());
    }

    @Test
    void getFailureAsThrowable() {
        String exceptionMessage = "fail :(";
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer> result = Result.createChecked(throwingSupplier, RuntimeException.class);
        Throwable throwable = result.failure().get().toThrowable();
        assertEquals(exceptionMessage, throwable.getMessage());
    }

    @Test
    void throwableFailure() {
        String exceptionMessage = "fail :(";
        Result<Integer> result = Result.failure(new RuntimeException(exceptionMessage));
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
                Result.createChecked(throwingSupplier, TestException.class));
    }

    @Test
    void canNotCreateEmptyResults() {
        assertThrows(IllegalArgumentException.class, () -> Result.ok(null));
        assertThrows(IllegalArgumentException.class, () -> Result.failure((Throwable) null));
        assertThrows(IllegalArgumentException.class, () -> Result.failure((Failure) null));
        assertThrows(IllegalArgumentException.class, () -> Result.failures(null));
        assertThrows(IllegalArgumentException.class, () -> Result.failures(Collections.emptyList()));
    }

    @Test
    void simpleFailure() {
        Result result = Result.failure(new SimpleFailure());
        assertTrue(result.failure().get().toString().startsWith("dev.yila.functional.ResultTest$SimpleFailure@"));
    }

    @Test
    void toOptional() {
        Result<String> good = Result.ok("good");
        Result failure = Result.failure(Failure.create("failure", "result"));

        assertEquals("good", good.value().get());
        assertFalse(failure.value().isPresent());
        assertEquals("failure", ((Failure) failure.failure().get()).getCode());
    }

    @Test
    void flatMapWithCheckedException() {
        ThrowingFunction<Integer, Integer, RuntimeException> function =
                (input) -> input + 2;
        Result<Integer> result = Result.ok(6)
                .flatMap(function, RuntimeException.class);
        assertEquals(8, result.getOrThrow());
    }

    static class TestException extends Exception {}

    static class SimpleFailure implements Failure {}

    static class SomeFailure implements Failure {
        @Override
        public String getCode() {
            return "some.failure.code";
        }

        @Override
        public String toString() {
            return "Some failure";
        }
    }
}
