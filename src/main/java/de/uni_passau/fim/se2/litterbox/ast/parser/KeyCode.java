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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import java.util.Optional;

public enum KeyCode {
    ANY_KEY(0),
    SPACE(32),
    LEFT_ARROW(37),
    UP_ARROW(38),
    RIGHT_ARROW(39),
    DOWN_ARROW(40);

    private final int keycode;

    KeyCode(int keycode) {
        this.keycode = keycode;
    }

    public int getKeycode() {
        return keycode;
    }

    public static Optional<KeyCode> tryFromKeycode(final int keycode) {
        return switch (keycode) {
            case 0 -> Optional.of(ANY_KEY);
            case 32 -> Optional.of(SPACE);
            case 37 -> Optional.of(LEFT_ARROW);
            case 38 -> Optional.of(UP_ARROW);
            case 39 -> Optional.of(RIGHT_ARROW);
            case 40 -> Optional.of(DOWN_ARROW);
            default -> Optional.empty();
        };
    }
}
