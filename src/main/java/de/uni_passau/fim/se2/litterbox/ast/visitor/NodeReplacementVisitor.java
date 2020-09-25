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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.SpriteMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.DataInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.ReferenceInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.TypeInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

public class NodeReplacementVisitor extends CloneVisitor {

    private ASTNode target;
    private ASTNode replacement;

    public NodeReplacementVisitor(ASTNode target, ASTNode replacement) {
        this.target = target;
        this.replacement = replacement;
    }

    protected boolean isTargetNode(ASTNode node) {
        return node == target;
    }

    @Override
    public ASTNode visit(ActorDefinition node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PenDownStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PenUpStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PenClearStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Equals node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LessThan node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BiggerThan node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ProcedureDefinition node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StrId node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Script node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(CreateCloneOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StartedAsClone node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IfElseStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IfThenStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(WaitUntil node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UntilStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Not node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(And node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Or node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Broadcast node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BroadcastAndWait node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ReceptionOfMessage node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(RepeatForeverStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(CallStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeleteClone node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StopAll node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StmtList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(RepeatTimesStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StringLiteral node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BoolLiteral node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NumberLiteral node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ColorLiteral node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Never node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ParameterDefinitionList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ParameterDefinition node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ExpressionList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SwitchBackdrop node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NextBackdrop node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SwitchBackdropAndWait node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BackdropSwitchTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(KeyPressed node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MoveSteps node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeXBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeYBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetXTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetYTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GoToPos node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GoToPosXY node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Qualified node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetPenColorToColorStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ColorTouchingColor node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Touching node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PenStampStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangePenColorParamBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetPenColorParamTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetAttributeTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ActorDefinitionList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ActorType node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Key node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Message node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Program node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetStmtList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Next node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Prev node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Random node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(WithExpr node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GreenFlag node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AttributeAboveValue node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UnspecifiedExpression node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UnspecifiedId node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StringContains node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IsKeyPressed node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IsMouseDown node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UnspecifiedBoolExpr node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FromNumber node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Add node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AsNumber node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Current node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DaysSince2000 node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DistanceTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Div node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IndexOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LengthOfString node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LengthOfVar node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Loudness node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Minus node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Mod node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MouseX node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MouseY node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Mult node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NumFunct node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NumFunctOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PickRandom node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Round node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Timer node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UnspecifiedNumExpr node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AsString node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AttributeOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ItemOfVariable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Join node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LetterOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UnspecifiedStringExpr node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Username node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MousePos node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FromExpression node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(RandomPos node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ProcedureDefinitionList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ExpressionStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(UnspecifiedStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AskAndWait node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ClearGraphicEffects node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ClearSoundEffects node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PlaySoundUntilDone node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StartSound node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StopAllSounds node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeVariableBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ResetTimer node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetVariableTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StopOtherScriptsInSprite node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(WaitSeconds node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeclarationAttributeAsTypeStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeclarationAttributeOfIdentAsTypeStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeclarationIdentAsTypeStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeclarationStmtList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeclarationBroadcastStmt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AddTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeleteAllOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DeleteOf node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(InsertAt node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ReplaceItem node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeLayerBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeSizeBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GoToLayer node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Hide node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(HideVariable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(HideList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ShowList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Say node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SayForSecs node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetSizeTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Show node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ShowVariable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SwitchCostumeTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NextCostume node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Think node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ThinkForSecs node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GlideSecsTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GlideSecsToXY node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(IfOnEdgeBounce node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PointInDirection node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PointTowards node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(TurnLeft node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(TurnRight node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StopThisScript node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(TimeComp node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Edge node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MousePointer node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SpriteTouchable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BooleanType node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ListType node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NumberType node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StringType node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AsBool node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AsTouchable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ScriptList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SpriteClicked node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StageClicked node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Costume node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Backdrop node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Direction node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PositionX node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PositionY node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Size node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Volume node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Answer node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NameNum node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FixedAttribute node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AttributeFromFixed node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(AttributeFromVariable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(LayerChoice node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetPenSizeTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangePenSizeBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetGraphicEffectTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeGraphicEffectBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(GraphicEffect node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SoundEffect node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetSoundEffectTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeSoundEffectBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetVolumeTo node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ChangeVolumeBy node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DragMode node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(RotationStyle node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetRotationStyle node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SetDragMode node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SpriteTouchingColor node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Variable node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ScratchList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(Parameter node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ListContains node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(EventAttribute node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(VariableMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MetaMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ListMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ExtensionMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(CommentMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PenWithParamMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ProgramMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BroadcastMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ImageMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SoundMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MonitorSliderMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MonitorListMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MonitorParamMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ReferenceInputMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(TypeInputMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DataInputMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(DataBlockMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NonDataBlockMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(TopNonDataBlockMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FieldsMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NoMutationMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(CallMutationMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(PrototypeMutationMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StopMutationMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(StageMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SpriteMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(BroadcastMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(CommentMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(FieldsMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ImageMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(InputMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ListMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MonitorMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(MonitorParamMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(SoundMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(VariableMetadataList node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NoMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(NoBlockMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ProcedureMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(ForwardBackwardChoice node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }

    @Override
    public ASTNode visit(CloneOfMetadata node) {
        if (isTargetNode(node)) {
            return replacement;
        }
        return super.visit(node);
    }
}
