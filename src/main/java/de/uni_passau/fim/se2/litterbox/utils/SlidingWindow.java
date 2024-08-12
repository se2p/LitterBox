/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Allows iteration over a collection, grouping its elements into blocks of fixed size by passing a
 * "sliding window" over them. The window will be empty if the source collection contains fewer
 * elements than the window size.
 *
 * @param <T> the element type
 * @author Sebastian Schweikl
 */
public class SlidingWindow<T> implements Spliterator<Stream<T>> {

    private static final int characteristics = ORDERED | SIZED | NONNULL;

    /**
     * Iterator over the source collection from which the sliding window was constructed.
     */
    private final Iterator<T> source;

    /**
     * The current window.
     */
    private final Queue<T> window;

    /**
     * The window size.
     */
    private final int windowSize;

    /**
     * The number of times the window can be sled.
     */
    private final int numberOfSlides;

    private final boolean windowTooBig;

    public SlidingWindow(final Collection<T> source, final int windowSize) {
        Preconditions.checkNotNull(source);
        Preconditions.checkArgument(windowSize > 0, "Illegal window size: " + windowSize);

        this.source = source.iterator();
        this.window = new ArrayDeque<>(windowSize);
        this.windowSize = windowSize;

        int numberOfSlides = source.size() - windowSize + 1;
        boolean windowTooBig = numberOfSlides < 0;

        if (windowTooBig) {
            this.numberOfSlides = 0;
            this.windowTooBig = true;
        } else {
            this.numberOfSlides = numberOfSlides;
            this.windowTooBig = false;
        }
    }

    public SlidingWindow(final T[] source, final int windowSize) {
        this(Arrays.asList(source), windowSize);
    }

    public static <T> Stream<Stream<T>> of(final Collection<T> source, final int windowSize) {
        return StreamSupport.stream(new SlidingWindow<>(source, windowSize), false);
    }

    public static <T> Stream<Stream<T>> of(final T[] source, final int windowSize) {
        return of(Arrays.asList(source), windowSize);
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Stream<T>> action) {
        // Terminate early if window is too big for the source collection.
        if (windowTooBig) {
            return false;
        }

        // The window must be filled with elements from the source before it can be sled.
        while (window.size() < windowSize) {
            if (source.hasNext()) {
                window.offer(source.next());
            } else {
                return false;
            }
        }

        // Once we have enough elements in the window, we can perform the action on the window.
        action.accept(window.stream());

        // Afterwards, we remove the head element from the window so that the window can advance.
        window.poll();

        return true;
    }

    /**
     * Returns {@code null}. Partitioning a sliding window is not supported.
     *
     * @return {@code null}
     */
    @Override
    public Spliterator<Stream<T>> trySplit() {
        return null; // not supported
    }

    @Override
    public long estimateSize() {
        return numberOfSlides;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }
}
