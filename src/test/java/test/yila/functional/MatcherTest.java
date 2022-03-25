package test.yila.functional;

import dev.yila.functional.Matcher;
import dev.yila.functional.Result;
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
        Result<String> result = matcher.get(input);
        assertEquals(result.get(), expectedResult);
    }

    @Test
    void nullNotAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> Matcher.create(Integer.class, String.class).get(null));
    }

    @Test
    void matchFirstMatcher() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(number -> number == 2, number -> "two")
                .on(number -> number > 0, number -> "greaterThan0")
                .get(2);
        assertEquals("two", result.get());
    }

    @Test
    void staticMatcherEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(EQ(2), number -> "two")
                .get(2);
        assertEquals("two", result.get());
    }

    @Test
    void staticMatcherNEQ() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(NEQ(2), number -> "notTwo")
                .get(5);
        assertEquals("notTwo", result.get());
    }

    @Test
    void matchWithoutElse() {
        Result<String> result = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two")
                .get(3);
        assertTrue(result.hasFailures());
        assertEquals("[Not found a matcher for value: 3]", result.getFailuresToString());
    }

    @Test
    void cannotMutateMatchers() {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number < 3, number -> "two");
        Result<String> result = matcher.get(3);
        assertTrue(result.hasFailures());
        Matcher newMatcher = matcher.on(number -> number == 3, number -> "Yes");
        assertTrue(matcher.get(3).hasFailures());
        assertEquals("Yes", newMatcher.get(3).get());
    }

    @Test
    void matcherCanBeUsedMultipleTimes() {
        Matcher<Integer, String> matcher = Matcher.create(Integer.class, String.class)
                .on(number -> number == 1, number -> "one")
                .on(number -> number > 1, number -> "big");
        assertEquals("big", matcher.get(2).get());
        assertEquals("one", matcher.get(1).get());
        assertEquals("big", matcher.get(3).get());
    }

    @Test
    void matcherFromInitialFunctions() {
        Matcher matcher = Matcher.from((Integer number) -> number < 2, number -> "small")
                .orElse(number -> "big");
        assertEquals("big", matcher.get(2).get());
        assertEquals("small", matcher.get(1).get());
        assertEquals("big", matcher.get(3).get());
    }
}
