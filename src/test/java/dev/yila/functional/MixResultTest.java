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

import dev.yila.functional.failure.Failure;
import dev.yila.functional.failure.MultipleFailures;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixResultTest {

    @RepeatedTest(10)
    public void sequenceAll() {
        Result<String> direct = DirectResult.ok("direct");
        Result<String> async = AsyncResult.create(ThreadPool.get(), () -> "async");
        Result<String> lazy = LazyResult.create(() -> "lazy");

        Result<String> result = DirectResult.sequence(list -> {
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
