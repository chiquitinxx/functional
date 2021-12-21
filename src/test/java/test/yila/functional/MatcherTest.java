package test.yila.functional;

import dev.yila.functional.Matcher;
import dev.yila.functional.Result;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
