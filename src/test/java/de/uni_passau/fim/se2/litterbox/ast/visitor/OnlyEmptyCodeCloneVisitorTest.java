/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OnlyEmptyCodeCloneVisitorTest implements JsonTest {

    @Test
    public void testForever() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/visitors/foreverWithSay.json");
        OnlyEmptyCodeCloneVisitor vis = new OnlyEmptyCodeCloneVisitor();
        Script script = vis.apply(program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0));
        Assertions.assertTrue(script.getStmtList().getStmts().get(0) instanceof RepeatForeverStmt);
        RepeatForeverStmt forever = (RepeatForeverStmt) script.getStmtList().getStmts().get(0);
        Assertions.assertTrue(forever.getStmtList().getStmts().isEmpty());
    }
}
