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

    /**
     * Create a new pair
     * @param first The first object
     * @param second The second object
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Create a new pair
     * @param o The object to create the pair from
     * @return The pair
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    /**
     * Get the hash code of the pair
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
