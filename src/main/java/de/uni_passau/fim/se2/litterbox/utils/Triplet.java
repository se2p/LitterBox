package de.uni_passau.fim.se2.litterbox.utils;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Triplet<T> extends AbstractCollection<T> {
    private final T first;
    private final T second;
    private final T third;

    public Triplet(final Triplet<? extends T> copy) {
        Objects.requireNonNull(copy);
        this.first = copy.first;
        this.second = copy.second;
        this.third = copy.third;
    }

    public Triplet(final T first, final T second, final T third) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
        this.third = Objects.requireNonNull(third);
    }

    public static <T> Triplet<T> of(final T fst, final T snd, final T thd) {
        return new Triplet<>(fst, snd, thd);
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < 3;
            }

            @Override
            public T next() {
                switch (index++) {
                    case 0:
                        return getFirst();
                    case 1:
                        return getSecond();
                    case 2:
                        return getThird();
                    default:
                        throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Triplet<?> triplet = (Triplet<?>) o;
        return first.equals(triplet.first) && second.equals(triplet.second) && third.equals(triplet.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }

    @Override
    public String toString() {
        return "Triplet(" + first + ", " + second + ", " + third + ')';
    }
}
