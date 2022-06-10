/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeDeletionVisitorTest implements JsonTest {

    @Test
    public void testDeletion() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/visitors/deleteBlock.json");

        DistanceTo distance = (DistanceTo) ((MoveSteps) program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts().get(0)).getSteps();
        // Stmt replacement = new TurnLeft(target.getSteps(), target.getMetadata());

        NodeDeletionVisitor deletionVisitor = new NodeDeletionVisitor(distance);
        Program programCopy = deletionVisitor.apply(program);

        MoveSteps statements2 = (MoveSteps) programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStatement(0);
        assertTrue(statements2.getSteps() instanceof NumberLiteral);

        LessThan lessThan = (LessThan) ((UntilStmt) program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts().get(1)).getBoolExpr();
        deletionVisitor = new NodeDeletionVisitor(lessThan);
        programCopy = deletionVisitor.apply(program);

        UntilStmt until = (UntilStmt) programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStatement(1);
        assertTrue(until.getBoolExpr() instanceof UnspecifiedBoolExpr);

        Equals eq = (Equals) ((WaitUntil) program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStatement(2)).getUntil();
        assertTrue(eq.getOperand1() instanceof Qualified);
        deletionVisitor = new NodeDeletionVisitor(eq.getOperand1());
        programCopy = deletionVisitor.apply(program);

        Equals equals = (Equals) ((WaitUntil) programCopy.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStatement(2)).getUntil();
        assertTrue(equals.getOperand1() instanceof StringLiteral);
    }
}
