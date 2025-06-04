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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;

class ScriptsTriggeredByStatementTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptsTriggeredByStatement(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testMessagesAndEvents() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/questions/eventsTriggeredByStatements.json");
        Set<Issue> issues = runFinder(prog, new ScriptsTriggeredByStatement(), false);
        assertThat(issues.size()).isEqualTo(2);

        for (Issue issue : issues) {
            String answer;
            if (issue.getHint().getHintText().contains("[solutions][scratchblocks]\nwhen backdrop switches to [backdrop1 v]")) {
                answer = "\\[solutions]\\[scratchblocks](?:(?!\\[scratchblocks]).)*\\[/scratchblocks]\\[/solutions]";
            }
            else {
                answer = "\\[solutions]\\[scratchblocks](?:(?!\\[scratchblocks]).)*\\[/scratchblocks]\\|\\[scratchblocks](?:(?!\\[scratchblocks]).)*\\[/scratchblocks]\\[/solutions]";
            }
            assertThat(issue.getHint().getHintText()).containsMatch(Pattern.compile(answer, Pattern.DOTALL));
        }
    }

    @Test
    public void testBroadcastStmtWithNoEvent() throws IOException, ParsingException {
        assertThatFinderReports(0, new ScriptsTriggeredByStatement(), "./src/test/fixtures/questions/allElements.json");
    }
}
