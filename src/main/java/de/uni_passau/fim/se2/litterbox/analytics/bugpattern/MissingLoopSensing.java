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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A script should execute actions when an event occurs. Instead of continuously checking for the event to occur
 * inside a forever or until loop it is only checked once in a conditional construct, making it
 * unlikely that the timing is correct.
 */
public class MissingLoopSensing extends AbstractIssueFinder {
    public static final String NAME = "missing_loop_sensing";
    public static final String VARIABLE_VERSION = "missing_loop_sensing_variable";
    private boolean insideGreenFlagClone = false;
    private boolean insideLoop = false;
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
            insideGreenFlagClone = true;
        }
        inCondition = false;
        super.visit(node);
        insideGreenFlagClone = false;
        afterWaitUntil = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(UntilStmt node) {
        insideLoop = true;
        visitChildren(node);
        insideLoop = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideGreenFlagClone && !insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(Touching node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(Equals node) {
        if (insideGreenFlagClone && !insideLoop && inCondition && !afterWaitUntil) {
            insideEquals = true;
        }
        visitChildren(node);
        if (hasVariable) {
            Hint hint = new Hint(VARIABLE_VERSION);
            addIssue(node, node.getMetadata(), hint);
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
        if (insideGreenFlagClone && !insideLoop) {
            inCondition = true;
            BoolExpr boolExpr = node.getBoolExpr();
            boolExpr.accept(this);
            inCondition = false;
        }
        node.getStmtList().accept(this);
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
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(VARIABLE_VERSION);
        return keys;
    }
}
