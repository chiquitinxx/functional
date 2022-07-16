package dev.yila.functional;

import java.util.function.BiFunction;

/**
 * Class to store 2 values
 * @param <L>
 * @param <R>
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
}
