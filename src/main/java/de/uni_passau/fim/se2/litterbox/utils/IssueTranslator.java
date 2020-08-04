package de.uni_passau.fim.se2.litterbox.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class IssueTranslator {

    private static IssueTranslator instance;
    private ResourceBundle hints;
    private ResourceBundle names;
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
     * <p>
     * If not instance exists yet a new one will be created
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
            names = ResourceBundle.getBundle("IssueHints", locale);
        } catch (MissingResourceException e) {
            names = ResourceBundle.getBundle("IssueHints", Locale.ENGLISH);
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
        if (names.containsKey(finderName)) {
            return names.getString(finderName);
        } else {
            return finderName;
        }
    }

    /**
     * Returns a translated version of the name for a given finder.
     *
     * @param finderName for which a translated name should be retrieved from the resource bundle
     * @return translated name
     */
    public String getName(String finderName) {
        if (names.containsKey(finderName)) {
            return names.getString(finderName);
        } else {
            return finderName;
        }
    }
}
