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
