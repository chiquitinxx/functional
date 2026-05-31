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

import static org.junit.jupiter.api.Assertions.*;

public class PairTest {

    @Test
    void nullAreNotAllowedAsValues() {
        assertThrows(IllegalArgumentException.class, () -> new Pair<>(null, null));
        assertThrows(IllegalArgumentException.class, () -> new Pair<>(1, null));
        assertThrows(IllegalArgumentException.class, () -> new Pair<>(null, 2));
    }

    @Test
    void usePair() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);
        assertEquals(5, pair.right());
        assertEquals("hello", pair.left());
    }

    @Test
    void applyFunctionToPair() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);
        Integer result = pair.apply((left, right) -> right * 7);
        assertEquals(35, result);
    }

    @Test
    void equalPair() {
        Pair<String, Integer> hello5 = new Pair<>("hello", 5);
        Pair<String, Integer> hello6 = new Pair<>("hello", 6);
        Pair<String, Integer> bye5 = new Pair<>("bye", 5);
        Pair<String, Integer> sameHello5 = new Pair<>("hello", 5);

        assertFalse(hello5.equals("patata"));
        assertNotEquals(hello5, hello6);
        assertNotEquals(hello5, bye5);
        assertNotEquals(bye5, hello6);

        assertEquals(hello5, sameHello5);
    }

    @Test
    void hashcode() {
        Pair<String, Integer> pair = new Pair<>("hello", 5);

        // Record's hashCode combines components, just verify it's consistent
        Pair<String, Integer> samePair = new Pair<>("hello", 5);
        assertEquals(pair.hashCode(), samePair.hashCode());
    }

    @Test
    void pairToString() {
        Pair<String, Integer> hello5 = new Pair<>("hello", 5);
        // Record's toString format: Pair[left=hello, right=5]
        String toString = hello5.toString();
        assertTrue(toString.contains("hello"));
        assertTrue(toString.contains("5"));
    }
}
