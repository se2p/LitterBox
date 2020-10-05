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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.PenWithParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
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
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

/**
 * Checks if the project has loose blocks without a head.
 */
public class DeadCode extends AbstractIssueFinder {

    public static final String NAME = "dead_code";
    private boolean addHint = false;

    @Override
    public void visit(Script node) {
        currentScript = node;
        if (node.getEvent() instanceof Never && node.getStmtList().getStmts().size() > 0) {
            addHint = true;
            node.getStmtList().getStmts().get(0).accept(this);
        }
        addHint = false;
        currentScript = null;
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
    public void visit(PenDownStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PenUpStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PenClearStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Equals node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(LessThan node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BiggerThan node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (addHint) {
            addIssue(node, ((CloneOfMetadata) node.getMetadata()).getCloneBlockMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(WaitUntil node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Not node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(And node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Or node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(CallStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DeleteClone node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopAll node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchBackdrop node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(NextBackdrop node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeXBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetXTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetYTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GoToPos node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GoToPosXY node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ColorTouchingColor node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Touching node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PenStampStmt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        if (addHint) {
            addIssue(node, ((PenWithParamMetadata) node.getMetadata()).getPenBlockMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        if (addHint) {
            addIssue(node, ((PenWithParamMetadata) node.getMetadata()).getPenBlockMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IsMouseDown node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Add node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Current node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DaysSince2000 node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DistanceTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Div node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IndexOf node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(LengthOfString node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(LengthOfVar node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Loudness node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Minus node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Mod node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(MouseX node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(MouseY node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Mult node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(NumFunctOf node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PickRandom node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Round node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Timer node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(AttributeOf node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ItemOfVariable node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Join node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(LetterOf node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Username node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(AskAndWait node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ClearSoundEffects node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StartSound node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopAllSounds node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ResetTimer node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetVariableTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(AddTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DeleteAllOf node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(DeleteOf node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(InsertAt node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeLayerBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeSizeBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GoToLayer node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Hide node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(HideVariable node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(HideList node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ShowList node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Say node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SayForSecs node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetSizeTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Show node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ShowVariable node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(NextCostume node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Think node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ThinkForSecs node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PointInDirection node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PointTowards node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(TurnLeft node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(TurnRight node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(StopThisScript node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ExpressionStmt node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(Costume node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Backdrop node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Direction node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PositionX node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(PositionY node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Size node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Volume node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Answer node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetPenSizeTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetVolumeTo node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetRotationStyle node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SetDragMode node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Variable node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ScratchList node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Parameter node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(ListContains node) {
        if (addHint) {
            addIssue(node, node.getMetadata());
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(Qualified node) {
        node.getSecond().accept(this);
    }

    @Override
    public void visit(ASTNode node) {
        if (addHint) {
            addIssueWithLooseComment();
        } else {
            visitChildren(node);
        }
    }
}
