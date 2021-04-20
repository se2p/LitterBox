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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Collections;
import java.util.Set;

class TokenVisitor implements ScratchVisitor {

    private Multiset<ASTNode> tokens = LinkedHashMultiset.create();

    public Set<ASTNode> getUniqueTokens() {
        return Collections.unmodifiableSet(tokens.elementSet());
    }

    public int getTotalTokenCount() {
        return tokens.size();
    }

    public int getTokenCount(ASTNode token) {
        return tokens.count(token);
    }

    @Override
    public void visit(Program program) {
        for (ActorDefinition actor : program.getActorDefinitionList().getDefinitions()) {
            visit(actor.getScripts());
            visit(actor.getProcedureDefinitionList());
        }
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        tokens.add(procedure);
        visit(procedure.getParameterDefinitionList());
        visit(procedure.getStmtList());
    }

    //---------------------------------------------------------------
    // Operands
    //---------------------------------------------------------------

    @Override
    public void visit(BoolLiteral node) {
        tokens.add(node);
    }

    @Override
    public void visit(ColorLiteral node) {
        tokens.add(node);
    }

    @Override
    public void visit(NumberLiteral node) {
        tokens.add(node);
    }

    @Override
    public void visit(StringLiteral node) {
        tokens.add(node);
    }

    @Override
    public void visit(Identifier node) {
        tokens.add(node);
    }

    @Override
    public void visit(Position node) {
        tokens.add(node);
    }

    @Override
    public void visit(ElementChoice node) {
        tokens.add(node);
    }

    @Override
    public void visit(Key node) {
        tokens.add(node);
    }

    @Override
    public void visit(NumFunct node) {
        // Debatable?
        tokens.add(node);
    }

    @Override
    public void visit(EventAttribute node) {
        tokens.add(node);
    }

    @Override
    public void visit(FixedAttribute node) {
        tokens.add(node);
    }

    @Override
    public void visit(TimeComp node) {
        tokens.add(node);
    }

    @Override
    public void visit(NameNum node) {
        tokens.add(node);
    }

    @Override
    public void visit(Edge node) {
        tokens.add(node);
    }

    @Override
    public void visit(MousePointer node) {
        tokens.add(node);
    }

    @Override
    public void visit(SpriteTouchable node) {
        tokens.add(node);
    }

    //---------------------------------------------------------------
    // Operators
    //---------------------------------------------------------------

    @Override
    public void visit(Parameter node) {
        // The name of the parameter is stored in a string
        // it's sufficient to count that as operand
        // visitChildren(node);
        tokens.add(node);
    }

    @Override
    public void visit(Variable node) {
        tokens.add(node);
    }

    @Override
    public void visit(Answer node) {
        tokens.add(node);
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        // An empty slot for a boolean reporter
        tokens.add(node);
    }

    @Override
    public void visit(Round node) {
        tokens.add(node);
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        tokens.add(node);
        visitChildren(node);
    }

    @Override
    public void visit(Expression node) {
        if (node instanceof SingularExpression) {
            tokens.add(node);
        } else if (node instanceof UnaryExpression) {
            // Round and Not are already handled
            visitChildren(node);
        } else if (node instanceof BinaryExpression || node instanceof BoolExpr || node instanceof ComparableExpr) {
            tokens.add(node);
            visitChildren(node);
        } else if (node instanceof ExpressionList) {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Stmt node) {
        tokens.add(node);
        visitChildren(node);
    }

    @Override
    public void visit(CallStmt node) {
        tokens.add(node);
        visit(node.getExpressions());
    }

    @Override
    public void visit(Event node) {
        if (!(node instanceof Never)) {
            tokens.add(node);
        }
        visitChildren(node);
    }
}
