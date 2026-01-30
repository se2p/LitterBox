/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for translatable text with parameterized placeholders.
 *
 * <p>This class provides the common infrastructure for text that:
 * <ul>
 *   <li>Can be identified by a key (for lookup from properties files)</li>
 *   <li>Can contain inline text (pre-translated or literal)</li>
 *   <li>Can have parameters substituted using placeholder syntax</li>
 * </ul>
 *
 * <p>Subclasses like {@link Hint} and
 * {@link de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionText}
 * specify which properties files to use for translation.
 *
 * @see Hint
 * @see HintPlaceholder
 */
public abstract class TranslatableText {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)}}");

    /**
     * Escape character used to mark placeholders in text templates.
     * For example, %VARIABLE will be replaced with the value of the VARIABLE parameter.
     */
    public static final String ESCAPE_CHARACTER = "%";

    /**
     * The key for looking up the text in a properties file.
     * May be null if pre-translated text is provided directly.
     */
    protected final String textKey;

    /**
     * Pre-translated or literal text content.
     * Using the {@link #textKey} is preferred for i18n support.
     */
    protected final String textContent;

    /**
     * Parameters to substitute into the text template.
     * Keys are placeholder names (without the % prefix), values are the replacements.
     */
    protected final Map<String, HintPlaceholder> parameters = new HashMap<>();

    /**
     * Constructs a TranslatableText with the given key and content.
     *
     * @param textKey     the key for properties file lookup (may be null)
     * @param textContent the pre-translated text content (may be null)
     */
    protected TranslatableText(String textKey, String textContent) {
        this.textKey = textKey;
        this.textContent = textContent;
    }

    /**
     * Returns the key for this text.
     *
     * @return the translation key, or null if using direct text content
     */
    public String getKey() {
        return textKey;
    }

    /**
     * Returns the direct text content, if set.
     *
     * @return the text content, or null if using key-based lookup
     */
    public String getTextContent() {
        return textContent;
    }

    /**
     * Returns the parameters map for direct access.
     *
     * @return unmodifiable view of the parameters map
     */
    public Map<String, HintPlaceholder> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Sets a simple string parameter value.
     *
     * @param key   the parameter name (without % prefix)
     * @param value the string value to substitute
     */
    public void setParameter(final String key, final String value) {
        requireNonNull(key, "Parameter key must not be null");
        requireNonNull(value, "Parameter value must not be null");
        parameters.put(key, new HintPlaceholder.Value(value));
    }

    /**
     * Sets a parameter with a rich placeholder value.
     *
     * @param key   the parameter name (without % prefix)
     * @param value the placeholder value to substitute
     */
    public void setParameter(final String key, final HintPlaceholder value) {
        requireNonNull(key, "Parameter key must not be null");
        requireNonNull(value, "Parameter value must not be null");
        parameters.put(key, value);
    }

    /**
     * Sets a translatable parameter value.
     *
     * @param key   the parameter name (without % prefix)
     * @param value the translatable value to substitute
     */
    public void setParameter(final String key, final TranslatableText value) {
        requireNonNull(key, "Parameter key must not be null");
        requireNonNull(value, "Parameter value must not be null");
        parameters.put(key, new HintPlaceholder.Translatable(value.getKey()));
    }

    /**
     * Resolves the text by looking up the key and substituting parameters.
     *
     * @param translator the translator for looking up keys and translating placeholders
     * @param keyLookup  function to look up the key from the appropriate properties file
     * @return the resolved, translated text with parameters substituted
     */
    public String resolveText(final IssueTranslator translator,
                              Function<String, String> keyLookup) {
        if (textContent != null) {
            return textContent;
        }

        String text = keyLookup.apply(textKey);

        for (final var entry : parameters.entrySet()) {
            final String key = entry.getKey();
            final HintPlaceholder placeholder = entry.getValue();
            final String value = translatePlaceholder(translator, placeholder);

            text = text.replace(ESCAPE_CHARACTER + key, value);
        }

        return text;
    }

    /**
     * Translates a placeholder value to its string representation.
     *
     * @param translator  the translator for i18n lookups
     * @param placeholder the placeholder to translate
     * @return the translated string value
     */
    protected String translatePlaceholder(final IssueTranslator translator,
                                          final HintPlaceholder placeholder) {
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

    /**
     * Converts a CFG Defineable to a displayable name.
     * Provides a default implementation that handles common types.
     *
     * @param translator the translator for i18n lookups
     * @param def        the defineable to convert
     * @return the formatted name
     */
    protected String getDefineableName(final IssueTranslator translator,
                                       final Defineable def) {
        if (def instanceof Variable var) {
            return formatDefineableName(translator, IssueTranslator.GeneralTerm.VARIABLE,
                    var.getIdentifier(), "var");
        } else if (def instanceof ListVariable list) {
            return formatDefineableName(translator, IssueTranslator.GeneralTerm.LIST,
                    list.getIdentifier(), "list");
        } else if (def instanceof Parameter param) {
            return getIdentifierName(param.getIdentifier());
        } else if (def instanceof Attribute attr) {
            String attrName = translator.getInfo(getTermFromAttributeType(attr.getAttributeType()));
            String attrTerm = translator.getInfo(IssueTranslator.GeneralTerm.ATTRIBUTE);
            if (attr.getParameter() != null) {
                return attrTerm + " \"" + attrName + " (" + attr.getParameter() + ")\"";
            }
            return attrTerm + " \"" + attrName + "\"";
        } else if (def instanceof RobotAttribute attr) {
            return translator.getInfo(getTermFromAttributeType(attr.getAttributeType()));
        }

        return def.toString();
    }

    private String formatDefineableName(IssueTranslator translator, IssueTranslator.GeneralTerm term,
                                        Identifier ident,
                                        String tag) {
        String name = getIdentifierName(ident);
        if (useFormattingTags()) {
            return String.format("[%s]%s \"%s\"[/%s]", tag, translator.getInfo(term), name, tag);
        }
        return name;
    }

    private String getIdentifierName(Identifier identifier) {
        if (identifier instanceof LocalIdentifier local) {
            return local.getName();
        } else if (identifier instanceof Qualified qualified) {
            return qualified.getFirst().getName() + "." + qualified.getSecond().getName().getName();
        }
        return identifier != null ? identifier.toString() : "unknown";
    }

    /**
     * Whether to use formatting tags (like [var]) in getDefineableName.
     * Defaults to false, subclasses like Hint may override.
     *
     * @return true to use tags
     */
    protected boolean useFormattingTags() {
        return false;
    }

    private IssueTranslator.GeneralTerm getTermFromAttributeType(
            final Attribute.AttributeType attributeType) {
        return switch (attributeType) {
            case POSITION_X, POSITION_Y -> IssueTranslator.GeneralTerm.POSITION;
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
            case ROTATION_STYLE -> IssueTranslator.GeneralTerm.ROTATION_STYLE;
            case SOUND_STATE -> IssueTranslator.GeneralTerm.SOUND_STATE;
            case DRAG_MODE -> IssueTranslator.GeneralTerm.DRAG_MODE;
            case VIDEO_STATE -> IssueTranslator.GeneralTerm.VIDEO_STATE;
            case VIDEO_TRANSPARENCY -> IssueTranslator.GeneralTerm.TRANSPARENCY;
        };
    }

    private IssueTranslator.GeneralTerm getTermFromAttributeType(
            final RobotAttribute.AttributeType attributeType) {
        return switch (attributeType) {
            case LED -> IssueTranslator.GeneralTerm.LED;
            case ROCKY_LIGHT -> IssueTranslator.GeneralTerm.ROCKY_LIGHT;
            case MOTOR_POWER -> IssueTranslator.GeneralTerm.MOTOR_POWER;
            case MATRIX -> IssueTranslator.GeneralTerm.MATRIX;
        };
    }
}
