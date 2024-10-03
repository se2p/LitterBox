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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class IssueTranslator {

    private static final Logger log = Logger.getLogger(IssueTranslator.class.getName());

    private static IssueTranslator instance;
    private ResourceBundle hints;
    private ResourceBundle names;
    private ResourceBundle general;
    private Locale locale;

    public enum GeneralTerm {
        SIZE("size"),
        POSITION("position"),
        COSTUME("costume"),
        ROTATION("rotation"),
        VISIBILITY("visibility"),
        VARIABLE("variable"),
        LIST("list"),
        ATTRIBUTE("attribute"),
        LAYER("layer"),
        VOLUME("volume"),
        SOUND_EFFECT("sound_effect"),
        GRAPHIC_EFFECT("graphic_effect"),
        BACKDROP("backdrop"),
        MATRIX("matrix"),
        LED("led"),
        ROCKY_LIGHT("rocky_light"),
        MOTOR_POWER("motor_power"),

        TIMER("timer");

        private final String key;

        GeneralTerm(String label) {
            this.key = label;
        }

        public String getKey() {
            return key;
        }
    }

    /**
     * Private constructor to avoid instantiation of singleton.
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
        final Locale newLocale;
        if (lang == null) {
            newLocale = Locale.ENGLISH;
        } else {
            newLocale = Locale.forLanguageTag(lang);
        }

        if (!locale.equals(newLocale)) {
            locale = newLocale;
            loadResourceBundles();
        }
    }

    private void loadResourceBundles() {
        names = loadResourceBundle("IssueNames", locale);
        hints = loadResourceBundle("IssueHints", locale);
        general = loadResourceBundle("GeneralTerms", locale);
    }

    private ResourceBundle loadResourceBundle(final String name, final Locale locale) {
        try {
            return  ResourceBundle.getBundle(name, locale);
        } catch (MissingResourceException e) {
            log.warning("Could not load resource bundle for language "
                    + locale.toLanguageTag()
                    + ". Defaulting to English.");
            return ResourceBundle.getBundle(name, Locale.ENGLISH);
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

    /**
     * Finds the translation for a finder group.
     *
     * @param finderGroup Some finder group.
     * @return The translated name.
     */
    public String getInfo(final FinderGroup finderGroup) {
        final String keyword = finderGroup.group();
        return getInfo(keyword);
    }

    public String getInfo(GeneralTerm keyword) {
        return getInfo(keyword.getKey());
    }
}
