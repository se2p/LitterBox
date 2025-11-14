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
package de.uni_passau.fim.se2.litterbox.analytics.hint;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.HintPlaceholder;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MessageNeverSent;

import java.util.*;

public abstract class MessageNeverSentHintFactory {
    public static final String MESSAGE_IN_SAY_OR_THINK = "message_never_sent_say_think";
    public static final String TOUCHING_USED = "message_never_sent_touching";

    public static Hint generateHint(String messageText, Map<String, Set<String>> sayText, Map<String, Set<String>> thinkText, Map<String, Set<String>> touchingSprites) {
        Hint hint;

        for (Map.Entry<String, Set<String>> entry : sayText.entrySet()) {
            if (entry.getKey().contains(messageText)) {
                hint = Hint.fromKey(MESSAGE_IN_SAY_OR_THINK);
                hint.setParameter(Hint.HINT_SAY_THINK, new HintPlaceholder.Translatable("say"));
                hint.setParameter(Hint.HINT_SPRITES, generateSpritesText(entry.getValue()));
                hint.setParameter(Hint.HINT_MESSAGE, messageText);
                return hint;
            }
        }

        for (Map.Entry<String, Set<String>> entry : thinkText.entrySet()) {
            if (entry.getKey().contains(messageText)) {
                hint = Hint.fromKey(MESSAGE_IN_SAY_OR_THINK);
                hint.setParameter(Hint.HINT_SAY_THINK, new HintPlaceholder.Translatable("think"));
                hint.setParameter(Hint.HINT_SPRITES, generateSpritesText(entry.getValue()));
                hint.setParameter(Hint.HINT_MESSAGE, messageText);
                return hint;
            }
        }

        for (Map.Entry<String, Set<String>> entry : touchingSprites.entrySet()) {
            if (messageText.contains(entry.getKey())) {
                hint = Hint.fromKey(TOUCHING_USED);
                hint.setParameter(Hint.HINT_SPRITES, generateSpritesText(entry.getValue()));
                hint.setParameter(Hint.HINT_SPRITE, entry.getKey());
                hint.setParameter(Hint.HINT_MESSAGE, messageText);
                return hint;
            }
        }

        hint = Hint.fromKey(MessageNeverSent.NAME);
        hint.setParameter(Hint.HINT_MESSAGE, messageText);

        return hint;
    }

    private static String generateSpritesText(Set<String> strings) {
        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (String string : strings) {
            builder.append(string);
            if (i < strings.size()) {
                builder.append(", ");
            }
            i++;
        }
        return builder.toString();
    }

    public static Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(MessageNeverSent.NAME);
        keys.add(MESSAGE_IN_SAY_OR_THINK);
        keys.add(TOUCHING_USED);
        return keys;
    }
}
