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

import java.util.function.BiFunction;

/**
 * A record to store 2 sorted values, also known as couple.
 * @param <L> left
 * @param <R> right
 */
public record Pair<L, R>(L left, R right) {

    /**
     * Creates a new Pair with the given left and right values.
     * @param left the left value
     * @param right the right value
     * @param <Left> the type of the left value
     * @param <Right> the type of the right value
     * @return a new Pair
     * @throws IllegalArgumentException if either value is null
     */
    public static <Left, Right> Pair<Left, Right> of(Left left, Right right) {
        return new Pair<>(left, right);
    }

    /**
     * Constructs a new Pair with null checks.
     * @param left the left value
     * @param right the right value
     * @throws IllegalArgumentException if either value is null
     */
    public Pair {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Null is not allowed as any value for Pair.");
        }
    }

    /**
     * Applies a bi-function to the left and right values.
     * @param biFunction the function to apply
     * @param <T> the result type
     * @return the result of applying the function
     */
    public <T> T apply(BiFunction<L, R, T> biFunction) {
        return biFunction.apply(this.left, this.right);
    }
}
