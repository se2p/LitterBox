/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for ForeverInsideLoopFix heuristic.
 *
 * <p>The bug is reported when a forever/until/repeat loop is nested inside another loop.
 * The fix is detected when the loop is no longer nested but still in a working script.
 */
public class ForeverInsideLoopFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ForeverNotNested() throws IOException, ParsingException {
        // Fix: Forever loop is at top level (not nested inside another loop)
        String source = """
                when green flag clicked
                forever
                    move (10) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_UntilNotNested() throws IOException, ParsingException {
        // Fix: Repeat until loop is at top level
        String source = """
                when green flag clicked
                repeat until <(x position) > (100)>
                    move (5) steps
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, UntilStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_ForeverNestedInRepeat() throws IOException, ParsingException {
        // No Fix: Forever loop is nested inside a repeat loop
        String source = """
                when green flag clicked
                repeat (10)
                    forever
                        move (10) steps
                    end
                end
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, RepeatForeverStmt.class);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy block was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        ForeverInsideLoopFix heuristic = new ForeverInsideLoopFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
