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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatementInsertionVisitorTest implements JsonTest {

    @Test
    public void testInsertionScript() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/visitors/insertionBasis.json");

        Script parent = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0);

        MoveSteps move = new MoveSteps(new NumberLiteral(10), new NoBlockMetadata());

        StatementInsertionVisitor visitor = new StatementInsertionVisitor(parent, 0, move);
        Program programCopy = visitor.apply(program);

        StmtList statements1 = programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList();
        assertEquals(parent.getStmtList().getStmts().size() + 1, statements1.getStmts().size());
        assertEquals(move, statements1.getStatement(0));
    }

    @Test
    public void testInsertionForevert() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/visitors/insertionBasis.json");

        RepeatForeverStmt parent = (RepeatForeverStmt) program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScript(0).getStmtList().getStatement(0);

        MoveSteps move = new MoveSteps(new NumberLiteral(10), new NoBlockMetadata());

        StatementInsertionVisitor visitor = new StatementInsertionVisitor(parent, 0, move);
        Program programCopy = visitor.apply(program);

        StmtList statements1 = ((RepeatForeverStmt) programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStatement(0)).getStmtList();
        assertEquals(parent.getStmtList().getStmts().size() + 1, statements1.getStmts().size());
        assertEquals(move, statements1.getStatement(0));
    }
}
