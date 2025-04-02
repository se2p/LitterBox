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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import com.google.common.truth.Truth;
import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class CallWithoutDefinitionTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new CallWithoutDefinition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testCallWithoutDef() throws IOException, ParsingException {
        assertThatFinderReports(1, new CallWithoutDefinition(), "./src/test/fixtures/bugpattern/callWithoutDefinition.json");
    }

    @Test
    public void testSportPong() throws IOException, ParsingException {
        assertThatFinderReports(0, new CallWithoutDefinition(), "./src/test/fixtures/bugpattern/sportpong.json");
    }

    @Test
    public void testWriteTheDraw() throws IOException, ParsingException {
        assertThatFinderReports(0, new CallWithoutDefinition(), "./src/test/fixtures/bugpattern/writeTheDraw.json");
    }

    @Test
    public void testHomeVideo() throws IOException, ParsingException {
        assertThatFinderReports(0, new CallWithoutDefinition(), "./src/test/fixtures/bugpattern/scratchHomeVideo.json");
    }

    @Test
    public void testDerpyAnimal() throws IOException, ParsingException {
        assertThatFinderReports(0, new CallWithoutDefinition(), "./src/test/fixtures/bugpattern/derpyAnimal.json");
    }

    @Test
    public void testHintText() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/callWithoutDefinitionMessage.json");
        CallWithoutDefinition finder = new CallWithoutDefinition();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        Hint hint = Hint.fromKey(finder.getName());
        hint.setParameter(Hint.BLOCK_NAME,"block name ()");
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHintText()).isEqualTo(hint.getHintText());
        }
    }

    @Test
    public void testHintTextTwoParams() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugpattern/callWithoutDefinitionBoth.json");
        CallWithoutDefinition finder = new CallWithoutDefinition();
        Set<Issue> reports = finder.check(program);
        Truth.assertThat(reports).hasSize(1);
        Hint hint = Hint.fromKey(finder.getName());
        hint.setParameter(Hint.BLOCK_NAME,"block name () <>");
        for (Issue issue : reports) {
            Truth.assertThat(issue.getHintText()).isEqualTo(hint.getHintText());
        }
    }
}
