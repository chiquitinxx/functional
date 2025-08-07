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
 * Class to store 2 sorted values, also known as couple.
 * @param <L> left
 * @param <R> right
 */
public class Pair<L, R> {

    private final L left;
    private final R right;

    public static <Left, Right> Pair<Left, Right> of(Left left, Right right) {
        return new Pair<>(left, right);
    }

    public Pair(L left, R right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Null is not allowed as any value for Pair.");
        }
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public <T> T apply(BiFunction<L, R, T> biFunction) {
        return biFunction.apply(this.left, this.right);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair &&
                Objects.equals(this.left, ((Pair<?, ?>) obj).left) &&
                Objects.equals(this.right, ((Pair<?, ?>) obj).right);
    }

    @Override
    public int hashCode() {
        return this.left.hashCode() + this.right.hashCode();
    }

    @Override
    public String toString() {
        return "Pair(" + this.left + ", " + this.right + ")";
    }
}
