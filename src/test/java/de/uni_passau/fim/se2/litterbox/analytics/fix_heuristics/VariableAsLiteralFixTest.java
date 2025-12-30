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
 * Tests for VariableAsLiteralFix.
 * Bug: String literal matches a variable name.
 * Fix: The literal no longer matches any variable name in scope.
 */
public class VariableAsLiteralFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_LiteralChanged() throws IOException, ParsingException {
        // Literal "hello" doesn't match any variable - fix reported
        String code = """
                when green flag clicked
                say [hello]
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, StringLiteral.class,
                lit -> "hello".equals(lit.getText()));

        VariableAsLiteralFix finder = new VariableAsLiteralFix(blockId);
        assertFixFound(finder, program);
    }
}
