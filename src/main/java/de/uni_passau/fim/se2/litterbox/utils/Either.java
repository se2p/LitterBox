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

import java.util.NoSuchElementException;

/**
 * Allows storing exactly one value of type A or B.
 *
 * <p>Ensures that <em>exactly</em> one of {@code left} or {@code right} is {@code null}.
 *
 * @param left Some value.
 * @param right Another value.
 * @param <A> The type of the left value.
 * @param <B> The type of the right value.
 */
public record Either<A, B>(A left, B right) {

    public Either {
        Preconditions.checkState(
                (left == null) ^ (right == null),
                "An Either must store exactly one value."
        );
    }

    /**
     * Retrieves the left value.
     * @return The left value.
     * @throws NoSuchElementException If the left value is {@code null}.
     */
    public A asLeft() {
        if (left == null) {
            throw new NoSuchElementException("No value present");
        }
        return left;
    }

    /**
     * Retrieves the right value.
     * @return The right value.
     * @throws NoSuchElementException If the right value is {@code null}.
     */
    public B asRight() {
        if (right == null) {
            throw new NoSuchElementException("No value present");
        }
        return right;
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasRight() {
        return right != null;
    }
}
