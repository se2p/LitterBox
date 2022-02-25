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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InitialisationOfLooks extends AbstractIssueFinder {

    public static final String NAME = "initialisation_of_looks";
    public static final String HINT_STAGE = "initialisation_of_looks_stage";
    public static final String HINT_SPRITE = "initialisation_of_looks_sprite";
    private boolean inGreenFlag = false;
    private boolean inCustomBlock = false;

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
                    if (stmt instanceof SetSizeTo || stmt instanceof SwitchCostumeTo || stmt instanceof Show
                            || stmt instanceof Hide || stmt instanceof ClearGraphicEffects
                            || stmt instanceof SetGraphicEffectTo || stmt instanceof SwitchBackdrop) {
                        stmt.accept(this);
                        break;
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
    public void visit(SwitchCostumeTo node) {
        check(node);
    }

    @Override
    public void visit(SetSizeTo node) {
        check(node);
    }

    @Override
    public void visit(Show node) {
        check(node);
    }

    @Override
    public void visit(Hide node) {
        check(node);
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        check(node);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        check(node);
    }

    @Override
    public void visit(SwitchBackdrop node) {
        check(node);
    }

    private void check(AbstractNode node) {
        if (inGreenFlag || inCustomBlock) {
            Hint hint;
            if (currentActor.isStage() || node instanceof SwitchBackdrop) {
                hint = new Hint(HINT_STAGE);
            } else {
                hint = new Hint(HINT_SPRITE);
            }
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM, hint);
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

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(HINT_STAGE);
        keys.add(HINT_SPRITE);
        return keys;
    }
}
