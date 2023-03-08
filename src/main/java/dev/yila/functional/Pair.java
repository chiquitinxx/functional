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
    public String toString() {
        return "Pair(" + this.left + ", " + this.right + ")";
    }
}
