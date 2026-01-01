/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OverwrittenVisualEffect;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Fix heuristic for the OverwrittenVisualEffect bug.
 *
 * <p>The bug is reported when visual effects are overwritten without delay.
 * The bug is fixed if the sequence is broken by a halt or the effect is removed.
 */
public class OverwrittenVisualEffectFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new OverwrittenVisualEffect();

    public OverwrittenVisualEffectFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }




    @Override
    public void visit(StmtList node) {
        Map<String, Stmt> lastEffectStmt = new HashMap<>();

        for (Stmt stmt : node.getStmts()) {
            boolean isHalting = containsHalting(stmt);
            String effectKey = getEffectKey(stmt);

            if (stmt instanceof ClearGraphicEffects) {
                if (lastEffectStmt.containsKey("CLEAR")) {
                    reportIfTarget(lastEffectStmt.get("CLEAR"), true);
                }
                for (var entry : lastEffectStmt.entrySet()) {
                    if (entry.getKey().startsWith("EFFECT_")) {
                        reportIfTarget(entry.getValue(), true);
                    }
                }
                lastEffectStmt.clear();
                lastEffectStmt.put("CLEAR", stmt);
                effectKey = null;
            }

            if (effectKey != null) {
                if (lastEffectStmt.containsKey(effectKey)) {
                    reportIfTarget(lastEffectStmt.get(effectKey), true);
                }
                lastEffectStmt.put(effectKey, stmt);
            }

            if (isHalting) {
                for (Stmt s : lastEffectStmt.values()) {
                    reportIfTarget(s, false);
                }
                lastEffectStmt.clear();
            }
        }

        // At end of list, remaining are safe
        for (Stmt s : lastEffectStmt.values()) {
            reportIfTarget(s, false);
        }

        visitChildren(node);
    }

    private String getEffectKey(Stmt stmt) {
        if (stmt instanceof SwitchCostumeTo || stmt instanceof NextCostume) {
            return "COSTUME";
        } else if (stmt instanceof SwitchBackdrop || stmt instanceof NextBackdrop) {
            return "BACKDROP";
        } else if (stmt instanceof SetSizeTo) {
            return "SIZE";
        } else if (stmt instanceof Show || stmt instanceof Hide) {
            return "VISIBILITY";
        } else if (stmt instanceof GoToLayer || stmt instanceof ChangeLayerBy) {
            return "LAYER";
        } else if (stmt instanceof Say || stmt instanceof Think) {
            return "BUBBLE";
        } else if (stmt instanceof SetGraphicEffectTo setEffect) {
            return "EFFECT_" + setEffect.getEffect().getTypeName();
        }
        return null;
    }

    private void reportIfTarget(Stmt stmt, boolean isBug) {
        if (matchesBugLocation(stmt) && !isBug) {
            addFix(stmt, stmt.getMetadata());
        }
    }

    private boolean containsHalting(Stmt stmt) {
        if (isHalting(stmt)) {
            return true;
        }
        for (ASTNode child : stmt.getChildren()) {
            if (child instanceof Stmt childStmt && containsHalting(childStmt)) {
                return true;
            } else if (containsHaltingNode(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsHaltingNode(ASTNode node) {
        if (node instanceof Stmt stmt && isHalting(stmt)) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (containsHaltingNode(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHalting(Stmt stmt) {
        return stmt instanceof WaitSeconds
                || stmt instanceof ThinkForSecs
                || stmt instanceof SayForSecs
                || stmt instanceof GlideSecsToXY
                || stmt instanceof GlideSecsTo
                || stmt instanceof PlaySoundUntilDone
                || stmt instanceof AskAndWait
                || stmt instanceof Speak;
    }
}
