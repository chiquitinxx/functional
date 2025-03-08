package dev.yila.functional;

import java.util.Arrays;
import java.util.Objects;

public class ImmutableList<T> {

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
        public boolean equals(Object obj) {
            return obj instanceof Node &&
                    Arrays.equals(this.all, ((Node)obj).all);
        }

        @Override
        public int hashCode() {
            return this.current.hashCode();
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

    public T head() {
        return node.current();
    }

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
