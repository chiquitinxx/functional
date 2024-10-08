package dev.yila.functional;

import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.DescriptionFailure;
import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static dev.yila.functional.Result.removeOptional;
import static org.junit.jupiter.api.Assertions.*;

public abstract class ResultTest {

    static final String CODE = "someCode";
    static final String DESCRIPTION = "some description";

    @Test
    void successResult() {
        Result<Integer, Failure> result = number(5);
        assertFalse(result.hasFailure());
        result.onSuccess(number -> assertEquals(5, number));
        result.onFailure(r -> fail("never executed"));
        assertEquals(5, result.getOrThrow());
        assertEquals(5, result.orElse(r -> 0));
    }

    @Test
    void codeDescriptionFailureResult() {
        Result<Integer, Failure> result = failure(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        assertTrue(result.hasFailure());
        result.onSuccess(number -> fail("never executed"));
        result.onFailure(r -> assertEquals(result.failure().get(), r.failure().get()));
        assertEquals(0, result.orElse(r -> 0));
        assertThrows(NoSuchElementException.class, result::getOrThrow);
    }

    @Test
    void descriptionFailureResult() {
        Result<Integer, Failure> result = failure(DescriptionFailure.create(DESCRIPTION));
        assertEquals(DESCRIPTION, result.failure().get().toString());
    }

    @Test
    void chainNumberResults() {
        Result<Integer, Failure> result = number(5);
        Result<Integer, Failure> multiplyBy3 = result.map(number -> number * 3);
        assertEquals(15, multiplyBy3.getOrThrow());
        assertEquals(25, result.flatMap(number -> number(number * 5)).getOrThrow());
        assertEquals(30, multiplyBy3.flatMap(number -> number(number * 2)).getOrThrow());
    }

    @Test
    void flatMapWithCheckedException() {
        ThrowingFunction<Integer, Integer, RuntimeException> function =
                (input) -> input + 2;
        Result<Integer, Failure> result = number(6)
                .flatMap(function, RuntimeException.class);
        assertEquals(8, result.getOrThrow());
    }

    @Test
    void checkedExceptionInFlatMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException("Fail");
        };
        Result<Integer, Failure> result = number(3)
                .flatMap(throwingFunction, RuntimeException.class);
        assertEquals("ThrowableFailure: java.lang.RuntimeException: Fail", result.failure().get().toString());
    }

    @Test
    void joinResults() {
        Result<Integer, Failure> joinFailure = Result.join(list -> list.get(0),
                number(5),
                failure(CodeDescriptionFailure.create(CODE, DESCRIPTION)));
        assertTrue(joinFailure.hasFailure());
        Result<Integer, Failure> join = Result.join(list ->
                        list.stream().reduce(0, Integer::sum),
                number(5),
                number(4),
                number(3));
        assertEquals(12, join.getOrThrow());
    }

    @Test
    void nothingHappensMapFailure() {
        Result<Integer, Failure> result = failure(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        Result<Integer, Failure> mapResult = result.map(number -> number * 2);
        assertTrue(mapResult.hasFailure());
        assertEquals("DirectResult(FAILURE): someCode: some description", result.toString());
    }

    @Test
    void nothingHappensFlatMapFailure() {
        Result<Integer, Failure> result = failure(CodeDescriptionFailure.create(CODE, DESCRIPTION));
        Result<Integer, Failure> mapResult = result.flatMap(number -> number(number * 2));
        assertTrue(mapResult.hasFailure());
        assertEquals(result.failure(), mapResult.failure());
    }

    @Test
    void throwableFailure() {
        String exceptionMessage = "fail :(";
        Result<?, Failure> result = failure(new RuntimeException(exceptionMessage));
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
        Result<String, Failure> good = string("good");
        Result failure = failure(CodeDescriptionFailure.create("failure", "result"));

        assertEquals("good", good.value().get());
        assertFalse(failure.value().isPresent());
        assertEquals("failure: result", failure.failure().get().toString());
    }

    @Test
    void removeOptionalFromFailure() {
        Result failure = failure(new SimpleFailure());
        Result<String, Failure> result = removeOptional(failure);

        assertTrue(result.hasFailure());
        assertTrue(result.failure().get() instanceof SimpleFailure);
    }

    @Test
    void removeEmptyOptional() {
        Result<Optional<String>, Failure> empty = optional(Optional.empty());
        Result<String, Failure> result = removeOptional(empty);

        assertTrue(result.hasFailure());
        assertTrue(result.failure().get().toThrowable() instanceof NoSuchElementException);
    }

    @Test
    void removePresentOptional() {
        Result<Optional<String>, Failure> present = optional(Optional.of("hello"));
        Result<String, Failure> result = removeOptional(present);

        assertEquals("hello", result.getOrThrow());
    }

    static class SimpleFailure implements Failure {}

    abstract Result<Integer, Failure> number(Integer integer);
    abstract Result<String, Failure> string(String string);
    abstract Result<Optional<String>, Failure> optional(Optional<String> optional);
    abstract Result<Integer, Failure> failure(Failure failure);
    abstract Result<Integer, Failure> failure(Throwable throwable);
}
