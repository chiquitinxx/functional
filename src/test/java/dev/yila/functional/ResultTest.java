package dev.yila.functional;

import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.DescriptionFailure;
import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static dev.yila.functional.Result.join;
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
        ThrowingFunction<Integer, Integer, RuntimeException> function =
                (input) -> input + 2;
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
        assertEquals("ThrowableFailure: java.lang.RuntimeException: Fail", result.failure().get().toString());
    }

    @Test
    void checkedExceptionInMap() {
        ThrowingFunction throwingFunction = (input) -> {
            throw new RuntimeException("Fail");
        };
        Result<Integer> result = number(3)
                .map(throwingFunction, RuntimeException.class);
        assertEquals("ThrowableFailure: java.lang.RuntimeException: Fail", result.failure().get().toString());
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
        Result<?> result = failure(new RuntimeException(exceptionMessage));
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
        Result<String> result = join(present);

        assertEquals("hello", result.getOrThrow());
    }

    @Test
    void flatMapFun() {
        Fun<String, String> upper = Fun.from(String::toUpperCase);

        Result<String> toUpper = string("hello").flatMap(upper);

        assertEquals("HELLO", toUpper.getOrThrow());
    }

    @Test
    void flatMapFunAfterFailure() {
        Fun<Integer, Integer> upper = Fun.from(n -> n * 2);

        Result<Integer> twoTimes = failure(Failure.create("error")).flatMap(upper);

        assertTrue(twoTimes.hasFailure());
    }

    @Test
    void flatMapFunFailure() {
        ThrowingFunction<String, String, NullPointerException> function = s -> {
            throw new NullPointerException("null");
        };
        Fun<String, String> upper = Fun.from(function, NullPointerException.class);

        Result<String> result = string("hello").flatMap(upper);

        assertTrue(result.hasFailure());
    }

    static class SimpleFailure implements Failure {}

    abstract Result<Integer> number(Integer integer);
    abstract Result<String> string(String string);
    abstract Result<Optional<String>> optional(Optional<String> optional);
    abstract Result<Integer> failure(Failure failure);
    abstract Result<Integer> failure(Throwable throwable);
}
