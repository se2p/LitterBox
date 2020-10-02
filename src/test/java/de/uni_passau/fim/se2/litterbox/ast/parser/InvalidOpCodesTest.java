/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class InvalidOpCodesTest implements JsonTest {

    @Test
    public void testInvalidOpCodes() throws ParsingException, IOException {
        // This project only contains invalid opcodes, thus the programs actor should not have any scripts
        Program program = getAST("src/test/fixtures/invalidOPCodes.json");
        ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().get(1);
        for (Script s : sprite.getScripts().getScriptList()) {
            Stmt stmt = s.getStmtList().getStmts().get(0);
            assertTrue("Sprite may not contain any stmts except Unspecifiedstmt", stmt instanceof UnspecifiedStmt);
        }
    }
}
