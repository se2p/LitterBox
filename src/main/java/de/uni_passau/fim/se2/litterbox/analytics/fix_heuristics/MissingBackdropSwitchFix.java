/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingBackdropSwitch;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Fix heuristic for the MissingBackdropSwitch bug.
 *
 * <p>The bug is reported when BackdropSwitchTo event is used but backdrop is never switched.
 * The bug is fixed if the backdrop is now switched to.
 */
public class MissingBackdropSwitchFix extends AbstractFixHeuristic {

    private boolean nextRandPrev;
    private Set<String> switchedBackdrops;
    private BackdropSwitchTo targetEvent;
    private ActorDefinition foundActor;
    private Script foundScript;

    private final MissingBackdropSwitch relatedBugFinder = new MissingBackdropSwitch();

    public MissingBackdropSwitchFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        switchedBackdrops = new HashSet<>();
        nextRandPrev = false;
        targetEvent = null;
        foundActor = null;
        foundScript = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(SwitchBackdrop node) {
        recordSwitch(node.getElementChoice());
        visitChildren(node);
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        recordSwitch(node.getElementChoice());
        visitChildren(node);
    }

    @Override
    public void visit(NextBackdrop node) {
        nextRandPrev = true;
        visitChildren(node);
    }

    private void recordSwitch(ElementChoice choice) {
        if (choice instanceof Next || choice instanceof Prev || choice instanceof Random) {
            nextRandPrev = true;
        } else if (choice instanceof WithExpr withExpr) {
            if (withExpr.getExpression() instanceof StrId strId) {
                switchedBackdrops.add(strId.getName());
            } else if (withExpr.getExpression() instanceof StringLiteral stringLiteral) {
                switchedBackdrops.add(stringLiteral.getText());
            } else if (withExpr.getExpression() instanceof AsString expr) {
                if (expr.getOperand1() instanceof StrId strId) {
                    switchedBackdrops.add(strId.getName());
                } else if (expr.getOperand1() instanceof StringLiteral stringLiteral) {
                    switchedBackdrops.add(stringLiteral.getText());
                }
            } else if (withExpr.getExpression() instanceof NumExpr) {
                nextRandPrev = true;
            }
        }
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (matchesBugLocation(node)) {
            targetEvent = node;
            foundActor = currentActor;
            foundScript = currentScript;
        }
        visitChildren(node);
    }

    @Override
    public Set<FixResult> check(Program program) {
        switchedBackdrops = new HashSet<>();
        nextRandPrev = false;
        targetEvent = null;
        foundActor = null;
        foundScript = null;

        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        if (targetEvent != null) {
            String backdrop = targetEvent.getBackdrop().getName();
            if (nextRandPrev || switchedBackdrops.contains(backdrop)) {
                this.currentActor = foundActor;
                this.currentScript = foundScript;
                addFix(targetEvent, targetEvent.getMetadata());
            }
        }

        return Collections.unmodifiableSet(fixes);
    }
}
