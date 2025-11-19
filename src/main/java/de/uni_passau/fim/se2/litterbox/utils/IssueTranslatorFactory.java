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

import java.util.*;

public final class IssueTranslatorFactory {

    private static final Map<Locale, IssueTranslator> ISSUE_TRANSLATORS = new HashMap<>();

    private static final Set<Locale> VALID_LOCALES = Set.of(Locale.ENGLISH, Locale.GERMAN, Locale.forLanguageTag("es"));

    private IssueTranslatorFactory() {
        throw new IllegalCallerException("utility class constructor");
    }

    public static IssueTranslator getIssueTranslator() {
        return getIssueTranslator(Locale.getDefault());
    }

    public static IssueTranslator getIssueTranslator(final Locale locale) {
        final IssueTranslator translator = ISSUE_TRANSLATORS.get(locale);
        return Objects.requireNonNullElseGet(translator, () -> createIssueTranslator(locale));
    }

    private static IssueTranslator createIssueTranslator(final Locale locale) {
        final Locale translatorLocale;
        if (VALID_LOCALES.contains(locale)) {
            translatorLocale = locale;
        } else {
            translatorLocale = Locale.ENGLISH;
        }

        final IssueTranslator issueTranslator = new IssueTranslator(translatorLocale);
        ISSUE_TRANSLATORS.put(translatorLocale, issueTranslator);
        return issueTranslator;
    }
}
