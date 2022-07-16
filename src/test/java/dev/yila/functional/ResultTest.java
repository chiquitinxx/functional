package dev.yila.functional;

import dev.yila.functional.failure.BasicFailure;
import dev.yila.functional.failure.Failure;
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
        assertFalse(result.hasFailures());
        assertTrue(result.notHasFailure(BasicFailure.class));
        result.onSuccess(number -> assertEquals(5, number));
        result.onFailures(r -> fail("never executed"));
        assertEquals(5, result.get());
        assertEquals(5, result.orElse(r -> 0));
    }

    @Test
    void failureResult() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        assertTrue(result.hasFailures());
        assertTrue(result.hasFailure(BasicFailure.class));
        result.onSuccess(number -> fail("never executed"));
        result.onFailures(r -> assertEquals(1, r.getFailures().size()));
        assertEquals(0, result.orElse(r -> 0));
        assertThrows(NoSuchElementException.class, result::get);
    }

    @Test
    void resultWithMultipleFailures() {
        List<Failure> failures = new ArrayList();
        failures.add(new SomeFailure());
        failures.add(Failure.create(CODE, DESCRIPTION));
        Result result = Result.failures(failures);
        assertTrue(result.hasFailure(SomeFailure.class));
        assertEquals(2, result.getFailures().size());
        assertTrue(result.getFailures().get(0) instanceof SomeFailure);
        assertEquals("[Some failure, someCode: some description]", result.getFailuresToString());
        assertEquals("[some.failure.code, someCode]", result.getFailuresCode());
    }

    @Test
    void chainResults() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> multiplyBy3 = result.map(number -> number * 3);
        assertEquals(15, multiplyBy3.get());
        assertEquals(25, result.flatMap(number -> Result.ok(number * 5)).get());
    }

    @Test
    void joinResults() {
        Result joinFailure = Result.join(Result.ok(5), Result.failure(Failure.create(CODE, DESCRIPTION)));
        assertTrue(joinFailure.hasFailures());
        Result<List> join = Result.join(Result.ok(5), Result.ok(4), Result.ok(3));
        List<Integer> numbers = join.get();
        assertEquals(60, numbers.stream().reduce(1, (ac, value) -> ac * value));
    }

    @Test
    void createResults() {
        Result<Integer> result = Result.create(() -> 7);
        assertEquals(7, result.get());
        result = Result.flatCreate(() -> Result.ok(6));
        assertEquals(6, result.get());
    }

    @Test
    void nothingHappensMapFailure() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        Result<Integer> mapResult = result.map(number -> number * 2);
        assertTrue(mapResult.hasFailures());
        assertSame(result, mapResult);
    }

    @Test
    void nothingHappensFlatMapFailure() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        Result<Integer> mapResult = result.flatMap(number -> Result.ok(number * 2));
        assertTrue(mapResult.hasFailures());
        assertSame(result, mapResult);
    }

    @Test
    void checkedExceptionNotThrown() {
        Result<Integer> result = Result.createChecked(() -> 5, RuntimeException.class);
        assertEquals(5, result.get());
        assertEquals("Result(OK):5", result.toString());
    }

    @Test
    void checkedExceptionLaunched() {
        String exceptionMessage = "fail :(";
        ThrowingSupplierException supplierException = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer> result = Result.createChecked(supplierException, RuntimeException.class);
        assertTrue(result.hasFailure(ThrowableFailure.class));
        assertEquals("[ThrowableFailure: java.lang.RuntimeException: fail :(]", result.getFailuresToString());
        ThrowableFailure failure = (ThrowableFailure) result.getFailures().get(0);
        assertEquals(exceptionMessage, failure.getThrowable().getMessage());
    }

    @Test
    void checkedExceptionInFlatMapOnFail() {
        ThrowingSupplierException supplierException = () -> {
            throw new RuntimeException("aaa");
        };
        Result<Integer> result = Result.createChecked(supplierException, RuntimeException.class)
                .flatMap(number -> {
                    throw new RuntimeException("uuu");
                }, RuntimeException.class);
        assertTrue(result.getFailuresToString().contains("aaa"));
    }

    @Test
    void checkedExceptionInFlatMap() {
        ThrowingFunctionException functionException = (input) -> {
            throw new RuntimeException("Fail");
        };
        Result<Integer> result = Result.create(() -> 3)
                .flatMap(functionException, RuntimeException.class);
        assertEquals("[ThrowableFailure: java.lang.RuntimeException: Fail]", result.getFailuresToString());
    }

    @Test
    void failureAsThrowableForResultOk() {
        Result<Integer> result = Result.ok(5);
        assertThrows(NoSuchElementException.class, result::getFailuresAsThrowable);
    }

    @Test
    void getFailureAsThrowable() {
        String exceptionMessage = "fail :(";
        ThrowingSupplierException supplierException = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer> result = Result.createChecked(supplierException, RuntimeException.class);
        Throwable throwable = result.getFailuresAsThrowable();
        assertEquals(exceptionMessage, throwable.getMessage());
    }

    @Test
    void throwableFailure() {
        String exceptionMessage = "fail :(";
        Result<Integer> result = Result.failure(new RuntimeException(exceptionMessage));
        Throwable throwable = result.getFailuresAsThrowable();
        assertEquals(exceptionMessage, throwable.getMessage());
    }

    @Test
    void getMultipleFailureAsThrowable() {
        List<Failure> failures = new ArrayList<>();
        Exception firstException = new Exception("hello");
        Exception secondException = new Exception("second");
        failures.add(new ThrowableFailure(firstException));
        failures.add(Failure.create("any", "desc"));
        failures.add(new ThrowableFailure(secondException));
        Result<Integer> result = Result.failures(failures);
        Throwable throwable = result.getFailuresAsThrowable();
        assertSame(throwable, firstException);
        assertEquals(2, throwable.getSuppressed().length);
        assertEquals("any: desc", throwable.getSuppressed()[0].getMessage());
        assertSame(secondException, throwable.getSuppressed()[1]);
    }

    @Test
    void unexpectedCheckedExceptions() {
        String exceptionMessage = "fail :(";
        ThrowingSupplierException supplierException = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        assertThrows(RuntimeException.class, () ->
                Result.createChecked(supplierException, TestException.class));
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
        assertTrue(result.getFailuresToString().startsWith("[dev.yila.functional.ResultTest$SimpleFailure@"));
        assertEquals("[dev.yila.functional.ResultTest$SimpleFailure]", result.getFailuresCode());
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
