/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowTest {

    @Test
    void testAdvanceEmpty() {
        final var empty = Collections.emptyList();
        final var window = new SlidingWindow<>(empty, 1);
        assertFalse(window.tryAdvance(ignored -> { /* no-op */ }));
    }

    @Test
    void testAdvanceSingleton() {
        final var singleton = Collections.singletonList(1);
        final var window = new SlidingWindow<>(singleton, 1);
        assertTrue(window.tryAdvance(ignored -> { /* no-op */ }));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Test
    void testAdvance() {
        final var list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        final var window = new SlidingWindow<>(list, 5);
        final var expected = List.of(
                List.of(1, 2, 3, 4, 5),
                List.of(2, 3, 4, 5, 6),
                List.of(3, 4, 5, 6, 7),
                List.of(4, 5, 6, 7, 8),
                List.of(5, 6, 7, 8, 9)
        );
        final var actual = new LinkedList<>();
        while (window.tryAdvance(s -> actual.add(s.collect(toList())))) { /* no-op */ }
        assertEquals(expected, actual);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Test
    void testAdvanceNarrowWindow() {
        final var list = List.of(1, 2, 3, 4, 5);
        final var window = new SlidingWindow<>(list, 1);
        final var expected = List.of(
                List.of(1),
                List.of(2),
                List.of(3),
                List.of(4),
                List.of(5)
        );
        final var actual = new LinkedList<>();
        while (window.tryAdvance(s -> actual.add(s.collect(toList())))) { /* no-op */ }
        assertEquals(expected, actual);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Test
    void testAdvanceWideWindow() {
        final var list = List.of(1, 2, 3, 4, 5);
        final var window = new SlidingWindow<>(list, list.size());
        final var expected = List.of(list);
        final var actual = new LinkedList<>();
        while (window.tryAdvance(s -> actual.add(s.collect(toList())))) { /* no-op */ }
        assertEquals(expected, actual);
    }

    @Test
    void testAdvanceWindowTooBig() {
        final var list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        final int listSize = list.size();
        final int windowSize = listSize + 1;
        final var window = new SlidingWindow<>(list, windowSize);
        assertFalse(window.tryAdvance(ignored -> { /* no-op */ }));
    }

    @Test
    void testEstimateSizeEmpty() {
        final var empty = Collections.emptyList();
        final var window = new SlidingWindow<>(empty, 1);
        final long expected = 0;
        final long actual = window.estimateSize();
        assertEquals(expected, actual);
    }

    @Test
    void testEstimateSizeSingleton() {
        final var empty = Collections.singletonList(42);
        final var window = new SlidingWindow<>(empty, 1);
        final long expected = 1;
        final long actual = window.estimateSize();
        assertEquals(expected, actual);
    }

    @Test
    void testEstimateSizes() {
        final var list = List.of(1, 1, 1, 1, 1, 1, 1, 1, 1);

        final var narrowWindow = new SlidingWindow<>(list, 1);
        final long expectedNarrow = list.size();
        final long actualNarrow = narrowWindow.estimateSize();
        assertEquals(expectedNarrow, actualNarrow);

        final var wideWindow = new SlidingWindow<>(list, list.size());
        final long expectedWide = 1;
        final long actualWide = wideWindow.estimateSize();
        assertEquals(expectedWide, actualWide);

        final int windowSize = list.size() / 2;
        final var window = new SlidingWindow<>(list, windowSize);
        final long expected = list.size() - windowSize + 1;
        final long actual = window.estimateSize();
        assertEquals(expected, actual);
    }

    @Test
    void testTrySplit() {
        final var items = List.of(1, 2, 3, 4);
        final var window = new SlidingWindow<>(items, items.size());
        assertNull(window.trySplit());
    }

    @Test
    void testCharacteristics() {
        final var window = new SlidingWindow<>(List.of(1, 2, 3, 4, 5), 2);
        final int expected = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL;
        final int actual = window.characteristics();
        assertEquals(expected, actual);
    }

    @Test
    void testSlidingAverageOfThree() {
        final int windowSize = 3;
        final List<Double> numbers = List.of(4.0, 3.0, 1.0, 2.0, 5.0);
        final List<Double> expectedAverages = List.of(
                (4.0 + 3.0 + 1.0) / windowSize,
                (3.0 + 2.0 + 1.0) / windowSize,
                (2.0 + 1.0 + 5.0) / windowSize);
        final List<Double> actualAverages = SlidingWindow.of(numbers, windowSize)
                .map(window -> window.collect(averagingDouble(Double::doubleValue)))
                .toList();
        assertEquals(expectedAverages, actualAverages);
    }

    @Test
    void testIllegalWindowSizes() {
        final var list = List.of(1, 2, 3, 4);
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindow<>(list, 0));
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindow<>(list, -23));
    }
}
