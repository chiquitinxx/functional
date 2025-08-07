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

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A function with two input arguments.
 * @param <I1> input 1
 * @param <I2> input 2
 * @param <O> output
 */
public class Fun2<I1, I2, O> {

    private final BiFunction<I1, I2, O> function;

    public Fun2(BiFunction<I1, I2, O> function) {
        Objects.requireNonNull(function);
        this.function = function;
    }

    public static <Input1, Input2, Output> Fun2<Input1, Input2, Output> from(BiFunction<Input1, Input2, Output> function) {
        return new Fun2<>(function);
    }

    public Result<O> apply(I1 i1, I2 i2) {
        return DirectResult.ok(function.apply(i1, i2));
    }

    public Fun<I2, O> curry(I1 i1) {
        return Fun.from(i2 -> function.apply(i1, i2));
    }
}
