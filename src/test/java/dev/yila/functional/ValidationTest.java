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
                null, Failure.create(new Throwable()), b -> true));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(
                "value", null, b -> true));
        assertThrows(IllegalArgumentException.class, () -> Validation.validate(
                "value", Failure.create(new Throwable()), null));
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
