package dev.yila.functional;

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
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
