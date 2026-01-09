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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.OverwrittenVisualEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
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

    private final OverwrittenVisualEffect relatedBugFinder = new OverwrittenVisualEffect();

    public OverwrittenVisualEffectFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    private static final String KEY_CLEAR = "CLEAR";
    private static final String KEY_COSTUME = "COSTUME";
    private static final String KEY_BACKDROP = "BACKDROP";
    private static final String KEY_SIZE = "SIZE";
    private static final String KEY_VISIBILITY = "VISIBILITY";
    private static final String KEY_LAYER = "LAYER";
    private static final String KEY_BUBBLE = "BUBBLE";

    @Override
    public void visit(StmtList node) {
        Map<String, Stmt> lastEffectStmt = new HashMap<>();

        for (Stmt stmt : node.getStmts()) {
            boolean isHalting = containsHalting(stmt);
            String effectKey = getEffectKey(stmt);

            if (stmt instanceof ClearGraphicEffects) {
                if (lastEffectStmt.containsKey(KEY_CLEAR)) {
                    addFixIfTargetNotBuggy(lastEffectStmt.get(KEY_CLEAR), true);
                }
                for (GraphicEffect.GraphicEffectType type : GraphicEffect.GraphicEffectType.values()) {
                    String key = getGraphicEffectKey(type);
                    if (lastEffectStmt.containsKey(key)) {
                        addFixIfTargetNotBuggy(lastEffectStmt.get(key), true);
                    }
                }
                // Only clear graphic effects and previous CLEAR, keep other states (visibility, costume, etc.)
                lastEffectStmt.keySet().removeIf(k -> k.startsWith("EFFECT_"));
                lastEffectStmt.remove(KEY_CLEAR);

                lastEffectStmt.put(KEY_CLEAR, stmt);
                effectKey = null;
            }

            if (effectKey != null) {
                if (lastEffectStmt.containsKey(effectKey)) {
                    addFixIfTargetNotBuggy(lastEffectStmt.get(effectKey), true);
                }
                lastEffectStmt.put(effectKey, stmt);
            }

            if (isHalting) {
                for (Stmt s : lastEffectStmt.values()) {
                    addFixIfTargetNotBuggy(s, false);
                }
                lastEffectStmt.clear();
            }
        }

        // At end of list, remaining are safe
        for (Stmt s : lastEffectStmt.values()) {
            addFixIfTargetNotBuggy(s, false);
        }

        visitChildren(node);
    }

    private String getGraphicEffectKey(GraphicEffect.GraphicEffectType type) {
        return "EFFECT_" + type.name();
    }

    private String getEffectKey(Stmt stmt) {
        if (stmt instanceof SwitchCostumeTo || stmt instanceof NextCostume) {
            return KEY_COSTUME;
        } else if (stmt instanceof SwitchBackdrop || stmt instanceof NextBackdrop) {
            return KEY_BACKDROP;
        } else if (stmt instanceof SetSizeTo) {
            return KEY_SIZE;
        } else if (stmt instanceof Show || stmt instanceof Hide) {
            return KEY_VISIBILITY;
        } else if (stmt instanceof GoToLayer || stmt instanceof ChangeLayerBy) {
            return KEY_LAYER;
        } else if (stmt instanceof Say || stmt instanceof Think) {
            return KEY_BUBBLE;
        } else if (stmt instanceof SetGraphicEffectTo setEffect) {
            return getGraphicEffectKey(setEffect.getEffect().getType());
        }
        return null;
    }

    private void addFixIfTargetNotBuggy(Stmt stmt, boolean isBug) {
        if (matchesBugLocation(stmt) && !isBug) {
            addFix(stmt, stmt.getMetadata());
        }
    }

    /**
     * Recursively checks if the given node or any of its descendants is a halting statement.
     */
    private boolean containsHalting(ASTNode node) {
        if (node instanceof Stmt stmt && AstNodeUtil.isHaltingStatement(stmt)) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (containsHalting(child)) {
                return true;
            }
        }
        return false;
    }
}
