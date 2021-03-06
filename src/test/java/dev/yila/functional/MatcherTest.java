package dev.yila.functional;

import dev.yila.functional.failure.MatcherNotFoundFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static dev.yila.functional.Matcher.EQ;
import static dev.yila.functional.Matcher.NEQ;
import static org.junit.jupiter.api.Assertions.*;

public class MatcherTest {

    @ParameterizedTest
    @CsvSource({
            "1, one",
            "2, two",
            "3, other"
    })
    void matchNumbers(Integer input, String expectedResult) {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two")
                .orElse(number -> "other");
        Result<String> result = matcher.result(input);
        assertEquals(result.getOrThrow(), expectedResult);
    }

    @Test
    void nullNotAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> Matcher.create(Integer.class, String.class).result(null));
    }

    @Test
    void matchFirstMatcher() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(number -> number == 2, number -> "two")
                .on(number -> number > 0, number -> "greaterThan0")
                .result(2);
        assertEquals("two", result.getOrThrow());
    }

    @Test
    void staticMatcherEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(EQ(2), number -> "two")
                .result(2);
        assertEquals("two", result.getOrThrow());
    }

    @Test
    void staticMatcherNEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(NEQ(2), number -> "notTwo")
                .result(5);
        assertEquals("notTwo", result.getOrThrow());
    }

    @Test
    void matchWithoutElse() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two")
                .result(3);
        assertTrue(result.hasFailure(MatcherNotFoundFailure.class));
        assertEquals("[Not found a matcher for value: 3]", result.getFailuresToString());
    }

    @Test
    void cannotMutateMatchers() {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two");
        Result<String> result = matcher.result(3);
        assertTrue(result.hasFailures());
        Matcher newMatcher = matcher.on(number -> number == 3, number -> "Yes");
        assertTrue(matcher.result(3).hasFailures());
        assertEquals("Yes", newMatcher.result(3).getOrThrow());
    }

    @Test
    void matcherCanBeUsedMultipleTimes() {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number > 1, number -> "big");
        assertEquals("big", matcher.result(2).getOrThrow());
        assertEquals("one", matcher.result(1).getOrThrow());
        assertEquals("big", matcher.result(3).getOrThrow());
    }

    @Test
    void matcherFromInitialFunctions() {
        Matcher matcher = Matcher.from((Integer number) -> number < 2, number -> "small")
                .orElse(number -> "big");
        assertEquals("big", matcher.result(2).getOrThrow());
        assertEquals("small", matcher.result(1).getOrThrow());
        assertEquals("big", matcher.result(3).getOrThrow());
    }
}
