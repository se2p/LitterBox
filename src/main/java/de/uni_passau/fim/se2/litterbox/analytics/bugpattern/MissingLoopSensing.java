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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.smells.UnnecessaryIfAfterUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * A script should execute actions when an event occurs. Instead of continuously checking for the event to occur
 * inside a forever or until loop it is only checked once in a conditional construct, making it
 * unlikely that the timing is correct.
 */
public class MissingLoopSensing extends AbstractIssueFinder {
    public static final String NAME = "missing_loop_sensing";
    public static final String VARIABLE_VERSION = "missing_loop_sensing_variable";
    private Stack<LoopStmt> loopStack = new Stack<>();
    private boolean inCondition = false;
    private boolean insideEquals = false;
    private boolean hasVariable = false;
    private boolean afterWaitUntil = false;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            inCondition = false;
            loopStack = new Stack<>();
            super.visit(node);
            afterWaitUntil = false;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not be detected in Procedures
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopStack.push(node);
        visitChildren(node);
        loopStack.pop();
    }

    @Override
    public void visit(UntilStmt node) {
        loopStack.push(node);
        visitChildren(node);
        loopStack.pop();
    }

    @Override
    public void visit(IfThenStmt node) {
        if (loopStack.isEmpty()) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(Touching node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        }
    }

    @Override
    public void visit(Equals node) {
        if (inCondition && !afterWaitUntil) {
            insideEquals = true;
        }
        visitChildren(node);
        if (hasVariable) {
            Hint hint = new Hint(VARIABLE_VERSION);
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH, hint);
            hasVariable = false;
        }
        insideEquals = false;
    }

    @Override
    public void visit(Variable node) {
        if (insideEquals) {
            hasVariable = true;
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (insideEquals) {
            hasVariable = true;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (loopStack.isEmpty()) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
        node.getElseStmts().accept(this);
    }

    @Override
    public void visit(WaitUntil node) {
        afterWaitUntil = true;
        super.visit(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public boolean areCoupled(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.areCoupled(first, other);
        }

        if (other.getFinder() instanceof UnnecessaryIfAfterUntil) {
            return other.getFinder().areCoupled(other, first);
        }
        return false;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.areCoupled(first, other);
        }

        if (!(other.getFinder() instanceof ForeverInsideIf)) {
            return false;
        }
        IfStmt ifStmt = findIf(first.getCodeLocation());
        boolean resultOfThenSection = false;
        if (ifStmt.getThenStmts().getNumberOfStatements() > 0) {
            resultOfThenSection =
                    ifStmt.getThenStmts().getStatement(ifStmt.getThenStmts().getNumberOfStatements() - 1) == other.getCodeLocation();
        }
        if (ifStmt instanceof IfThenStmt) {
            return resultOfThenSection;
        } else {
            IfElseStmt ifElseStmt = (IfElseStmt) ifStmt;
            boolean resultOfElseSection = false;
            if (ifElseStmt.getElseStmts().getNumberOfStatements() > 0) {
                resultOfElseSection = ifElseStmt.getElseStmts().getStatement(ifElseStmt.getElseStmts().getNumberOfStatements() - 1) == other.getCodeLocation();
            }
            return resultOfThenSection || resultOfElseSection;
        }
    }

    private IfStmt findIf(ASTNode codeLocation) {
        ASTNode parent = codeLocation.getParentNode();
        while (!(parent instanceof IfStmt)) {
            if (parent != null) {
                parent = parent.getParentNode();
            } else {
                throw new IllegalStateException("It can not happen that MissingLoopSensing can be found without an IfStmt. Something went wrong.");
            }
        }
        return (IfStmt) parent;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(VARIABLE_VERSION);
        return keys;
    }
}
