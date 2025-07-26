package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunTest {

    @Test
    public void createFunction() {
        Fun<Integer, Integer> twoTimes = Fun.from((a) -> 2 * a);

        assertEquals(20, twoTimes.apply(10).getOrThrow());
    }

    @Test
    public void composeFunctions() {
        Fun<Integer, Integer> twoTimes = Fun.from((a) -> 2 * a);
        Fun<Integer, Integer> threeTimes = Fun.from((a) -> 3 * a);

        Fun<Integer, Integer> sixTimes = Fun.compose(twoTimes, threeTimes);
        assertEquals(30, sixTimes.apply(5).getOrThrow());
    }

    @Test
    public void successThrowableFunction() {
        ThrowingFunction<String, String, RuntimeException> function = String::toUpperCase;
        Fun<String, String> upper = Fun.from(function, RuntimeException.class);

        assertEquals("WORLD", upper.apply("world").getOrThrow());
    }

    @Test
    public void exceptionThrowableFunction() {
        ThrowingFunction<String, String, NullPointerException> function = s -> {
            throw new NullPointerException("yup");
        };
        Fun<String, String> upper = Fun.from(function, NullPointerException.class);

        assertEquals("yup", upper.apply("tt").failure().get().toThrowable().getMessage());
    }

    @Test
    public void runtimeExceptionThrowableFunction() {
        ThrowingFunction<String, String, NullPointerException> function = s -> {
            throw new RuntimeException("yup");
        };
        Fun<String, String> upper = Fun.from(function, NullPointerException.class);

        assertThrows(RuntimeException.class, () -> upper.apply("tt"));
    }
}
