package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MultipleFailures;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixResultTest {

    @RepeatedTest(10)
    public void joinAll() {
        Result<String> direct = DirectResult.ok("direct");
        Result<String> async = AsyncResult.create(CompletableFuture.completedFuture("async"));
        Result<String> lazy = LazyResult.create(() -> "lazy");

        Result<String> result = Result.join(list -> {
            StringBuilder all = new StringBuilder();
            list.forEach(all::append);
            return all.toString();
        }, direct, async, lazy);

        assertEquals("directasynclazy", result.getOrThrow());
    }

    @Test
    public void multipleFailures() {
        Failure multiple = new MultipleFailures(
                Failure.create(new Throwable("hello")),
                Failure.create("message"),
                Failure.create("code", "description")
        );

        assertEquals("[ThrowableFailure: java.lang.Throwable: hello, message, code: description]", multiple.toString());
    }
}
