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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;

public class HideWithoutShow extends AbstractIssueFinder {
    public static final String NAME = "hide_without_show";
    private boolean hasVisibleChange;
    private boolean hasHide;
    private Hide firstHide;
    private boolean hasShow;

    @Override
    public void visit(ActorDefinition node) {
        hasHide = false;
        hasVisibleChange = false;
        hasShow = false;
        firstHide = null;
        super.visit(node);
        if (hasHide && !hasShow) {
            if (hasVisibleChange) {
                addIssue(firstHide, firstHide.getMetadata());
            }
        }
    }

    @Override
    public void visit(Show node) {
        hasShow = true;
    }

    @Override
    public void visit(Hide node) {
        if (!hasHide) {
            firstHide = node;
        }
        hasHide = true;
    }

    @Override
    public void visit(SpriteMotionStmt node) {
        hasVisibleChange = true;
    }

    @Override
    public void visit(SpriteLookStmt node) {
        hasVisibleChange = true;
    }

    @Override
    public void visit(ActorLookStmt node) {
        hasVisibleChange = true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
