package dev.yila.functional.failure;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MultipleFailuresTest {

    @Test
    public void multiple() {
        List<Failure> list = new ArrayList<>();
        list.add(CodeDescriptionFailure.create("code", "description"));
        Throwable throwable = new RuntimeException();
        list.add(new ThrowableFailure(throwable));

        MultipleFailures failures = new MultipleFailures(list);
        assertSame(throwable, failures.getFailures().get(1).toThrowable());
        assertEquals("code: description", failures.getFailures()
                .get(0).toThrowable().getMessage());
    }

    @Test
    public void multipleToThrowable() {
        List<Failure> list = new ArrayList<>();
        list.add(CodeDescriptionFailure.create("code", "description"));
        list.add(new ThrowableFailure(new RuntimeException("Fail :(")));

        MultipleFailures failures = new MultipleFailures(list);
        Throwable throwable = failures.toThrowable();
        assertEquals("code: description", throwable.getMessage());
        assertEquals("Fail :(", throwable.getSuppressed()[0].getMessage());
    }
}
