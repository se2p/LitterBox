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

public sealed interface HintPlaceholder {

    record Value(String value) implements HintPlaceholder {}

    /**
     * A placeholder that will be translated using
     * {@link de.uni_passau.fim.se2.litterbox.utils.IssueTranslator#getInfo(String)}.
     *
     * @param translationKey The key for the translation string.
     */
    record Translatable(String translationKey) implements HintPlaceholder {}

    /**
     * A fixed string with translatable elements inside.
     *
     * <p>Translatable elements must be enclosed in double braces and contain the translation key that will be passed
     * into {@link de.uni_passau.fim.se2.litterbox.utils.IssueTranslator#getInfo(String)}. For example, in a value like
     * {@code {{if}} <> {{then}} ??? {{else}} ??} the {@code {{if}}} will be replaced by the result of
     * {@code IssueTranslator::getInfo("if")}, {@code {{then}}} by {@code IssueTranslator::getInfo("then")}, and so on.
     *
     * @param value The string that should be part of the hint.
     */
    record ValueWithTranslatableElements(String value) implements HintPlaceholder {}

    record Defineable(de.uni_passau.fim.se2.litterbox.cfg.Defineable defineable) implements HintPlaceholder {}

}
