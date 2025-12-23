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

/**
 * A failure that wraps a Throwable.
 */
public class ThrowableFailure implements Failure {

    private final Throwable throwable;

    /**
     * Creates a new ThrowableFailure.
     * 
     * @param throwable the throwable to wrap
     */
    public ThrowableFailure(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Returns the wrapped throwable.
     * 
     * @return the wrapped throwable
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public String toString() {
        return "ThrowableFailure: " + throwable.getClass().getName() + ": " + throwable.getMessage();
    }
}
