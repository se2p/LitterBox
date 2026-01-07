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
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
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
                lastEffectStmt.clear();
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
}
