package dev.yila.functional;

import org.junit.jupiter.api.RepeatedTest;

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
}
