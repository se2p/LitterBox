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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;

/**
 * This checks for an initialization for the sprite location. This initialization should usually happen in a
 * GreenFlag script or a CustomBlock. As initial position for the sprite we set X = 0, Y = 0.
 */
public class InitialisationOfPosition extends AbstractIssueFinder {
    public static final String NAME = "initialisation_of_position";
    private boolean initializedX = false;
    private boolean initializedY = false;
    private boolean inCustomBlock = false;
    private boolean inGreenFlag = false;

    @Override
    public void visit(ActorDefinition actor) {
        initializedX = false;
        initializedY = false;
        super.visit(actor);
    }

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag) {
            inGreenFlag = true;
            this.currentScript = node;
            this.currentProcedure = null;
            node.getStmtList().accept(this);
            inGreenFlag = false;
            visitChildren(node);
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        inCustomBlock = true;
        this.currentProcedure = node;
        this.currentScript = null;
        node.getStmtList().accept(this);
        inCustomBlock = false;
        visitChildren(node);
    }

    @Override
    public void visit(StmtList node) {
        if (inCustomBlock) {
            if (node.getParentNode() instanceof ProcedureDefinition) {
                for (Stmt stmt : node.getStmts()) {
                    if (stmt instanceof GoToPosXY || stmt instanceof SetXTo || stmt instanceof SetYTo) {
                        stmt.accept(this);
                    }
                }
            }
        } else {

            // Initialization should not be in a control- statement
            node.getStmts().forEach(stmt -> {
                if (!(stmt instanceof ControlStmt)) {
                    stmt.accept(this);
                }
            });
        }
    }

    @Override
    public void visit(SetXTo stmt) {
        if (stmt.getNum() instanceof NumberLiteral) {
            initializedX = true;
            if (initializedY) {
                check(stmt);
                initializedX = false;
                initializedY = false;
            }
        }
    }

    @Override
    public void visit(SetYTo stmt) {
        if (stmt.getNum() instanceof NumberLiteral) {
            initializedY = true;
            if (initializedX) {
                check(stmt);
                initializedX = false;
                initializedY = false;
            }
        }
    }

    @Override
    public void visit(GoToPosXY stmt) {
        if (stmt.getX() instanceof NumberLiteral
                && stmt.getY() instanceof NumberLiteral) {
            check(stmt);
        }
    }

    private void check(AbstractNode node) {
        if (inGreenFlag || inCustomBlock) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
            //} else if (inCustomBlock) {
            //   initializedInBlock = true;
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
