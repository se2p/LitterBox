/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Round;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;

public class HalsteadVisitor implements ScratchVisitor {

    private Multiset<Class<?>> operators = LinkedHashMultiset.create();

    private Multiset<ASTNode> operands  = LinkedHashMultiset.create();

    public int getTotalOperands() {
        return operands.size();
    }

    public int getTotalOperators() {
        return operators.size();
    }

    public int getUniqueOperands() {
        return operands.elementSet().size();
    }

    public int getUniqueOperators() {
        return operators.elementSet().size();
    }

    @Override
    public void visit(Program program) {
        for(ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            visit(actor.getScripts());
            visit(actor.getProcedureDefinitionList());
        }
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        operators.add(procedure.getClass());
        visit(procedure.getParameterDefinitionList());
        visit(procedure.getStmtList());
    }

    //---------------------------------------------------------------
    // Operands
    //---------------------------------------------------------------

    @Override
    public void visit(BoolLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(ColorLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(NumberLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(StringLiteral node) {
        operands.add(node);
    }

    @Override
    public void visit(Identifier node) {
        operands.add(node);
    }

    @Override
    public void visit(Position node) {
        operands.add(node);
    }

    @Override
    public void visit(ElementChoice node) {
        operands.add(node);
    }

    @Override
    public void visit(Key node) {
        operands.add(node);
    }

    @Override
    public void visit(NumFunct node) {
        // Debatable?
        operands.add(node);
    }

    @Override
    public void visit(EventAttribute node) {
        operands.add(node);
    }

    @Override
    public void visit(FixedAttribute node) {
        operands.add(node);
    }

    @Override
    public void visit(TimeComp node) {
        operands.add(node);
    }

    @Override
    public void visit(NameNum node) {
        operands.add(node);
    }

    @Override
    public void visit(Edge node) {
        operands.add(node);
    }

    @Override
    public void visit(MousePointer node) {
        operands.add(node);
    }

    @Override
    public void visit(SpriteTouchable node) {
        operands.add(node);
    }

    //---------------------------------------------------------------
    // Operators
    //---------------------------------------------------------------

    @Override
    public void visit(Parameter node) {
        // The name of the parameter is stored in a string
        // it's sufficient to count that as operand
        visitChildren(node);
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        // An empty slot for a boolean reporter
        operands.add(node);
    }

    @Override
    public void visit(Round node) {
        operators.add(node.getClass());
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        operators.add(node.getClass());
        visitChildren(node);
    }

    @Override
    public void visit(Expression node) {
        if (node instanceof SingularExpression) {
            operands.add(node);
        } else if (node instanceof UnaryExpression) {
            // Round and Not are already handled
            visitChildren(node);
        } else if (node instanceof BinaryExpression || node instanceof BoolExpr || node instanceof ComparableExpr) {
            operators.add(node.getClass());
            visitChildren(node);
        } else if (node instanceof ExpressionList) {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Stmt node) {
        operators.add(node.getClass());
        visitChildren(node);
    }

    @Override
    public void visit(CallStmt node) {
        operators.add(node.getClass());
        visit(node.getExpressions());
    }

    @Override
    public void visit(Event node) {
        if (!(node instanceof Never)) {
            operators.add(node.getClass());
        }
        visitChildren(node);
    }
}
