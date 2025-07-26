package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Fun2Test {

    @Test
    public void createFunction() {
        Fun2<Integer, Integer, Integer> sum = Fun2.from(Integer::sum);

        assertEquals(25, sum.apply(19, 6).getOrThrow());
    }

    @Test
    public void curryFunction() {
        Fun2<Integer, Integer, Integer> sum = Fun2.from(Integer::sum);
        Fun<Integer, Integer> plus10 = sum.curry(10);

        assertEquals(12, plus10.apply(2).getOrThrow());
    }
}
