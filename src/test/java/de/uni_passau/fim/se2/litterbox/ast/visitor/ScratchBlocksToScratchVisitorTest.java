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

import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarLexer;
import de.uni_passau.fim.se2.litterbox.ScratchBlocksGrammarParser;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScratchBlocksToScratchVisitorTest {

    private Script getScript(String scratchBlocksInput) {
        ScratchBlocksGrammarLexer lexer = new ScratchBlocksGrammarLexer(CharStreams.fromString(scratchBlocksInput));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScratchBlocksGrammarParser parser = new ScratchBlocksGrammarParser(tokens);
        ParseTree tree = parser.actor();

        ScratchBlocksToScratchVisitor vis = new ScratchBlocksToScratchVisitor();
        ASTNode node = vis.visit(tree);
        return ((Script) node);
    }

    private StmtList getStmtList(String scratchBlocksInput) {
        return getScript(scratchBlocksInput).getStmtList();
    }

    @Test
    public void testSayWithLiteral() {
        StmtList statement = getStmtList("say [ja!]\n");
        Assertions.assertInstanceOf(Say.class, statement.getStatement(0));
        StringExpr expr = ((Say) statement.getStatement(0)).getString();
        Assertions.assertInstanceOf(StringLiteral.class, expr);
        Assertions.assertEquals("ja!", ((StringLiteral) expr).getText());
    }

    @Test
    public void testExprOrLiteralNum() {
        StmtList statements = getStmtList("move (10) steps\n");
        Assertions.assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        double value = ((NumberLiteral) moveSteps.getSteps()).getValue();
        Assertions.assertEquals(10, value);
    }

    @Test
    public void testExprOrLiteralString() {
        StmtList statements = getStmtList("move [a] steps\n");
        Assertions.assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String literal = ((StringLiteral) ((AsNumber) moveSteps.getSteps()).getOperand1()).getText();
        Assertions.assertEquals("a", literal);
    }

    @Test
    public void testExprOrLiteralExpression() {
        StmtList statements = getStmtList("move (\\)\\(\\_\\$\\\\\\\\\\\\a) steps\n");
        Assertions.assertInstanceOf(MoveSteps.class, statements.getStatement(0));
        MoveSteps moveSteps = (MoveSteps) statements.getStatement(0);
        String variableName = ((Variable) ((AsNumber) moveSteps.getSteps()).getOperand1()).getName().getName();
        Assertions.assertEquals(")(_$\\\\\\a", variableName);
    }

    @Test
    public void testPositionFixedRandom() {
        StmtList statements = getStmtList("go to (random position v)\n");
        Assertions.assertInstanceOf(GoToPos.class, statements.getStatement(0));
        GoToPos goToPos = (GoToPos) statements.getStatement(0);
        Assertions.assertInstanceOf(RandomPos.class, goToPos.getPosition());
    }

    @Test
    public void testTouchingColor() {
        StmtList stmtList = getStmtList("<touching color (#ff00ff)?>\n");
        Assertions.assertInstanceOf(ExpressionStmt.class, stmtList.getStatement(0));
        Assertions.assertInstanceOf(SpriteTouchingColor.class, ((ExpressionStmt) stmtList.getStatement(0)).getExpression());
    }
}
