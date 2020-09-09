/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PenWithParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;

public abstract class TopBlockFinder extends AbstractIssueFinder {

    boolean setHint = false;

    @Override
    public void visit(PenDownStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PenUpStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PenClearStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (setHint) {
            addIssue(node, ((CloneOfMetadata) node.getMetadata()).getCloneBlockMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(CallStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DeleteClone node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopAll node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(NextBackdrop node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(KeyPressed node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetXTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetYTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GoToPos node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GoToPosXY node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PenStampStmt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        if (setHint) {
            addIssue(node, ((PenWithParamMetadata) node.getMetadata()).getPenBlockMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        if (setHint) {
            addIssue(node, ((PenWithParamMetadata) node.getMetadata()).getPenBlockMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GreenFlag node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(AttributeAboveValue node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(AskAndWait node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ClearSoundEffects node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StartSound node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopAllSounds node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ResetTimer node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetVariableTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(AddTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DeleteAllOf node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeLayerBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeSizeBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GoToLayer node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Hide node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(HideVariable node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(HideList node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ShowList node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Say node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SayForSecs node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetSizeTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Show node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ShowVariable node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(NextCostume node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Think node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ThinkForSecs node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PointInDirection node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PointTowards node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(TurnLeft node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopThisScript node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StageClicked node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetPenSizeTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetVolumeTo node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetRotationStyle node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetDragMode node) {
        if (setHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ASTNode node) {
        if (setHint) {
            addIssueWithLooseComment();
        } else {
            visitChildren(node);
        }
    }
}
