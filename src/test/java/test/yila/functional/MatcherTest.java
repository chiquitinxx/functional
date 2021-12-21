package test.yila.functional;

import dev.yila.functional.Matcher;
import dev.yila.functional.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class MatcherTest {

    @ParameterizedTest
    @CsvSource({
            "1, one",
            "2, two",
            "3, other"
    })
    void matchNumbers(Integer input, String expectedResult) {
        Result<String> result = Matcher.create(input, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two")
                .orElse(number -> "other");
        assertEquals(result.get(), expectedResult);
    }

    @Test
    void nullNotAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> Matcher.create(null, String.class));
    }

    @Test
    void matchFirstMatcher() {
        Result<String> result = Matcher.create(2, String.class)
                .on(number -> number == 2, number -> "two")
                .on(number -> number > 0, number -> "greaterThan0")
                .get();
        assertEquals("two", result.get());
    }

    @Test
    void matchWithoutElse() {
        Result<String> result = Matcher.create(3, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two")
                .get();
        assertTrue(result.hasFailures());
        assertEquals("[Not found a matcher for value: 3]", result.getFailuresToString());
    }

    @Test
    void changingMatcherNotChangeResult() {
        Matcher<Integer, String> matcher = Matcher.create(3, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two");
        Result<String> result = matcher.get();
        assertTrue(result.hasFailures());
        matcher.on(number -> number == 3, number -> "Yes");
        result = matcher.get();
        assertTrue(result.hasFailures());
    }
}
