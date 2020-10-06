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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.utils.GroupConstants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertWithMessage;

public class ResourceBundleTest {

    @ParameterizedTest(name = "Testing existence of bug names for language {0}")
    @ValueSource(strings = {"de", "en"})
    public void checkBugResourceNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        for (String bugFinder : IssueTool.getBugFinderNames()) {
            assertWithMessage("Language "+locale+", bug finder "+bugFinder +" not found in name resources").that(names.keySet()).contains(bugFinder);
        }
    }

    @ParameterizedTest(name = "Testing existence of smell names for language {0}")
    @ValueSource(strings = {"de", "en"})
    public void checkSmellResourceNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        for (String smellFinder : IssueTool.getSmellFinderNames()) {
            assertWithMessage("Language "+locale+", smell finder "+smellFinder +" not found in name resources").that(names.keySet()).contains(smellFinder);
        }
    }

    @ParameterizedTest(name = "Testing existence of bug hints for language {0}")
    @ValueSource(strings = {"de", "en"})
    public void checkBugResourceHints(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> bugFinders = IssueTool.getFinders(GroupConstants.BUGS);
        for (IssueFinder finder : bugFinders) {
            for (String key : finder.getHintKeys()) {
                assertWithMessage("Language "+locale+", hint key "+key +" not found in resources").that(hints.keySet()).contains(key);
            }
        }
    }

    @ParameterizedTest(name = "Testing existence of smell hints for language {0}")
    @ValueSource(strings = {"de", "en"})
    public void checkSmellResourceHints(String locale) {
        ResourceBundle hints = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> smellFinders = IssueTool.getFinders(GroupConstants.SMELLS);
        for (IssueFinder finder : smellFinders) {
            for (String key : finder.getHintKeys()) {
                assertWithMessage("Language "+locale+", hint key "+key +" not found in resources").that(hints.keySet()).contains(key);
            }
        }
    }

    @ParameterizedTest(name = "Testing for spurious issue names for language {0}")
    @ValueSource(strings = {"de", "en"})
    public void checkSpuriousNames(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueNames", Locale.forLanguageTag(locale));
        Collection<String> finders = new HashSet<>(IssueTool.getAllFinderNames());
        finders.addAll(new MetricTool().getMetricNames()); // TODO: Maybe metrics should go in a different resource file?
        for (String key : Collections.list(names.getKeys())) {
            assertWithMessage("Language "+locale+", key "+key +" does not match a finder").that(finders).contains(key);
        }
    }

    @ParameterizedTest(name = "Testing for spurious issue hint keys for language {0}")
    @ValueSource(strings = {"de", "en"})
    public void checkSpuriousHints(String locale) {
        ResourceBundle names = ResourceBundle.getBundle("IssueHints", Locale.forLanguageTag(locale));
        List<IssueFinder> allFinders = IssueTool.getFinders(GroupConstants.ALL);
        Set<String> hintKeys = allFinders.stream().flatMap(f -> f.getHintKeys().stream()).collect(Collectors.toSet());
        for (String key : Collections.list(names.getKeys())) {
            assertWithMessage("Language "+locale+", key "+key +" is not used").that(hintKeys).contains(key);
        }
    }
}
