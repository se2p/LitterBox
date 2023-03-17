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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.utils.GroupConstants;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public class ResourceBundleTest {

    private static final List<String> DOUBLE_TAGS = Arrays.asList("[sbi]", "[var]", "[list]", "[bc]");

    private static final List<String> SINGLE_TAGS = Arrays.asList("[TRUE]", "[FALSE]", "[LEQ]", "[EQ]", "[GEQ]", "[IF]", "[ELSE]", "[sbVariables]");

    @ParameterizedTest(name = "Testing existence of bug names for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkBugResourceNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        for (String bugFinder : IssueTool.getBugFinderNames()) {
            assertWithMessage("Language " + locale + ", bug finder " + bugFinder + " not found in name resources").that(names.keySet()).contains(bugFinder);
            checkEncodingProblems(names.getString(bugFinder));
        }
    }

    @ParameterizedTest(name = "Testing existence of smell names for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkSmellResourceNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        for (String smellFinder : IssueTool.getSmellFinderNames()) {
            assertWithMessage("Language " + locale + ", smell finder " + smellFinder + " not found in name resources").that(names.keySet()).contains(smellFinder);
            checkEncodingProblems(names.getString(smellFinder));
        }
    }

    @ParameterizedTest(name = "Testing existence of perfume names for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkPerfumeResourceNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        for (String perfumeFinder : IssueTool.getPerfumeFinderNames()) {
            assertWithMessage("Language " + locale + ", perfume finder " + perfumeFinder + " not found in name resources").that(names.keySet()).contains(perfumeFinder);
            checkEncodingProblems(names.getString(perfumeFinder));
        }
    }

    private void checkEncodingProblems(String hint) {
        List<String> invalidChars = Arrays.asList("Ã¶", "ÃŸ", "Ã¤", "Ã¼", "Ã„", "Ã–", "Ãœ");
        for (String invalidChar : invalidChars) {
            assertThat(hint).doesNotContain(invalidChar);
        }
    }

    private void checkValidBrackets(String key, String hint) {
        List<String> matches = Pattern.compile("\\[[^\\]]+\\]")
                .matcher(hint)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
        String currentToken = "";
        for (String match : matches) {
            if (!currentToken.isEmpty()) {
                if (match.startsWith("[/")) {
                    assertWithMessage("Found invalid tag " + match + " when expecting [/sbi] in hint for " + key).that(match.replace("/", "")).isEqualTo(currentToken);
                    currentToken = "";
                } else {
                    assertWithMessage("Found invalid tag " + match + " when expecting [/sbi] in hint for " + key).that(currentToken).isEqualTo("[sbi]");
                }
            } else if (DOUBLE_TAGS.contains(match)) {
                currentToken = match;
            } else {
                assertWithMessage("Found invalid tag " + match + " in hint for " + key).that(SINGLE_TAGS).contains(match);
            }
        }
        assertWithMessage("Unmatched tag " + currentToken + " in hint for " + key).that(currentToken).isEmpty();
    }

    @ParameterizedTest(name = "Testing encoding of general terms for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void testEncodingInGeneralTerms(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("GeneralTerms", Locale.forLanguageTag(locale));

        for (String key : hints.keySet()) {
            String hint = hints.getString(key);
            checkEncodingProblems(hint);
        }
    }

    @ParameterizedTest(name = "Testing existence of bug hints for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkBugResourceHints(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> bugFinders = IssueTool.getFinders(GroupConstants.BUGS);
        for (IssueFinder finder : bugFinders) {
            for (String key : finder.getHintKeys()) {
                assertWithMessage("Language " + locale + ", hint key " + key + " not found in resources").that(hints.keySet()).contains(key);
                checkValidBrackets(key, hints.getString(key));
                checkEncodingProblems(hints.getString(key));
            }
        }
    }

    @ParameterizedTest(name = "Testing existence of smell hints for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkSmellResourceHints(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> smellFinders = IssueTool.getFinders(GroupConstants.SMELLS);
        for (IssueFinder finder : smellFinders) {
            for (String key : finder.getHintKeys()) {
                assertWithMessage("Language " + locale + ", hint key " + key + " not found in resources").that(hints.keySet()).contains(key);
                checkValidBrackets(key, hints.getString(key));
                checkEncodingProblems(hints.getString(key));
            }
        }
    }

    @ParameterizedTest(name = "Testing existence of perfume hints for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkPerfumeResourceHints(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> perfumeFinders = IssueTool.getFinders(GroupConstants.PERFUMES);
        for (IssueFinder finder : perfumeFinders) {
            for (String key : finder.getHintKeys()) {
                assertWithMessage("Language " + locale + ", hint key " + key + " not found in resources").that(hints.keySet()).contains(key);
                checkValidBrackets(key, hints.getString(key));
                checkEncodingProblems(hints.getString(key));
            }
        }
    }

    @ParameterizedTest(name = "Testing existence of general terms for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkGeneralTerms(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("GeneralTerms", Locale.forLanguageTag(locale));

        for (IssueTranslator.GeneralTerm term : IssueTranslator.GeneralTerm.values()) {
            assertWithMessage("Language " + locale + ", general term " + term.getKey() + " not found in resources").that(hints.keySet()).contains(term.getKey());
            checkEncodingProblems(hints.getString(term.getKey()));
        }
    }

    @ParameterizedTest(name = "Testing for spurious issue names for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkSpuriousNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        Collection<String> finders = new HashSet<>(IssueTool.getAllFinderNames());
        finders.addAll(new MetricTool().getMetricNames()); // TODO: Maybe metrics should go in a different resource file?
        finders.addAll(new ExtractionTool().getExtractorNames());
        for (String key : Collections.list(names.getKeys())) {
            assertWithMessage("Language " + locale + ", key " + key + " does not match a finder").that(finders).contains(key);
        }
    }

    @ParameterizedTest(name = "Testing for spurious issue hint keys for language {0}")
    @ValueSource(strings = {"de", "en", "es"})
    public void checkSpuriousHints(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> allFinders = IssueTool.getFinders(GroupConstants.ALL);
        Set<String> hintKeys = allFinders.stream().flatMap(f -> f.getHintKeys().stream()).collect(Collectors.toSet());
        for (String key : Collections.list(names.getKeys())) {
            assertWithMessage("Language " + locale + ", key " + key + " is not used").that(hintKeys).contains(key);
        }
    }

    // TODO: Cannot test this because keys are spread across many classes...
//    @ParameterizedTest(name = "Testing for spurious general term keys for language {0}")
//    @ValueSource(strings = {"de", "en", "es"})
//    public void checkSpuriousGeneralTerms(String locale) {
//        ResourceBundle names = ResourceBundle.getBundle("GeneralTerms", Locale.forLanguageTag(locale));
//        Set<String> hintKeys = Arrays.stream(IssueTranslator.GeneralTerm.values()).map(IssueTranslator.GeneralTerm::getLabel).collect(Collectors.toSet());
//        for (String key : Collections.list(names.getKeys())) {
//            assertWithMessage("Language "+locale+", key "+key +" is not used").that(hintKeys).contains(key);
//        }
//    }
}
