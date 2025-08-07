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

public interface Failure {

    static Failure create(Throwable t) {
        return new ThrowableFailure(t);
    }

    static Failure create(String message) {
        return new DescriptionFailure(message);
    }

    static Failure create(String code, String message) {
        return new CodeDescriptionFailure(code, message);
    }

    default Throwable toThrowable() {
        if (this instanceof ThrowableFailure) {
            return ((ThrowableFailure)this).getThrowable();
        } else {
            return new Throwable(this.toString());
        }
    }
}
