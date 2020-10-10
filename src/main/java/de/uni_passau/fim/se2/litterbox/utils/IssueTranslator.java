/*
 * Copyright (C) 2020 LitterBox contributors
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

public class IssueTranslator {

    private static IssueTranslator instance;
    private ResourceBundle hints;
    private ResourceBundle names;
    private ResourceBundle general;
    private Locale locale;

    /**
     * Private constructor to avoid instatiation of singleton.
     */
    private IssueTranslator() {
        locale = Locale.ENGLISH;
        loadResourceBundles();
    }

    /**
     * Returns an instance of this Singleton.
     *
     * <p>If not instance exists yet a new one will be created
     *
     * @return singleton instance of the IssueTranslator
     */
    public static IssueTranslator getInstance() {
        if (instance == null) {
            instance = new IssueTranslator();
        }
        return instance;
    }

    /**
     * Sets the locale for the issue translator and reloads the resource bundles.
     *
     * @param lang that identifies the locale to set
     */
    public void setLanguage(String lang) {
        if (lang == null) {
            locale = Locale.ENGLISH;
        }

        locale = Locale.forLanguageTag(lang);
        loadResourceBundles();
    }

    private void loadResourceBundles() {
        try {
            names = ResourceBundle.getBundle("IssueNames", locale);
        } catch (MissingResourceException e) {
            names = ResourceBundle.getBundle("IssueNames", Locale.ENGLISH);
            System.err.println("Could not load resource bundle for language "
                    + locale.toLanguageTag()
                    + "; Defaulting to english");
        }

        try {
            hints = ResourceBundle.getBundle("IssueHints", locale);
        } catch (MissingResourceException e) {
            hints = ResourceBundle.getBundle("IssueHints", Locale.ENGLISH);
            System.err.println("Could not load resource bundle for language "
                    + locale.toLanguageTag()
                    + "; Defaulting to english");
        }

        try {
            general = ResourceBundle.getBundle("GeneralTerms", locale);
        } catch (MissingResourceException e) {
            general = ResourceBundle.getBundle("GeneralTerms", Locale.ENGLISH);
            System.err.println("Could not load resource bundle for language "
                    + locale.toLanguageTag()
                    + "; Defaulting to english");
        }
    }

    /**
     * Returns a translated version of the hint for a given finder.
     *
     * @param finderName for which a translated hint should be retrieved from the resource bundle
     * @return translated hint for a given finder name
     */
    public String getHint(String finderName) {
        return hints.getString(finderName);
    }

    /**
     * Returns a translated version of the name for a given finder.
     *
     * @param finderName for which a translated name should be retrieved from the resource bundle
     * @return translated name
     */
    public String getName(String finderName) {
        return names.getString(finderName);
    }

    /**
     * Returns a translated version of an info keyword.
     *
     * <p>These translations are used for general information, such as true or false.
     *
     * @param keyword for general information
     * @return translated name
     */
    public String getInfo(String keyword) {
        if (general.containsKey(keyword)) {
            return general.getString(keyword);
        } else {
            return keyword;
        }
    }
}
