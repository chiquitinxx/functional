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
        Exception throwable = new RuntimeException();
        list.add(new ExceptionFailure(throwable));

        MultipleFailures failures = new MultipleFailures(list);
        assertSame(throwable, failures.getFailures().get(1).toException());
        assertEquals("code: description", failures.getFailures()
                .get(0).toException().getMessage());
    }

    @Test
    public void multipleToException() {
        List<Failure> list = new ArrayList<>();
        list.add(CodeDescriptionFailure.create("code", "description"));
        list.add(new ExceptionFailure(new RuntimeException("Fail :(")));

        MultipleFailures failures = new MultipleFailures(list);
        Throwable throwable = failures.toException();
        assertEquals("code: description", throwable.getMessage());
        assertEquals("Fail :(", throwable.getSuppressed()[0].getMessage());
    }
}
