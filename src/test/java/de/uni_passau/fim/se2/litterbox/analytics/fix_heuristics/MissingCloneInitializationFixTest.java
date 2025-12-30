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

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingCloneInitializationFix.
 * Bug: Create clone without "when I start as a clone" handler.
 * Fix: Handler now exists.
 */
public class MissingCloneInitializationFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_HandlerAdded() throws IOException, ParsingException {
        // Clone created and handler exists - fixed
        String code = """
                when green flag clicked
                create clone of (myself v)
                
                when I start as a clone
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf.class);

        MissingCloneInitializationFix finder = new MissingCloneInitializationFix(blockId);
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_HandlerStillMissing() throws IOException, ParsingException {
        // Clone created but handler still missing
        String code = """
                when green flag clicked
                create clone of (myself v)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf.class);

        MissingCloneInitializationFix finder = new MissingCloneInitializationFix(blockId);
        assertNoFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing create clone is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        MissingCloneInitializationFix finder = new MissingCloneInitializationFix(blockId);
        assertNoFixFound(finder, program);
    }
}
