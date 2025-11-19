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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hint {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)}}");

    public static final String ESCAPE_CHARACTER = "%";

    public static final String HINT_SPRITE = "SPRITE";
    public static final String HINT_SPRITES = "SPRITES";
    public static final String HINT_VARIABLE = "VARIABLE";
    public static final String HINT_VARIABLE1 = "VARIABLE1";
    public static final String HINT_VARIABLE2 = "VARIABLE2";
    public static final String HINT_MESSAGE = "MESSAGE";
    public static final String HINT_KEY = "KEY";
    public static final String HINT_SAY_THINK = "SAYTHINK";
    public static final String THEN_ELSE = "THENELSE";
    public static final String BLOCK_NAME = "BLOCKNAME";
    public static final String EVENT_HANDLER = "EVENTHANDLER";
    public static final String EVENT = "EVENT";
    public static final String STATEMENT = "STATEMENT";
    public static final String PROCEDURE = "PROCEDURE";
    public static final String METHOD = "METHOD";
    public static final String HINT_MESSAGE_MIDDLE = "MESSAGE_MIDDLE";
    public static final String HINT_MESSAGE_FINAL = "MESSAGE_FINAL";
    public static final String HINT_BLOCKNAME_MIDDLE = "BLOCKNAME_MIDDLE";
    public static final String HINT_BLOCKNAME_FINAL = "BLOCKNAME_FINAL";
    public static final String CONDITION = "CONDITION";
    public static final String ACTOR = "ACTOR";
    public static final String CHOICES = "CHOICES";
    public static final String ANSWER = "ANSWER";

    protected final String hintKey;

    /**
     * Pre-translated hint text. Using the {@link #hintKey} is preferred.
     */
    protected final String hintText;

    protected final Map<String, HintPlaceholder> parameters = new HashMap<>();

    private Hint(String hintKey, String hintText) {
        this.hintKey = hintKey;
        this.hintText = hintText;
    }

    public static Hint fromKey(final String hintKey) {
        return new Hint(hintKey, null);
    }

    public static Hint fromText(final String hintText) {
        return new Hint(null, hintText);
    }

    public void setParameter(final String key, final String value) {
        parameters.put(key, new HintPlaceholder.Value(value));
    }

    public void setParameter(final String key, final HintPlaceholder value) {
        parameters.put(key, value);
    }

    public String getHintText(final IssueTranslator translator) {
        if (hintText != null) {
            return hintText;
        }

        String hintText = translator.getHint(hintKey);

        for (final var entry : parameters.entrySet()) {
            final String key = entry.getKey();
            final HintPlaceholder placeholder = entry.getValue();
            final String value = translatePlaceholder(translator, placeholder);

            hintText = hintText.replace(ESCAPE_CHARACTER + key, value);
        }

        return hintText;
    }

    private String translatePlaceholder(final IssueTranslator translator, final HintPlaceholder placeholder) {
        return switch (placeholder) {
            case HintPlaceholder.Value v -> v.value();
            case HintPlaceholder.Translatable key -> translator.getInfo(key.translationKey());
            case HintPlaceholder.Defineable d -> getDefineableName(translator, d.defineable());
            case HintPlaceholder.ValueWithTranslatableElements v -> {
                final Matcher matcher = PLACEHOLDER_PATTERN.matcher(v.value());
                yield matcher.replaceAll(matchResult -> {
                    final String translationKey = matchResult.group().replace("{{", "").replace("}}", "");
                    return translator.getInfo(translationKey);
                });
            }
        };
    }

    private String getDefineableName(final IssueTranslator translator, final Defineable def) {
        final StringBuilder builder = new StringBuilder();

        if (def instanceof Variable variable) {
            builder.append("[var]");
            builder.append(translator.getInfo(IssueTranslator.GeneralTerm.VARIABLE));
            builder.append(" \"");
            if (variable.getIdentifier() instanceof LocalIdentifier localIdentifier) {
                builder.append(localIdentifier.getName());
            } else {
                builder.append(((Qualified) variable.getIdentifier()).getSecond().getName().getName());
            }
            builder.append("\"");
            builder.append("[/var]");
        } else if (def instanceof ListVariable variable) {
            builder.append("[list]");
            builder.append(translator.getInfo(IssueTranslator.GeneralTerm.LIST));
            builder.append(" \"");
            if (variable.getIdentifier() instanceof LocalIdentifier localIdentifier) {
                builder.append(localIdentifier.getName());
            } else {
                builder.append(((Qualified) variable.getIdentifier()).getSecond().getName().getName());
            }
            builder.append("\"");
            builder.append("[/list]");
        } else if (def instanceof Attribute attr) {
            builder.append(translator.getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE));
            builder.append(" \"");
            IssueTranslator.GeneralTerm attributeType = getTermFromAttributeType(attr.getAttributeType());
            builder.append(translator.getInfo(attributeType));
            builder.append("\"");
        } else if (def instanceof RobotAttribute attr) {
            builder.append(translator.getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE));
            builder.append(" \"");
            IssueTranslator.GeneralTerm attributeType = getTermFromAttributeType(attr.getAttributeType());
            builder.append(translator.getInfo(attributeType));
            builder.append("\"");
        }

        return builder.toString();
    }

    private IssueTranslator.GeneralTerm getTermFromAttributeType(final Attribute.AttributeType attributeType) {
        return switch (attributeType) {
            case POSITION -> IssueTranslator.GeneralTerm.POSITION;
            case ROTATION -> IssueTranslator.GeneralTerm.ROTATION;
            case SIZE -> IssueTranslator.GeneralTerm.SIZE;
            case VISIBILITY -> IssueTranslator.GeneralTerm.VISIBILITY;
            case GRAPHIC_EFFECT -> IssueTranslator.GeneralTerm.GRAPHIC_EFFECT;
            case SOUND_EFFECT -> IssueTranslator.GeneralTerm.SOUND_EFFECT;
            case VOLUME -> IssueTranslator.GeneralTerm.VOLUME;
            case LAYER -> IssueTranslator.GeneralTerm.LAYER;
            case COSTUME -> IssueTranslator.GeneralTerm.COSTUME;
            case BACKDROP -> IssueTranslator.GeneralTerm.BACKDROP;
            case TIMER -> IssueTranslator.GeneralTerm.TIMER;
        };
    }

    private IssueTranslator.GeneralTerm getTermFromAttributeType(final RobotAttribute.AttributeType attributeType) {
        return switch (attributeType) {
            case LED -> IssueTranslator.GeneralTerm.LED;
            case ROCKY_LIGHT -> IssueTranslator.GeneralTerm.ROCKY_LIGHT;
            case MOTOR_POWER -> IssueTranslator.GeneralTerm.MOTOR_POWER;
            case MATRIX -> IssueTranslator.GeneralTerm.MATRIX;
        };
    }
}
