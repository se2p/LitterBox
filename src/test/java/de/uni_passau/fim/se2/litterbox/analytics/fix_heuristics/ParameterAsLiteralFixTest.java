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
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for ParameterAsLiteralFix.
 * Bug: String literal matches a parameter name.
 * Fix: The literal no longer matches any parameter name.
 */
public class ParameterAsLiteralFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_LiteralChanged() throws IOException, ParsingException {
        // Literal "hello" in procedure body doesn't match parameter "x" - fix reported
        String code = """
                define myBlock (x)
                say [hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "hello".equals(lit.getText()));

        ParameterAsLiteralFix finder = new ParameterAsLiteralFix(blockId);
        assertFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_LiteralStillMatches() throws IOException, ParsingException {
        // Literal "x" still matches parameter "x"
        String code = """
                define myBlock (x)
                say [x]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "x".equals(lit.getText()));

        ParameterAsLiteralFix finder = new ParameterAsLiteralFix(blockId);
        assertNoFixFound(finder, program);
    }

    @Test
    public void testNoFixFound_ScriptDeleted() throws IOException, ParsingException {
        // Script containing the bug is gone
        String code = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = "non-existent-id";

        ParameterAsLiteralFix finder = new ParameterAsLiteralFix(blockId);
        assertNoFixFound(finder, program);
    }
}
