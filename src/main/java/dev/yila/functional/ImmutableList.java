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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        ImmutableList<T> list = null;
        for (int i = elements.length - 1; i >= 0; i--) {
            list = new ImmutableList<>(elements[i], list);
        }
        return list;
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
        ImmutableList<T> il = null;
        for (int i = list.size() - 1; i >= 0; i--) {
            il = new ImmutableList<>(list.get(i), il);
        }
        return il;
    }

    private final T head;
    private final ImmutableList<T> tail;
    private final int size;

    private ImmutableList(T head, ImmutableList<T> tail) {
        this.head = head;
        this.tail = tail;
        this.size = 1 + (tail == null ? 0 : tail.size());
    }

    /**
     * Get head of the list.
     * @return
     */
    public T head() {
        return this.head;
    }

    /**
     * Tail of the list.
     * @return
     */
    public Result<ImmutableList<T>> tail() {
        if (tail == null) {
            return DirectResult.failure(new EmptyTailFailure());
        }
        return DirectResult.ok(tail);
    }

    /**
     * Size of the list
     * @return
     */
    public int size() {
        return this.size;
    }

    /**
     * Map function on elements in ImmutableList
     * @param function
     * @return
     * @param <V>
     */
    public <V> ImmutableList<V> map(Function<T, V> function) {
        ImmutableList<V> mappedTail = tail == null ? null : tail.map(function);
        return new ImmutableList<>(function.apply(head), mappedTail);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImmutableList)) {
            return false;
        }
        ImmutableList<?> other = (ImmutableList<?>) obj;
        if (!Objects.equals(this.head, other.head)) {
            return false;
        }
        return Objects.equals(this.tail, other.tail);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(head);
        result = 31 * result + Objects.hashCode(tail);
        return result;
    }

    @Override
    public String toString() {
        return head.toString() + Optional.ofNullable(tail)
                .map(il -> ", " + il).orElse("");
    }
}
