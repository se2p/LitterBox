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

import de.uni_passau.fim.se2.litterbox.analytics.codeperfumes.AskAndAnswerPerfume;
import de.uni_passau.fim.se2.litterbox.analytics.smells.EmptyScript;
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IssueToolTest {

    @Test
    void getFindersThrowOnInvalidName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> IssueTool.getFinders("bugs,123-non-existing-finder")
        );
    }

    @Test
    void getFindersTrimWhitespace() throws IllegalArgumentException {
        final List<IssueFinder> finders = IssueTool.getFinders(
                " ask_and_answer_perfume  \t   ,\rempty_script\n"
        );
        assertThat(finders).hasSize(2);
    }

    @Test
    void getFindersTrimWhitespaceRemoveEmpty() {
        final List<IssueFinder> finders = IssueTool.getFinders(",\t,  \n   ");
        assertThat(finders).isEmpty();
    }

    @Test
    void getFindersMultipleCategories() {
        final List<IssueFinder> smells = IssueTool.getFinders(FinderGroup.SMELLS);
        final List<IssueFinder> bugs = IssueTool.getFinders(FinderGroup.BUGS);
        final List<IssueFinder> combined = IssueTool.getFinders("bugs,smells");

        assertThat(combined).hasSize(smells.size() + bugs.size());
    }

    @Test
    void getFinderCategoryAndSpecific() {
        final List<IssueFinder> bugs = IssueTool.getFinders(FinderGroup.BUGS);
        final List<IssueFinder> combined = IssueTool.getFinders("bugs,ask_and_answer_perfume");

        assertThat(combined).hasSize(bugs.size() + 1);
        assertThat(combined.stream().filter(finder -> AskAndAnswerPerfume.NAME.equals(finder.getName())))
                .hasSize(1);
    }

    @Test
    void getFinderCategoryAndSpecificNoDuplicate() {
        final List<IssueFinder> smells = IssueTool.getFinders(FinderGroup.SMELLS);
        final List<IssueFinder> combined = IssueTool.getFinders("smells,empty_script");

        assertThat(combined).hasSize(smells.size());
    }

    @Test
    void getFindersNoDuplicates() {
        final List<IssueFinder> smells = IssueTool.getFinders(FinderGroup.SMELLS);
        final List<IssueFinder> combined = IssueTool.getFinders("smells,smells");

        assertThat(combined).hasSize(smells.size());
    }

    @Test
    void excludeGroup() {
        final List<IssueFinder> smells = IssueTool.getFinders(FinderGroup.SMELLS);
        final List<IssueFinder> combined = IssueTool.getFinders("bugs,smells,-bugs");

        assertThat(combined.stream().map(IssueFinder::getName))
                .containsExactlyElementsIn(smells.stream().map(IssueFinder::getName).toList());
    }

    @Test
    void excludeGroupSelf() {
        final List<IssueFinder> combined = IssueTool.getFinders("-smells,smells");

        assertThat(combined).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-empty_script,smells",
            "empty_script,-empty_script,smells",
            "smells,-empty_script,empty_script"
    })
    void excludeSpecificFinder(final String commandString) {
        final List<IssueFinder> smells = IssueTool.getFinders("smells");
        final List<IssueFinder> combined = IssueTool.getFinders(commandString);

        assertThat(combined).hasSize(smells.size() - 1);
        assertThat(combined.stream().filter(f -> EmptyScript.NAME.equals(f.getName()))).isEmpty();
    }

}
