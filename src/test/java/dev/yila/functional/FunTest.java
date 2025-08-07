/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.yila.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunTest {

    @Test
    public void createFunction() {
        Fun<Integer, Integer> twoTimes = Fun.from(a -> 2 * a);

        assertEquals(20, twoTimes.apply(10).getOrThrow());
    }

    @Test
    public void composeFunctions() {
        Fun<Integer, Integer> twoTimes = Fun.from(a -> 2 * a);
        Fun<Integer, Integer> threeTimes = Fun.from(a -> 3 * a);

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

        assertThrows(RuntimeException.class, () -> upper.apply("tt"), "yup");
    }
}
