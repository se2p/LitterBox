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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointlessTimerResetTest implements JsonTest {

    @Test
    void testIssueFound() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                reset timer
                """;
        Program program = parseScratchBlocks(code);
        PointlessTimerReset finder = new PointlessTimerReset();
        Set<Issue> issues = finder.check(program);
        assertEquals(1, issues.size());
    }

    @Test
    void testNoIssueWhenTimerUsed() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                reset timer
                say (timer)
                """;
        Program program = parseScratchBlocks(code);
        PointlessTimerReset finder = new PointlessTimerReset();
        Set<Issue> issues = finder.check(program);
        assertEquals(0, issues.size());
    }

    @Test
    void testNoIssueWhenNoReset() throws IOException, ParsingException {
        String code = """
                when green flag clicked
                say (timer)
                """;
        Program program = parseScratchBlocks(code);
        PointlessTimerReset finder = new PointlessTimerReset();
        Set<Issue> issues = finder.check(program);
        assertEquals(0, issues.size());
    }
}
