/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

/**
 * Tests for InvalidNumberStringFix heuristic.
 *
 * <p>The bug is reported when a string that looks like a number format but is invalid
 * is used in a numeric context (AsNumber). The fix is detected when:
 * - The string is now a valid number
 * - The string no longer looks like a number (completely different content)
 * - The block was deleted
 */
public class InvalidNumberStringFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ValidNumber() throws IOException, ParsingException {
        // Fix: String is a valid number
        String source = """
                when green flag clicked
                move ((123.45) + [0]) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, AsNumber.class);

        InvalidNumberStringFix heuristic = new InvalidNumberStringFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_IntegerNumber() throws IOException, ParsingException {
        // Fix: String is a valid integer
        String source = """
                when green flag clicked
                move ((42) + [0]) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, AsNumber.class);

        InvalidNumberStringFix heuristic = new InvalidNumberStringFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_ScientificNotation() throws IOException, ParsingException {
        // Fix: String is valid scientific notation
        String source = """
                when green flag clicked
                move ((1e5) + [0]) steps
                """;

        Program program = parseProgram(source);
        String bugId = findBlockId(program, AsNumber.class);

        InvalidNumberStringFix heuristic = new InvalidNumberStringFix(createTestIssueDTO(bugId));
        assertFixFound(heuristic, program);
    }

    @Test
    public void testFixFound_BlockDeleted() throws IOException, ParsingException {
        // Fix: The buggy block was deleted
        String source = """
                when green flag clicked
                move (10) steps
                """;

        Program program = parseProgram(source);

        InvalidNumberStringFix heuristic = new InvalidNumberStringFix(createTestIssueDTO("__deleted_block__"));
        Set<FixResult> fixes = heuristic.check(program);
        // When the block is deleted, it's considered fixed
        Assertions.assertFalse(fixes.isEmpty(),
                "Should find fix when block is deleted");
    }
}
