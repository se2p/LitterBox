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
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;

import java.util.Arrays;
import java.util.Collection;

/**
 * Detects blocks that are used on the wrong actor type.
 *
 * <p>Some Scratch blocks are designed to work only with sprites (e.g., motion blocks,
 * say/think, show/hide, costume blocks, clone blocks) and will do nothing when placed
 * on the stage. Similarly, some blocks are designed for the stage only (e.g., next backdrop,
 * switch backdrop and wait, when stage clicked, when backdrop switches to) and will do
 * nothing when placed on a sprite.
 *
 * <p>While the Scratch editor normally prevents adding these blocks to the wrong actor type,
 * users can drag scripts between sprites and the stage, resulting in code that will never
 * execute as intended.
 */
public class InvalidActorBlocks extends AbstractIssueFinder implements PenExtensionVisitor {
    public static final String NAME = "invalid_actor_blocks";
    private static final String HINT_SPRITE = "invalid_actor_blocks_sprite";
    private static final String HINT_STAGE = "invalid_actor_blocks_stage";

    // Sprite-only blocks used on stage
    @Override
    public void visit(SpriteMotionStmt node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteLookStmt node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteClicked node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
    }

    @Override
    public void visit(DeleteClone node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
    }

    @Override
    public void visit(Myself node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node.getParentNode(), HINT_SPRITE);
        }
    }

    @Override
    public void visit(PositionX node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PositionY node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Direction node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Costume node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Size node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Touching node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(DistanceTo node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    // Pen blocks - Sprite only
    @Override
    public void visit(PenDownStmt node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenUpStmt node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenStampStmt node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetPenSizeTo node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        if (currentActor.isStage()) {
            addIssueWithHint(node, HINT_SPRITE);
        }
        visitChildren(node);
    }

    // Stage-only blocks used on sprite
    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (currentActor.isSprite()) {
            addIssueWithHint(node, HINT_STAGE);
        }
        visitChildren(node);
    }

    @Override
    public void visit(StageClicked node) {
        if (currentActor.isSprite()) {
            addIssueWithHint(node, HINT_STAGE);
        }
    }

    private void addIssueWithHint(ASTNode node, String hintKey) {
        Hint hint = Hint.fromKey(hintKey);
        addIssue(node, node.getMetadata(), hint);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        return Arrays.asList(HINT_SPRITE, HINT_STAGE);
    }
}