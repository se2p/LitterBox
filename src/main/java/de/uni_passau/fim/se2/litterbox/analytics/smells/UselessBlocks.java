/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UselessBlocks extends AbstractIssueFinder implements PenExtensionVisitor {
    public static final String NAME = "useless_blocks";
    public static final String HINT_STAGE = "useless_blocks_stage";
    public static final String HINT_SPRITE = "useless_blocks_sprite";
    private boolean isCurrentStage;
    private boolean isCurrentSprite;

    @Override
    public void visit(ActorDefinition node) {
        isCurrentSprite = node.isSprite();
        isCurrentStage = node.isStage();
        super.visit(node);
    }

    @Override
    public void visit(StageClicked node) {
        if (isCurrentSprite) {
            Hint hint = new Hint(HINT_SPRITE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (isCurrentSprite) {
            Hint hint = new Hint(HINT_SPRITE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteMotionStmt node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PositionX node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PositionY node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Direction node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteLookStmt node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Costume node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(Size node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(StartedAsClone node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(DeleteClone node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (node.getStringExpr() instanceof AsString asString) {
            if (asString.getOperand1() instanceof StrId strId) {
                String name = strId.getName();
                if (isCurrentStage && name.equals("_myself_")) {
                    Hint hint = new Hint(HINT_STAGE);
                    addIssue(node, node.getMetadata(), hint);
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(Touching node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(DistanceTo node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteClicked node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenDownStmt node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenUpStmt node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(PenStampStmt node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SetPenSizeTo node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        if (isCurrentStage) {
            Hint hint = new Hint(HINT_STAGE);
            addIssue(node, node.getMetadata(), hint);
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(HINT_SPRITE);
        keys.add(HINT_STAGE);
        return keys;
    }
}
