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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * If an initialized variable is changed repeatedly (in a RepeatForever) after
 * a WaitSeconds block, it is seen as a timer.
 */
public class Timer extends AbstractIssueFinder {

    public static final String NAME = "timer";
    private boolean insideLoop = false;
    private boolean waitSec = false;
    private List<Qualified> setVariables = new ArrayList<>();
    private boolean addComment = false;


    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        setVariables = new ArrayList<>();
        addComment = false;
        program.accept(this);
        addComment = true;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (!addComment) {
            if (node.getIdentifier() instanceof Qualified qualified) {
                setVariables.add(qualified);
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (!setVariables.isEmpty() && addComment) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (!setVariables.isEmpty() && addComment) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (!setVariables.isEmpty() && addComment) {
            insideLoop = true;
            visitChildren(node);
            insideLoop = false;
        }
    }

    @Override
    public void visit(StmtList node) {
        if (addComment && insideLoop && !setVariables.isEmpty()) {
            node.getStmts().forEach(stmt -> {
                if (stmt instanceof WaitSeconds) {
                    waitSec = true;
                }
            });
            node.getStmts().forEach(stmt -> {
                        if (stmt instanceof ChangeVariableBy changeVariableBy) {
                            if (waitSec) {
                                if (changeVariableBy.getIdentifier() instanceof Qualified changedVariable) {
                                    for (Qualified qualified : setVariables) {
                                        if (changedVariable.equals(qualified)) {
                                            addIssue(stmt, stmt.getMetadata(), IssueSeverity.HIGH);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
            );
            waitSec = false;
        } else {
            visitChildren(node);
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
