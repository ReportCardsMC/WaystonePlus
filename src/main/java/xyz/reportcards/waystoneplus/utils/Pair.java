package xyz.reportcards.waystoneplus.utils;

import java.util.Objects;

/**
 * A simple location class that can be serialized and deserialized
 * @param <T1> The type of the first object
 * @param <T2> The type of the second object
 */
public class Pair<T1, T2> {

    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
