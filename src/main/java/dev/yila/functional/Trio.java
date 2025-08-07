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

/**
 * Class to store 3 sorted values.
 * @param <F> first
 * @param <S> second
 * @param <T> third
 */
public class Trio<F, S, T> {

    private final F first;
    private final S second;
    private final T third;

    /**
     * Create a trio.
     * @param first value
     * @param second value
     * @param third value
     * @return
     * @param <First> type
     * @param <Second> type
     * @param <Third> type
     */
    public static <First, Second, Third> Trio<First, Second, Third> of(First first, Second second, Third third) {
        return new Trio<>(first, second, third);
    }

    public Trio(F first, S second, T third) {
        if (first == null || second == null || third == null) {
            throw new IllegalArgumentException("Null is not allowed as any value for Trio.");
        }
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Trio &&
                Objects.equals(this.first, ((Trio<?, ?, ?>) obj).first) &&
                Objects.equals(this.second, ((Trio<?, ?, ?>) obj).second) &&
                Objects.equals(this.third, ((Trio<?, ?, ?>) obj).third);
    }

    @Override
    public int hashCode() {
        return this.first.hashCode() + this.second.hashCode() + this.third.hashCode();
    }

    @Override
    public String toString() {
        return "Trio(" + this.first + ", " + this.second + ", "+ this.third + ")";
    }
}
