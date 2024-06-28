package dev.yila.functional;

import dev.yila.functional.failure.Failure;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixResultTest {

    @RepeatedTest(10)
    public void joinAll() {
        Result<String, Failure> direct = DirectResult.ok("direct");
        Result<String, Failure> async = AsyncResult.create(CompletableFuture.completedFuture("async"));
        Result<String, Failure> lazy = LazyResult.create(() -> "lazy");

        Result<String, Failure> result = Result.join(list -> {
            StringBuilder all = new StringBuilder();
            list.forEach(all::append);
            return all.toString();
        }, direct, async, lazy);

        assertEquals("directasynclazy", result.getOrThrow());
    }
}
