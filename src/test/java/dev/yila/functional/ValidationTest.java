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

import dev.yila.functional.failure.CodeDescriptionFailure;
import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {

    @Test
    void creation() {
        assertNotNull(new Validation());
    }

    @Test
    void successValidation() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 6);

        Result<Map<String, Object>> result = Validation.validate(map,
                CodeDescriptionFailure.create("map", "missing number"),
                m -> m.containsKey("number"));

        assertSame(map, result.getOrThrow());
    }

    @Test
    void failureValidation() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 6);

        Result<Map<String, Object>> result = Validation.validate(map,
                CodeDescriptionFailure.create("map", "missing random"),
                m -> m.containsKey("random"));

        assertEquals("map: missing random", result.failure().get().toString());
    }

    @Test
    void invalidValidations() {
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(
                null, Failure.create(new Exception()), b -> true));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(
                "value", null, b -> true));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(
                "value", Failure.create(new Exception()), null));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(null));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(
                "value", null));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate("value"));
    }

    @Test
    void successMultipleValidation() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", 6);

        Result<Map<String, Object>> result = Validation.validate(map,
                Pair.of(
                        CodeDescriptionFailure.create("map", "missing number"),
                        m -> m.containsKey("number")
                ), Pair.of(
                        CodeDescriptionFailure.create("map", "wrong number"),
                        m -> (Integer) m.get("number") == 6
                ));

        assertSame(map, result.getOrThrow());
    }
}
