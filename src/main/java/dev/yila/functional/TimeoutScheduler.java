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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * A utility class to provide a shared, static {@link ScheduledExecutorService}
 * for scheduling timeouts. This avoids the overhead of creating a new scheduler
 * for every asynchronous operation.
 */
final class TimeoutScheduler {

    private static final ScheduledExecutorService INSTANCE =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "AsyncResult-Timeout-Scheduler");
                thread.setDaemon(true); // Does not prevent JVM from exiting
                return thread;
            });

    private TimeoutScheduler() {}

    /**
     * @return The singleton instance of the ScheduledExecutorService.
     */
    static ScheduledExecutorService getInstance() {
        return INSTANCE;
    }
}
