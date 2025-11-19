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
package de.uni_passau.fim.se2.litterbox.ast.util;

import de.uni_passau.fim.se2.litterbox.analytics.HintPlaceholder;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.Optional;

public final class KeyValueTranslator {

    public static String getKeyValue(final IssueTranslator translator, final int numberValue) {
        final Optional<KeyCode> keyCode = KeyCode.tryFromKeycode(numberValue);

        if (keyCode.isPresent()) {
            return switch (keyCode.get()) {
                case UP_ARROW -> translator.getInfo("up_arrow");
                case DOWN_ARROW -> translator.getInfo("down_arrow");
                case LEFT_ARROW -> translator.getInfo("left_arrow");
                case RIGHT_ARROW -> translator.getInfo("right_arrow");
                case SPACE -> translator.getInfo("space");
                case ANY_KEY -> translator.getInfo("any");
            };
        } else {
            return String.valueOf((char) numberValue);
        }
    }

    public static HintPlaceholder getKeyValue(final int numberValue) {
        final Optional<KeyCode> keyCode = KeyCode.tryFromKeycode(numberValue);

        if (keyCode.isPresent()) {
            return switch (keyCode.get()) {
                case UP_ARROW -> new HintPlaceholder.Translatable("up_arrow");
                case DOWN_ARROW -> new HintPlaceholder.Translatable("down_arrow");
                case LEFT_ARROW -> new HintPlaceholder.Translatable("left_arrow");
                case RIGHT_ARROW -> new HintPlaceholder.Translatable("right_arrow");
                case SPACE -> new HintPlaceholder.Translatable("space");
                case ANY_KEY -> new HintPlaceholder.Translatable("any");
            };
        } else {
            return new HintPlaceholder.Value(String.valueOf((char) numberValue));
        }
    }
}
