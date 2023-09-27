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

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ForwardBackwardChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractTokenCheck implements JsonTest {
    private final Set<String> ALLOWED_ABSTRACT_TOKENS = allowedAbstractTokens();

    protected final void checkNodeLabel(final String label) {
        final String normalisedLabel = label.toLowerCase();
        assertTrue(
                ALLOWED_ABSTRACT_TOKENS.contains(normalisedLabel) || ALLOWED_ABSTRACT_TOKENS.contains(label),
                "Got disallowed token: '" + label + "'"
        );
    }

    protected abstract Set<String> getSpecialAllowedTokens();

    private Set<String> allowedAbstractTokens() {
        final Set<String> allowedTokens = new HashSet<>(getSpecialAllowedTokens());

        allowedTokens.add("scripts");
        // stop statement targets
        allowedTokens.addAll(Set.of("all", "this_script", "other_scripts"));
        // procedures, parameters, and their types
        allowedTokens.addAll(Set.of("procedures", "parameters", "parameter", "string", "boolean", "list", "number"));

        addEnumValuesToSet(allowedTokens, AbstractToken.class);

        Arrays.stream(DragMode.DragModeType.values())
                .map(DragMode.DragModeType::toString).forEach(allowedTokens::add);
        Arrays.stream(EventAttribute.EventAttributeType.values())
                .map(EventAttribute.EventAttributeType::toString).forEach(allowedTokens::add);
        Arrays.stream(FixedAttribute.FixedAttributeType.values())
                .map(FixedAttribute.FixedAttributeType::toString).forEach(allowedTokens::add);
        Arrays.stream(FixedDrum.FixedDrumType.values())
                .map(FixedDrum.FixedDrumType::toString).forEach(allowedTokens::add);
        Arrays.stream(FixedInstrument.FixedInstrumentType.values())
                .map(FixedInstrument.FixedInstrumentType::toString).forEach(allowedTokens::add);
        Arrays.stream(FixedLanguage.FixedLanguageType.values())
                .map(FixedLanguage.FixedLanguageType::toString).forEach(allowedTokens::add);
        Arrays.stream(FixedVoice.FixedVoiceType.values())
                .map(FixedVoice.FixedVoiceType::toString).forEach(allowedTokens::add);
        Arrays.stream(ForwardBackwardChoice.ForwardBackwardChoiceType.values())
                .map(ForwardBackwardChoice.ForwardBackwardChoiceType::toString).forEach(allowedTokens::add);
        Arrays.stream(GraphicEffect.GraphicEffectType.values())
                .map(GraphicEffect.GraphicEffectType::toString).forEach(allowedTokens::add);
        Arrays.stream(LayerChoice.LayerChoiceType.values())
                .map(LayerChoice.LayerChoiceType::toString).forEach(allowedTokens::add);
        Arrays.stream(NameNum.NameNumType.values())
                .map(NameNum.NameNumType::toString).forEach(allowedTokens::add);
        Arrays.stream(NumFunct.NumFunctType.values())
                .map(NumFunct.NumFunctType::toString).forEach(allowedTokens::add);
        Arrays.stream(RotationStyle.RotationStyleType.values())
                .map(RotationStyle.RotationStyleType::toString).forEach(allowedTokens::add);
        Arrays.stream(SoundEffect.SoundEffectType.values())
                .map(SoundEffect.SoundEffectType::toString).forEach(allowedTokens::add);
        Arrays.stream(TimeComp.TimeCompType.values())
                .map(TimeComp.TimeCompType::toString).forEach(allowedTokens::add);

        return allowedTokens.stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet());
    }

    protected static void addEnumValuesToSet(final Set<String> set, final Class<? extends Enum<?>> enumType) {
        Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .map(String::toLowerCase)
                .forEach(set::add);
    }
}
