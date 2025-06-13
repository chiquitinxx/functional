package dev.yila.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static dev.yila.functional.Matcher.EQ;
import static dev.yila.functional.Matcher.NEQ;
import static org.junit.jupiter.api.Assertions.*;

public class MatcherTest {

    @ParameterizedTest
    @CsvSource({
            "-1, lessThan3",
            "0, lessThan3",
            "1, one",
            "2, lessThan3",
            "3, other",
            "4, other"
    })
    void matchNumbers(Integer input, String expectedResult) {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "lessThan3")
                .orElse(number -> "other");
        DirectResult<String> result = matcher.resultFor(input);
        assertEquals(result.getOrThrow(), expectedResult);
    }

    @Test
    void nullNotAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> Matcher.create(Integer.class, String.class).resultFor(null));
    }

    @Test
    void matchFirstMatcher() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(number -> number == 2, number -> "two")
                .on(number -> number > 0, number -> "greaterThan0")
                .resultFor(2);
        assertEquals("two", result.getOrThrow());
    }

    @Test
    void staticMatcherEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(EQ(2), number -> "two")
                .resultFor(2);
        assertEquals("two", result.getOrThrow());
    }

    @Test
    void staticMatcherNEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(NEQ(2), number -> "notTwo")
                .resultFor(5);
        assertEquals("notTwo", result.getOrThrow());
    }

    @Test
    void staticMatcherEQAndNEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(NEQ(2), number -> "notTwo")
                .on(EQ(3), number -> "three")
                .orElse(number -> "isTwo")
                .resultFor(2);
        assertEquals("isTwo", result.getOrThrow());
    }

    @Test
    void matchWithoutElse() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two")
                .resultFor(3);
        assertTrue(result.hasFailure());
        assertEquals("Not found a matcher for value: 3", result.failure().get().toString());
    }

    @Test
    void cannotMutateMatchers() {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two");
        Result<String> result = matcher.resultFor(3);
        assertTrue(result.hasFailure());
        Matcher newMatcher = matcher.on(number -> number == 3, number -> "Yes");
        assertTrue(matcher.resultFor(3).hasFailure());
        assertEquals("Yes", newMatcher.resultFor(3).getOrThrow());
    }

    @Test
    void matcherCanBeUsedMultipleTimes() {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number > 1, number -> "big");
        assertEquals("big", matcher.resultFor(2).getOrThrow());
        assertEquals("one", matcher.resultFor(1).getOrThrow());
        assertEquals("big", matcher.resultFor(3).getOrThrow());
    }

    @Test
    void matcherFromInitialFunctions() {
        Matcher<Integer, String> matcher = Matcher
                .from((Integer number) -> number < 2, number -> "small")
                .orElse(number -> "big");
        assertEquals("big", matcher.resultFor(2).getOrThrow());
        assertEquals("small", matcher.resultFor(1).getOrThrow());
        assertEquals("big", matcher.resultFor(3).getOrThrow());
    }
}
