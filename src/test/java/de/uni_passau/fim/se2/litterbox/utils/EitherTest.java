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

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class EitherTest {

    @Test
    void notBothEmpty() {
        assertThrows(IllegalStateException.class, () -> new Either<>(null, null));
    }

    @Test
    void notBothFilled() {
        assertThrows(IllegalStateException.class, () -> new Either<>("a", 1));
    }

    @Test
    void asLeftOkay() {
        final var left = new Object();
        assertSame(left, new Either<>(left, null).asLeft());
    }

    @Test
    void asLeftNullError() {
        assertThrows(NoSuchElementException.class, () -> new Either<>(null, "").asLeft());
    }

    @Test
    void asRightOkay() {
        final var right = new Object();
        assertSame(right, new Either<>(null, right).asRight());
    }

    @Test
    void asRightNullError() {
        assertThrows(NoSuchElementException.class, () -> new Either<>(5, null).asRight());
    }
}
