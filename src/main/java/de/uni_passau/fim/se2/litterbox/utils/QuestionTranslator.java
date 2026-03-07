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
package de.uni_passau.fim.se2.litterbox.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Translator for question-related strings.
 * Loads translations from QuestionNames and QuestionTexts resource bundles.
 */
public final class QuestionTranslator {

    private static final Logger log = Logger.getLogger(QuestionTranslator.class.getName());

    private final Locale locale;
    private final ResourceBundle names;
    private final ResourceBundle texts;

    /**
     * Creates a QuestionTranslator for the given locale.
     * Package-private to match IssueTranslator pattern.
     *
     * @param locale the locale for translations
     */
    public QuestionTranslator(final Locale locale) {
        this.locale = locale;
        this.names = loadBundle("QuestionNames", locale);
        this.texts = loadBundle("QuestionTexts", locale);
    }

    private ResourceBundle loadBundle(final String bundleName, final Locale locale) {
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
            if (bundle.getLocale().equals(locale)) {
                return bundle;
            } else {
                return ResourceBundle.getBundle(bundleName, Locale.ENGLISH);
            }
        } catch (MissingResourceException e) {
            log.warning("Could not load resource bundle " + bundleName + " for language "
                    + locale.toLanguageTag()
                    + ". Defaulting to English.");
            return ResourceBundle.getBundle(bundleName, Locale.ENGLISH);
        }
    }

    /**
     * Returns the current locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the language name in English.
     *
     * @return the language display name
     */
    public String getLanguage() {
        return locale.getDisplayLanguage(Locale.ENGLISH);
    }

    /**
     * Returns a translated name for a question finder.
     *
     * @param finderName the question finder name
     * @return the translated name
     */
    public String getName(String finderName) {
        try {
            return names.getString(finderName);
        } catch (MissingResourceException e) {
            log.warning("Missing question name translation for: " + finderName);
            return finderName;
        }
    }

    /**
     * Returns a translated question text for a question finder.
     *
     * @param finderName the question finder name
     * @return the translated question text
     */
    public String getText(String finderName) {
        try {
            return texts.getString(finderName);
        } catch (MissingResourceException e) {
            log.warning("Missing question text translation for: " + finderName);
            return finderName;
        }
    }

    /**
     * Checks if a translation exists for the given finder name.
     *
     * @param finderName the finder name to check
     * @return true if both name and text translations exist
     */
    public boolean hasTranslation(String finderName) {
        return names.containsKey(finderName) && texts.containsKey(finderName);
    }
}
