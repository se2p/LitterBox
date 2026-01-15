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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsToXY;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

import java.util.HashMap;
import java.util.Map;

public class OverwrittenVisualEffect extends AbstractIssueFinder {

    public static final String NAME = "overwritten_visual_effect";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public void visit(StmtList node) {
        // We only check for sequences within a single StmtList.
        // We do not traverse into child blocks here (visitChildren will do that).

        Map<String, Stmt> lastEffectStmt = new HashMap<>();

        for (Stmt stmt : node.getStmts()) {
            boolean isHalting = AstNodeUtil.containsTimedHaltingStatement(stmt);

            if (stmt instanceof SwitchCostumeTo || stmt instanceof NextCostume) {
                checkAndPut(lastEffectStmt, "COSTUME", stmt);
            } else if (stmt instanceof SwitchBackdrop || stmt instanceof NextBackdrop) {
                checkAndPut(lastEffectStmt, "BACKDROP", stmt);
            } else if (stmt instanceof SetSizeTo) {
                checkAndPut(lastEffectStmt, "SIZE", stmt);
            } else if (stmt instanceof Show || stmt instanceof Hide) {
                checkAndPut(lastEffectStmt, "VISIBILITY", stmt);
            } else if (stmt instanceof GoToLayer || stmt instanceof ChangeLayerBy) {
                checkAndPut(lastEffectStmt, "LAYER", stmt);
            } else if (stmt instanceof Say || stmt instanceof Think) {
                checkAndPut(lastEffectStmt, "BUBBLE", stmt);
            } else if (stmt instanceof SetGraphicEffectTo setEffect) {
                String effectName = "EFFECT_" + setEffect.getEffect().getTypeName();
                if (lastEffectStmt.containsKey(effectName)) {
                    addIssue(lastEffectStmt.get(effectName));
                }
                lastEffectStmt.put(effectName, stmt);
            } else if (stmt instanceof ClearGraphicEffects) {
                if (lastEffectStmt.containsKey("CLEAR")) {
                    addIssue(lastEffectStmt.get("CLEAR"));
                }
                for (var entry : lastEffectStmt.entrySet()) {
                    if (entry.getKey().startsWith("EFFECT_")) {
                        addIssue(entry.getValue());
                    }
                }
                // Only clear graphic effects and previous CLEAR, keep other states (visibility, costume, etc.)
                lastEffectStmt.keySet().removeIf(k -> k.startsWith("EFFECT_"));
                lastEffectStmt.remove("CLEAR");

                lastEffectStmt.put("CLEAR", stmt);
            }

            if (isHalting) {
                lastEffectStmt.clear();
            }
        }

        visitChildren(node);
    }

    private void checkAndPut(Map<String, Stmt> map, String key, Stmt stmt) {
        if (map.containsKey(key)) {
            addIssue(map.get(key));
        }
        map.put(key, stmt);
    }

    private boolean containsHalting(Stmt stmt) {
        if (isHalting(stmt)) {
            return true;
        }
        // Recursively check children
        for (ASTNode child : stmt.getChildren()) {
            if (child instanceof Stmt childStmt) {
                if (containsHalting(childStmt)) {
                    return true;
                }
            } else {
                // Some children might not be Stmt but contain Stmts (e.g. StmtList)
                if (containsHaltingNode(child)) {
                    return true;
                }
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
                || stmt instanceof Speak
                || stmt instanceof RepeatTimesStmt
                || stmt instanceof RepeatForeverStmt
                || stmt instanceof UntilStmt
                || stmt instanceof WaitUntil
                || stmt instanceof AskAndWait;
    }
}
