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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingResource;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects MissingResource bug by replacing valid costume/sound names with invalid ones.
 * <p>
 * The detector checks if costume or sound names referenced in blocks exist in the
 * actor's metadata. This injector replaces valid names with invalid ones to trigger detection.
 */
public class MissingResourceInjector extends AbstractBugInjector {

    private static final String INVALID_COSTUME_NAME = "invalid_costume_xyz";
    private static final String INVALID_SOUND_NAME = "invalid_sound_xyz";

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingResource();
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (node.getCostumeChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId) {

            addMutation(node, this::replaceWithInvalidCostume);
        }
        super.visit(node);
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId) {

            addMutation(node, this::replaceWithInvalidSound);
        }
        super.visit(node);
    }

    @Override
    public void visit(StartSound node) {
        if (node.getElementChoice() instanceof WithExpr withExpr
                && withExpr.getExpression() instanceof StrId) {

            addMutation(node, this::replaceWithInvalidSoundStartSound);
        }
        super.visit(node);
    }

    private void replaceWithInvalidCostume(SwitchCostumeTo clonedNode) {
        // Create a new invalid StrId
        StrId invalidStrId = new StrId(INVALID_COSTUME_NAME);

        // Get original metadata from the WithExpr if present
        WithExpr originalWithExpr = (WithExpr) clonedNode.getCostumeChoice();
        WithExpr newWithExpr = new WithExpr(invalidStrId, originalWithExpr.getMetadata());
        invalidStrId.setParentNode(newWithExpr);

        // Create a new SwitchCostumeTo with the invalid choice
        SwitchCostumeTo newStmt = new SwitchCostumeTo(newWithExpr, clonedNode.getMetadata());
        newWithExpr.setParentNode(newStmt);

        // Replace in parent StmtList
        replaceInParent(clonedNode, newStmt);
    }

    private void replaceWithInvalidSound(PlaySoundUntilDone clonedNode) {
        StrId invalidStrId = new StrId(INVALID_SOUND_NAME);
        WithExpr originalWithExpr = (WithExpr) clonedNode.getElementChoice();
        WithExpr newWithExpr = new WithExpr(invalidStrId, originalWithExpr.getMetadata());
        invalidStrId.setParentNode(newWithExpr);

        PlaySoundUntilDone newStmt = new PlaySoundUntilDone(newWithExpr, clonedNode.getMetadata());
        newWithExpr.setParentNode(newStmt);

        replaceInParent(clonedNode, newStmt);
    }

    private void replaceWithInvalidSoundStartSound(StartSound clonedNode) {
        StrId invalidStrId = new StrId(INVALID_SOUND_NAME);
        WithExpr originalWithExpr = (WithExpr) clonedNode.getElementChoice();
        WithExpr newWithExpr = new WithExpr(invalidStrId, originalWithExpr.getMetadata());
        invalidStrId.setParentNode(newWithExpr);

        StartSound newStmt = new StartSound(newWithExpr, clonedNode.getMetadata());
        newWithExpr.setParentNode(newStmt);

        replaceInParent(clonedNode, newStmt);
    }
}
