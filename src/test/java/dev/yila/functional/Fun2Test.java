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
