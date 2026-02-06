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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LazyResultCachingTest {

    @Test
    void mapExceptionIsCachedAndSupplierRunOnce() {
        AtomicInteger counter = new AtomicInteger(0);
        
        Result<Integer> lazy = LazyResult.create(() -> {
            counter.incrementAndGet();
            return 10;
        }).map(i -> {
            throw new RuntimeException("Mapping Failed");
        });

        assertTrue(lazy.hasFailure(), "Should have failure on first access");
        assertEquals("Mapping Failed", lazy.failure().get().toException().getMessage());
        assertEquals(1, counter.get(), "Supplier should have run once");

        assertTrue(lazy.hasFailure(), "Should have failure on second access");
        assertEquals("Mapping Failed", lazy.failure().get().toException().getMessage());
        assertEquals(1, counter.get(), "Supplier should NOT have run again (result should be cached)");
    }
}
