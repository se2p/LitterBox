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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingInitializationFix heuristic.
 *
 * <p>The bug is reported when a variable is used before it is initialized.
 * The fix is detected when the variable use is now properly initialized before use.
 */
public class MissingInitializationFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_VariableInitialized() throws IOException, ParsingException {
        // Fix: Variable is initialized before it is used
        String source = """
                when green flag clicked
                set [myVar v] to (0)
                change [myVar v] by (1)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ChangeVariableBy.class);

        MissingInitializationFix heuristic = new MissingInitializationFix(bugId);
        assertFixFound(heuristic, program);
    }

    @Test
    public void testNoFix_VariableNotInitialized() throws IOException, ParsingException {
        // No Fix: Variable is used without being initialized first
        String source = """
                when green flag clicked
                change [myVar v] by (1)
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, ChangeVariableBy.class);

        MissingInitializationFix heuristic = new MissingInitializationFix(bugId);
        assertNoFixFound(heuristic, program);
    }

    @Test
    public void testSilent_BlockDeleted() throws IOException, ParsingException {
        // Silent: The buggy block/script was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        MissingInitializationFix heuristic = new MissingInitializationFix("non_existent_id");
        assertNoFixFound(heuristic, program);
    }
}
