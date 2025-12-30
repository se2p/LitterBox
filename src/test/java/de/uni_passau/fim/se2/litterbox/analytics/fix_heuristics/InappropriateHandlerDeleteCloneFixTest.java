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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for InappropriateHandlerDeleteCloneFix.
 * Bug: DeleteClone in GreenFlag script (runs on main sprite, not clone).
 * Fix: DeleteClone no longer in GreenFlag script.
 */
public class InappropriateHandlerDeleteCloneFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_DeleteCloneInCloneHandler() throws IOException, ParsingException {
        // DeleteClone in "When I start as clone" handler - fixed
        String code = """
                when I start as a clone
                move (10) steps
                delete this clone
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, DeleteClone.class);

        InappropriateHandlerDeleteCloneFix finder = new InappropriateHandlerDeleteCloneFix(blockId);
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFix_DeleteCloneStillInGreenFlag() throws IOException, ParsingException {
        // DeleteClone still in GreenFlag handler - bug persists
        String code = """
                when green flag clicked
                delete this clone
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, DeleteClone.class);

        InappropriateHandlerDeleteCloneFix finder = new InappropriateHandlerDeleteCloneFix(blockId);
        assertNoFixFound(finder, program);
    }
}
