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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingAskFix.
 * Bug: Answer block is used but Ask and Wait is never called.
 * Fix: Program now uses Ask and Wait.
 */
public class MissingAskFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_AskNowUsed() throws IOException, ParsingException {
        // Answer is used and Ask is now present - fixed
        String code = """
                when green flag clicked
                ask [What is your name?] and wait
                say (answer)
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, Answer.class);

        MissingAskFix finder = new MissingAskFix(blockId);
        assertFixFound(finder, program);
    }
}
