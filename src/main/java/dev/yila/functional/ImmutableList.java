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

import dev.yila.functional.failure.EmptyTailFailure;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Immutable list that can be divided by head and tails.
 * @param <T>
 */
public class ImmutableList<T> {

    /**
     * Create a new immutable list.
     * @param elements
     * @return
     * @param <T>
     */
    @SafeVarargs
    public static <T> ImmutableList<T> create(T... elements) {
        int size = elements.length;
        if (size == 0) {
            throw new IllegalArgumentException("Number of elements can't be 0.");
        }
        for (T t : elements) {
            if (t == null) {
                throw new IllegalArgumentException("Elements can not be null.");
            }
        }
        return new ImmutableList<>(new Elements<>(elements));
    }

    /**
     * Create new Immutable list from a Java List.
     * @param list
     * @return
     * @param <T>
     */
    public static <T> ImmutableList<T> from(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List must contains at least 1 element.");
        }
        for (T t : list) {
            if (t == null) {
                throw new IllegalArgumentException("Elements in the list can not be null.");
            }
        }
        T[] array = (T[]) list.toArray();
        return new ImmutableList<>(new Elements<>(array));
    }

    static class Elements<T> {
        private final T current;
        private final T[] all;
        private Elements<T> nextElements;
        private final int position;

        Elements(T[] all) {
            this.all = all;
            this.position = 0;
            this.current = all[0];
        }

        Elements(T[] all, int position) {
            this.all = all;
            this.position = position;
            this.current = all[position];
        }

        T current() {
            return this.current;
        }

        synchronized Elements<T> next() {
            if (nextElements == null) {
                if (all.length - position == 1) {
                    return null;
                } else {
                    this.nextElements = new Elements<>(all, position + 1);
                }
            }
            return this.nextElements;
        }

        int size() {
            return all.length - position;
        }

        T[] currentCopy() {
            return Arrays.copyOfRange(all, position, all.length - position);
        }

        @Override
        public String toString() {
            Elements<T> next = this.next();
            if (next == null) {
                return this.current.toString();
            }
            return this.current.toString() + ", " + next;
        }
    }

    private final Elements<T> elements;

    private ImmutableList(Elements<T> elements) {
        this.elements = elements;
    }

    /**
     * Get head of the list.
     * @return
     */
    public T head() {
        return elements.current();
    }

    /**
     * Tail of the list.
     * @return
     */
    public Result<ImmutableList<T>> tail() {
        Elements<T> next = elements.next();
        if (next == null) {
            return DirectResult.failure(new EmptyTailFailure());
        }
        return DirectResult.ok(new ImmutableList<>(next));
    }

    /**
     * Size of the list
     * @return
     */
    public int size() {
        return elements.size();
    }

    public <V> ImmutableList<V> map(Function<T, V> function) {
        T[] copy = this.elements.currentCopy();
        Object[] result = new Object[copy.length];
        for (int i = 0; i < copy.length; i++) {
            result[i] = function.apply(copy[i]);
        }
        return new ImmutableList<>(new Elements<>((V[])result));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImmutableList &&
                ((ImmutableList)obj).head().equals(this.head()) &&
                Objects.equals(((ImmutableList)obj).tail().orElse(r -> null),
                        this.tail().orElse(r -> null));
    }

    @Override
    public String toString() {
        return "[" + this.elements.toString() +  "]";
    }
}
