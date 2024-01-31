package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.MultipleFailures;
import dev.yila.functional.failure.ThrowableFailure;
import org.junit.jupiter.api.Test;

import java.util.*;

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
        Result<Integer, Failure> result = DirectResult.createChecked(() -> 5, RuntimeException.class);
        assertEquals(5, result.getOrThrow());
        assertEquals("DirectResult(OK): 5", result.toString());
    }

    @Test
    void checkedExceptionLaunched() {
        String exceptionMessage = "fail :(";
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException(exceptionMessage);
        };
        Result<Integer, Failure> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class);
        assertEquals("ThrowableFailure: java.lang.RuntimeException: fail :(", result.failure().get().toString());
        ThrowableFailure failure = (ThrowableFailure) result.failure().get();
        assertEquals(exceptionMessage, failure.getThrowable().getMessage());
    }

    @Test
    void checkedExceptionInFlatMapOnFail() {
        ThrowingSupplier throwingSupplier = () -> {
            throw new RuntimeException("aaa");
        };
        Result<Integer, Failure> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class)
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
        Result<Integer, Failure> result = DirectResult.createChecked(throwingSupplier, RuntimeException.class);
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
        assertThrows(IllegalArgumentException.class, () -> failure((Throwable) null));
        assertThrows(IllegalArgumentException.class, () -> failure((Failure) null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failures(null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.failures(Collections.emptyList()));
    }

    @Test
    void successValidation() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 6);

        Result<Map<String, Object>, Failure> result = DirectResult.validate(map,
                CodeDescriptionFailure.create("map", "missing number"),
                m -> m.containsKey("number"));

        assertSame(map, result.getOrThrow());
    }

    @Test
    void failureValidation() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 6);

        Result<Map<String, Object>, Failure> result = DirectResult.validate(map,
                CodeDescriptionFailure.create("map", "missing random"),
                m -> m.containsKey("random"));

        assertEquals("map: missing random", result.failure().get().toString());
    }

    @Test
    void invalidValidations() {
        assertThrows(IllegalArgumentException.class, () -> DirectResult.validate(
                null, Failure.create(new Throwable()), b -> true));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.validate(
                "value", null, b -> true));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.validate(
                "value", Failure.create(new Throwable()), null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.validate(null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.validate(
                "value", null));
        assertThrows(IllegalArgumentException.class, () -> DirectResult.validate("value"));
    }

    @Test
    void successMultipleValidation() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 6);

        Result<Map<String, Object>, Failure> result = DirectResult.validate(map,
                Pair.of(
                        CodeDescriptionFailure.create("map", "missing number"),
                        m -> m.containsKey("number")
                ), Pair.of(
                        CodeDescriptionFailure.create("map", "wrong number"),
                        m -> (Integer) m.get("number") == 6
                ));

        assertSame(map, result.getOrThrow());
    }

    static class TestException extends Exception {}

    static class SomeFailure implements Failure {
        @Override
        public String toString() {
            return "Some failure";
        }
    }

    @Override
    Result<Integer, Failure> number(Integer integer) {
        return DirectResult.ok(integer);
    }

    @Override
    Result<String, Failure> string(String string) {
        return DirectResult.ok(string);
    }

    @Override
    Result<Integer, Failure> failure(Failure failure) {
        return DirectResult.failure(failure);
    }

    @Override
    Result<Integer, Failure> failure(Throwable throwable) {
        return DirectResult.failure(throwable);
    }
}
