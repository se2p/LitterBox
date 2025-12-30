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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;

/**
 * Fix heuristic for the HideWithoutShow bug.
 *
 * <p>The bug is reported when Hide is used but Show is never called in a visible actor.
 * The bug is fixed if Show now exists OR the actor makes no visible changes.
 */
public class HideWithoutShowFix extends AbstractFixHeuristic {
    public static final String NAME = "hide_without_show_fix";

    private boolean hasVisibleChange;
    private boolean hasShow;
    private Hide targetHide;

    public HideWithoutShowFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    public void visit(ActorDefinition node) {
        hasVisibleChange = false;
        hasShow = false;
        targetHide = null;
        super.visit(node);

        if (targetHide != null && (hasShow || !hasVisibleChange)) {
            addIssue(targetHide, targetHide.getMetadata());
        }
    }

    @Override
    public void visit(Show node) {
        hasShow = true;
        super.visit(node);
    }

    @Override
    public void visit(Hide node) {
        if (matchesBugLocation(node)) {
            targetHide = node;
        }
        super.visit(node);
    }

    @Override
    public void visit(SpriteMotionStmt node) {
        hasVisibleChange = true;
        super.visit(node);
    }

    @Override
    public void visit(SpriteLookStmt node) {
        if (!(node instanceof Hide) && !(node instanceof Show)) {
            hasVisibleChange = true;
        }
        super.visit(node);
    }

    @Override
    public void visit(ActorLookStmt node) {
        hasVisibleChange = true;
        super.visit(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
