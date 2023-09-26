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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util;

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScriptEntityNameVisitor;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.Optional;
import java.util.stream.Collectors;

public final class NodeNameUtil {

    private static final char SPLIT_DELIMITER = '|';

    private NodeNameUtil() {
        throw new IllegalCallerException("utility class constructor");
    }

    /**
     * Normalize sprite name.
     *
     * @param actor The sprite for which the normalized name should be computed.
     * @return The normalized sprite name. An empty optional instead of an empty name.
     */
    public static Optional<String> normalizeSpriteName(final ActorDefinition actor) {
        final String spriteName = actor.getIdent().getName();
        final String splitName = StringUtil.splitToNormalisedSubtokenStream(spriteName, "|")
                .filter(subtoken -> !subtoken.matches("^\\d+$"))
                .collect(Collectors.joining(String.valueOf(SPLIT_DELIMITER)));

        if (splitName.isEmpty()) {
            return Optional.empty();
        } else {
            final String truncated = truncateName(splitName);
            return Optional.of(truncated);
        }
    }

    /**
     * Normalizes the sprite name to only include latin base alphabet characters a-z.
     *
     * <p>For latin characters with diacritic marks, those are removed and the base character is retained
     * (e.g., ö to o, á to a).
     *
     * @param actor The sprite for which the normalized name should be computed.
     * @return The normalized sprite name. An empty optional instead of an empty name.
     */
    public static Optional<String> normalizeSpriteNameLatinOnly(final ActorDefinition actor) {
        final String baseSpriteName = Normalizer.normalize(actor.getIdent().getName(), Normalizer.Form.NFKD);
        final String spriteName = StringUtils.stripAccents(baseSpriteName);

        final String splitName = StringUtil.splitToNormalisedSubtokenStream(spriteName, "|")
                .map(subtoken -> subtoken.replaceAll("[^a-zA-Z]", ""))
                .filter(subtoken -> !subtoken.isEmpty())
                .collect(Collectors.joining(String.valueOf(SPLIT_DELIMITER)));

        if (splitName.isEmpty()) {
            return Optional.empty();
        } else {
            final String truncated = truncateName(splitName);
            return Optional.of(truncated);
        }
    }

    private static String truncateName(final String name) {
        if (name.length() <= 100) {
            return name;
        }

        int truncatePoint = 100;
        while (shouldBeRemovedFromEnd(name.charAt(truncatePoint - 1))) {
            truncatePoint -= 1;
        }

        return StringUtils.truncate(name, truncatePoint);
    }

    private static boolean shouldBeRemovedFromEnd(final char character) {
        // should neither end with split marker '|' nor end with half of a multibyte Unicode character
        return character == SPLIT_DELIMITER || Character.isHighSurrogate(character);
    }

    /**
     * Checks if the actor has a default name that was generated upon actor creation by Scratch itself.
     *
     * <p>A default name is a translation of ‘sprite’ ({@link Constants#DEFAULT_SPRITE_NAMES}), followed by a number.
     *
     * @param actor Some actor.
     * @return True, if the actor has an automatically generated name.
     */
    public static boolean hasDefaultName(final ActorDefinition actor) {
        // no special replacements except removal of the numbers needed: if the non-numeric part is not in the list of
        // known default names, it is not a default name
        final String spriteName = actor.getIdent().getName().toLowerCase().replaceAll("\\d+", "");
        return isDefaultName(spriteName);
    }

    /**
     * Checks if a normalized sprite name is one of the default names.
     *
     * @param normalisedSpriteLabel A <emph>normalised</emph> sprite name.
     * @return If the name is a default name generated by Scratch.
     */
    private static boolean isDefaultName(final String normalisedSpriteLabel) {
        return Constants.DEFAULT_SPRITE_NAMES.contains(normalisedSpriteLabel);
    }

    /**
     * Builds a globally unique name for a script.
     *
     * <p>Combines the name/projectID of the program with the unique name of a script to create a globally unique
     * identifier for the script.
     *
     * @param program The program the script belongs to.
     * @param scriptEntity Some script inside {@code program}.
     * @return A unique name based on the program and script ids. Empty if no ID could be generated for the script.
     */
    public static Optional<String> getScriptEntityFullName(Program program, ScriptEntity scriptEntity) {
        return ScriptEntityNameVisitor.getScriptName(scriptEntity)
                .map(name -> program.getIdent().getName() + "_" + name);
    }

    public static Optional<String> getScriptEntityName(ScriptEntity node) {
        return ScriptEntityNameVisitor.getScriptName(node);
    }
}
