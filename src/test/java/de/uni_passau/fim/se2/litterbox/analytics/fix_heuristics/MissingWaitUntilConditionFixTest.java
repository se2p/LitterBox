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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for MissingWaitUntilConditionFix.
 * Bug: WaitUntil has unspecified boolean expression.
 * Fix: Condition is now specified.
 */
public class MissingWaitUntilConditionFixTest extends AbstractFixHeuristicTest {

    @Test
    public void testFixFound_ConditionSpecified() throws IOException, ParsingException {
        // WaitUntil with a real condition - should report fix
        String code = """
                when green flag clicked
                wait until <key (space v) pressed?>
                move (10) steps
                """;

        Program program = parseProgram(code);
        String blockId = findBlockId(program, WaitUntil.class);

        MissingWaitUntilConditionFix finder = new MissingWaitUntilConditionFix(blockId);
        assertFixFound(finder, program);
    }
}
