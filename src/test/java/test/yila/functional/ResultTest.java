package test.yila.functional;

import dev.yila.functional.Result;
import dev.yila.functional.failure.BasicFailure;
import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    private static final String CODE = "someCode";
    private static final String DESCRIPTION = "some description";

    @Test
    void successResult() {
        Result<Integer> result = Result.ok(5);
        assertFalse(result.hasFailures());
        assertTrue(result.notHasFailure(BasicFailure.class));
        assertEquals(5, result.get());
        assertEquals(5, result.orElse(r -> 0));
    }

    @Test
    void failureResult() {
        Result<Integer> result = Result.failure(Failure.create(CODE, DESCRIPTION));
        assertTrue(result.hasFailures());
        assertTrue(result.hasFailure(BasicFailure.class));
        assertEquals(0, result.orElse(r -> 0));
        assertThrows(NullPointerException.class, result::get);
    }

    @Test
    void resultWithMultipleErrors() {
        Result result = Result.failures(List.of(new SomeFailure(), Failure.create(CODE, DESCRIPTION)));
        assertTrue(result.hasFailure(SomeFailure.class));
    }

    @Test
    void chainResults() {
        Result<Integer> result = Result.ok(5);
        Result<Integer> multiplyBy3 = result.map(number -> number * 3);
        assertEquals(15, multiplyBy3.get());
        assertEquals(25, result.flatMap(number -> Result.ok(number * 5)).get());
    }

    static class SomeFailure implements Failure {

    }
}
