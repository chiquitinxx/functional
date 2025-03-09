package dev.yila.functional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Immutable list that can be divided by head and tails.
 * @param <T>
 */
public class ImmutableList<T> {

    /**
     *
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
        return new ImmutableList<>(new Node<>(elements));
    }

    public static <T> ImmutableList<T> from(List<T> list) {
        if (list == null || list.size() == 0) {
            throw new IllegalArgumentException("List must contains at least 1 element.");
        }
        for (T t : list) {
            if (t == null) {
                throw new IllegalArgumentException("Elements in the list can not be null.");
            }
        }
        T[] array = (T[]) list.stream().toArray();
        return new ImmutableList<>(new Node<>(array));
    }

    static class Node<T> {
        private final T current;
        private final T[] all;
        private Node<T> nextNode;

        Node(T[] all) {
            this.all = all;
            this.current = all[0];
        }

        T current() {
            return this.current;
        }

        Node<T> next() {
            if (nextNode == null) {
                if (all.length == 1) {
                    return null;
                } else {
                    this.nextNode = new Node<>(Arrays.copyOfRange(all, 1, all.length));
                }
            }
            return this.nextNode;
        }

        @Override
        public String toString() {
            Node<T> next = this.next();
            if (next == null) {
                return this.current.toString();
            }
            return this.current.toString() + ", " + next;
        }
    }

    private final Node<T> node;

    private ImmutableList(Node<T> node) {
        this.node = node;
    }

    /**
     * Get head of the list.
     * @return
     */
    public T head() {
        return node.current();
    }

    /**
     * Tail of the list.
     * @return
     */
    public Result<ImmutableList<T>> tail() {
        Node<T> next = node.next();
        if (next == null) {
            return DirectResult.failure(new EmptyTailFailure());
        }
        return DirectResult.ok(new ImmutableList<>(next));
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
        return "[" + this.node.toString() +  "]";
    }
}
