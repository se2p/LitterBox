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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration;

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.ExtensionBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.MBlockNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.MusicBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.TextToSpeechBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.ActorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astlists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorListMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorParamMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.monitor.MonitorSliderMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ResourceMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.SoundMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.type.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

class PathRepresentationVisitor implements ScratchVisitor {
    private int representation = -1;

    public int getRepresentation() {
        return representation;
    }

    @Override
    public void visit(ASTNode node) {
        // if this happens: override the missing `visit` methods
        throw new UnsupportedOperationException(
                "Cannot generate a path representation for node of type: " + node.getUniqueName()
        );
    }

    @Override
    public void visit(ActorDefinition node) {
        representation = 1;
    }

    @Override
    public void visit(SetStmt node) {
        representation = 2;
    }

    @Override
    public void visit(Equals node) {
        representation = 3;
    }

    @Override
    public void visit(LessThan node) {
        representation = 4;
    }

    @Override
    public void visit(BiggerThan node) {
        representation = 5;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        representation = 6;
    }

    @Override
    public void visit(StrId node) {
        representation = 7;
    }

    @Override
    public void visit(Script node) {
        representation = 8;
    }

    @Override
    public void visit(CreateCloneOf node) {
        representation = 9;
    }

    @Override
    public void visit(StartedAsClone node) {
        representation = 10;
    }

    @Override
    public void visit(IfElseStmt node) {
        representation = 11;
    }

    @Override
    public void visit(IfThenStmt node) {
        representation = 12;
    }

    @Override
    public void visit(WaitUntil node) {
        representation = 13;
    }

    @Override
    public void visit(UntilStmt node) {
        representation = 14;
    }

    @Override
    public void visit(Not node) {
        representation = 15;
    }

    @Override
    public void visit(And node) {
        representation = 16;
    }

    @Override
    public void visit(Or node) {
        representation = 17;
    }

    @Override
    public void visit(Broadcast node) {
        representation = 18;
    }

    @Override
    public void visit(BroadcastAndWait node) {
        representation = 19;
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        representation = 20;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        representation = 21;
    }

    @Override
    public void visit(CallStmt node) {
        representation = 22;
    }

    @Override
    public void visit(DeleteClone node) {
        representation = 23;
    }

    @Override
    public void visit(StopAll node) {
        representation = 24;
    }

    @Override
    public void visit(StmtList node) {
        representation = 25;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        representation = 26;
    }

    @Override
    public void visit(StringLiteral node) {
        representation = 27;
    }

    @Override
    public void visit(BoolLiteral node) {
        representation = 28;
    }

    @Override
    public void visit(NumberLiteral node) {
        representation = 29;
    }

    @Override
    public void visit(ColorLiteral node) {
        representation = 30;
    }

    @Override
    public void visit(LocalIdentifier node) {
        representation = 31;
    }

    @Override
    public void visit(Never node) {
        representation = 32;
    }

    @Override
    public void visit(ParameterDefinitionList node) {
        representation = 33;
    }

    @Override
    public void visit(ParameterDefinition node) {
        representation = 34;
    }

    @Override
    public void visit(ExpressionList node) {
        representation = 35;
    }

    @Override
    public void visit(Type node) {
        representation = 36;
    }

    @Override
    public void visit(SwitchBackdrop node) {
        representation = 37;
    }

    @Override
    public void visit(NextBackdrop node) {
        representation = 38;
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        representation = 39;
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        representation = 40;
    }

    @Override
    public void visit(KeyPressed node) {
        representation = 41;
    }

    @Override
    public void visit(MoveSteps node) {
        representation = 42;
    }

    @Override
    public void visit(ChangeXBy node) {
        representation = 43;
    }

    @Override
    public void visit(ChangeYBy node) {
        representation = 44;
    }

    @Override
    public void visit(SetXTo node) {
        representation = 45;
    }

    @Override
    public void visit(SetYTo node) {
        representation = 46;
    }

    @Override
    public void visit(GoToPos node) {
        representation = 47;
    }

    @Override
    public void visit(GoToPosXY node) {
        representation = 48;
    }

    @Override
    public void visit(TerminationStmt node) {
        representation = 49;
    }

    @Override
    public void visit(Qualified node) {
        representation = 50;
    }

    @Override
    public void visit(ColorTouchingColor node) {
        representation = 51;
    }

    @Override
    public void visit(Touching node) {
        representation = 52;
    }

    @Override
    public void visit(Clicked node) {
        representation = 53;
    }

    @Override
    public void visit(SetAttributeTo node) {
        representation = 54;
    }

    @Override
    public void visit(ActorDefinitionList node) {
        representation = 55;
    }

    @Override
    public void visit(ActorType node) {
        representation = 56;
    }

    @Override
    public void visit(Key node) {
        representation = 57;
    }

    @Override
    public void visit(Message node) {
        representation = 58;
    }

    @Override
    public void visit(Program node) {
        representation = 59;
    }

    @Override
    public void visit(SetStmtList node) {
        representation = 60;
    }

    @Override
    public void visit(ElementChoice node) {
        representation = 61;
    }

    @Override
    public void visit(Next node) {
        representation = 62;
    }

    @Override
    public void visit(Prev node) {
        representation = 63;
    }

    @Override
    public void visit(Random node) {
        representation = 64;
    }

    @Override
    public void visit(WithExpr node) {
        representation = 65;
    }

    @Override
    public void visit(Event node) {
        representation = 66;
    }

    @Override
    public void visit(GreenFlag node) {
        representation = 67;
    }

    @Override
    public void visit(AttributeAboveValue node) {
        representation = 68;
    }

    @Override
    public void visit(ComparableExpr node) {
        representation = 69;
    }

    @Override
    public void visit(Expression node) {
        representation = 70;
    }

    @Override
    public void visit(UnspecifiedExpression node) {
        representation = 71;
    }

    @Override
    public void visit(BoolExpr node) {
        representation = 72;
    }

    @Override
    public void visit(StringContains node) {
        representation = 73;
    }

    @Override
    public void visit(IsKeyPressed node) {
        representation = 74;
    }

    @Override
    public void visit(IsMouseDown node) {
        representation = 75;
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        representation = 76;
    }

    @Override
    public void visit(Color node) {
        representation = 77;
    }

    @Override
    public void visit(FromNumber node) {
        representation = 78;
    }

    @Override
    public void visit(NumExpr node) {
        representation = 79;
    }

    @Override
    public void visit(Add node) {
        representation = 80;
    }

    @Override
    public void visit(AsNumber node) {
        representation = 81;
    }

    @Override
    public void visit(Current node) {
        representation = 82;
    }

    @Override
    public void visit(DaysSince2000 node) {
        representation = 83;
    }

    @Override
    public void visit(DistanceTo node) {
        representation = 84;
    }

    @Override
    public void visit(Div node) {
        representation = 85;
    }

    @Override
    public void visit(IndexOf node) {
        representation = 86;
    }

    @Override
    public void visit(LengthOfString node) {
        representation = 87;
    }

    @Override
    public void visit(LengthOfVar node) {
        representation = 88;
    }

    @Override
    public void visit(Loudness node) {
        representation = 89;
    }

    @Override
    public void visit(Minus node) {
        representation = 90;
    }

    @Override
    public void visit(Mod node) {
        representation = 91;
    }

    @Override
    public void visit(MouseX node) {
        representation = 92;
    }

    @Override
    public void visit(MouseY node) {
        representation = 93;
    }

    @Override
    public void visit(Mult node) {
        representation = 94;
    }

    @Override
    public void visit(NumFunct node) {
        representation = 95;
    }

    @Override
    public void visit(NumFunctOf node) {
        representation = 96;
    }

    @Override
    public void visit(PickRandom node) {
        representation = 97;
    }

    @Override
    public void visit(Round node) {
        representation = 98;
    }

    @Override
    public void visit(Timer node) {
        representation = 99;
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        representation = 100;
    }

    @Override
    public void visit(StringExpr node) {
        representation = 101;
    }

    @Override
    public void visit(AsString node) {
        representation = 102;
    }

    @Override
    public void visit(AttributeOf node) {
        representation = 103;
    }

    @Override
    public void visit(ItemOfVariable node) {
        representation = 104;
    }

    @Override
    public void visit(Join node) {
        representation = 105;
    }

    @Override
    public void visit(LetterOf node) {
        representation = 106;
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        representation = 107;
    }

    @Override
    public void visit(Username node) {
        representation = 108;
    }

    @Override
    public void visit(Position node) {
        representation = 109;
    }

    @Override
    public void visit(MousePos node) {
        representation = 110;
    }

    @Override
    public void visit(FromExpression node) {
        representation = 111;
    }

    @Override
    public void visit(RandomPos node) {
        representation = 112;
    }

    @Override
    public void visit(ProcedureDefinitionList node) {
        representation = 113;
    }

    @Override
    public void visit(Stmt node) {
        representation = 114;
    }

    @Override
    public void visit(ExpressionStmt node) {
        representation = 115;
    }

    @Override
    public void visit(UnspecifiedStmt node) {
        representation = 116;
    }

    @Override
    public void visit(ActorLookStmt node) {
        representation = 117;
    }

    @Override
    public void visit(AskAndWait node) {
        representation = 118;
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        representation = 119;
    }

    @Override
    public void visit(ActorSoundStmt node) {
        representation = 120;
    }

    @Override
    public void visit(ClearSoundEffects node) {
        representation = 121;
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        representation = 122;
    }

    @Override
    public void visit(StartSound node) {
        representation = 123;
    }

    @Override
    public void visit(StopAllSounds node) {
        representation = 124;
    }

    @Override
    public void visit(CommonStmt node) {
        representation = 125;
    }

    @Override
    public void visit(ChangeVariableBy node) {
        representation = 126;
    }

    @Override
    public void visit(ResetTimer node) {
        representation = 127;
    }

    @Override
    public void visit(SetVariableTo node) {
        representation = 128;
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        representation = 129;
    }

    @Override
    public void visit(WaitSeconds node) {
        representation = 130;
    }

    @Override
    public void visit(ControlStmt node) {
        representation = 131;
    }

    @Override
    public void visit(IfStmt node) {
        representation = 132;
    }

    @Override
    public void visit(DeclarationStmt node) {
        representation = 133;
    }

    @Override
    public void visit(DeclarationAttributeAsTypeStmt node) {
        representation = 134;
    }

    @Override
    public void visit(DeclarationAttributeOfIdentAsTypeStmt node) {
        representation = 135;
    }

    @Override
    public void visit(DeclarationIdentAsTypeStmt node) {
        representation = 136;
    }

    @Override
    public void visit(DeclarationStmtList node) {
        representation = 137;
    }

    @Override
    public void visit(DeclarationBroadcastStmt node) {
        representation = 138;
    }

    @Override
    public void visit(ListStmt node) {
        representation = 139;
    }

    @Override
    public void visit(AddTo node) {
        representation = 140;
    }

    @Override
    public void visit(DeleteAllOf node) {
        representation = 141;
    }

    @Override
    public void visit(DeleteOf node) {
        representation = 142;
    }

    @Override
    public void visit(InsertAt node) {
        representation = 143;
    }

    @Override
    public void visit(ReplaceItem node) {
        representation = 144;
    }

    @Override
    public void visit(SpriteLookStmt node) {
        representation = 145;
    }

    @Override
    public void visit(ChangeLayerBy node) {
        representation = 146;
    }

    @Override
    public void visit(ChangeSizeBy node) {
        representation = 147;
    }

    @Override
    public void visit(GoToLayer node) {
        representation = 148;
    }

    @Override
    public void visit(Hide node) {
        representation = 149;
    }

    @Override
    public void visit(HideVariable node) {
        representation = 150;
    }

    @Override
    public void visit(HideList node) {
        representation = 151;
    }

    @Override
    public void visit(ShowList node) {
        representation = 152;
    }

    @Override
    public void visit(Say node) {
        representation = 153;
    }

    @Override
    public void visit(SayForSecs node) {
        representation = 154;
    }

    @Override
    public void visit(SetSizeTo node) {
        representation = 155;
    }

    @Override
    public void visit(Show node) {
        representation = 156;
    }

    @Override
    public void visit(ShowVariable node) {
        representation = 157;
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        representation = 158;
    }

    @Override
    public void visit(NextCostume node) {
        representation = 159;
    }

    @Override
    public void visit(Think node) {
        representation = 160;
    }

    @Override
    public void visit(ThinkForSecs node) {
        representation = 161;
    }

    @Override
    public void visit(SpriteMotionStmt node) {
        representation = 162;
    }

    @Override
    public void visit(GlideSecsTo node) {
        representation = 163;
    }

    @Override
    public void visit(GlideSecsToXY node) {
        representation = 164;
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        representation = 165;
    }

    @Override
    public void visit(PointInDirection node) {
        representation = 166;
    }

    @Override
    public void visit(PointTowards node) {
        representation = 167;
    }

    @Override
    public void visit(TurnLeft node) {
        representation = 168;
    }

    @Override
    public void visit(TurnRight node) {
        representation = 169;
    }

    @Override
    public void visit(StopThisScript node) {
        representation = 170;
    }

    @Override
    public void visit(TimeComp node) {
        representation = 171;
    }

    @Override
    public void visit(Touchable node) {
        representation = 172;
    }

    @Override
    public void visit(Edge node) {
        representation = 173;
    }

    @Override
    public void visit(MousePointer node) {
        representation = 174;
    }

    @Override
    public void visit(SpriteTouchable node) {
        representation = 175;
    }

    @Override
    public void visit(BooleanType node) {
        representation = 176;
    }

    @Override
    public void visit(ListType node) {
        representation = 177;
    }

    @Override
    public void visit(NumberType node) {
        representation = 178;
    }

    @Override
    public void visit(StringType node) {
        representation = 179;
    }

    @Override
    public void visit(Identifier node) {
        representation = 180;
    }

    @Override
    public void visit(AsBool node) {
        representation = 181;
    }

    @Override
    public void visit(AsTouchable node) {
        representation = 182;
    }

    @Override
    public void visit(ScriptList node) {
        representation = 183;
    }

    @Override
    public void visit(SpriteClicked node) {
        representation = 184;
    }

    @Override
    public void visit(StageClicked node) {
        representation = 185;
    }

    @Override
    public void visit(Costume node) {
        representation = 186;
    }

    @Override
    public void visit(Backdrop node) {
        representation = 187;
    }

    @Override
    public void visit(Direction node) {
        representation = 188;
    }

    @Override
    public void visit(PositionX node) {
        representation = 189;
    }

    @Override
    public void visit(PositionY node) {
        representation = 190;
    }

    @Override
    public void visit(Size node) {
        representation = 191;
    }

    @Override
    public void visit(Volume node) {
        representation = 192;
    }

    @Override
    public void visit(Answer node) {
        representation = 193;
    }

    @Override
    public void visit(NameNum node) {
        representation = 194;
    }

    @Override
    public void visit(FixedAttribute node) {
        representation = 195;
    }

    @Override
    public void visit(Attribute node) {
        representation = 196;
    }

    @Override
    public void visit(AttributeFromFixed node) {
        representation = 197;
    }

    @Override
    public void visit(AttributeFromVariable node) {
        representation = 198;
    }

    @Override
    public void visit(LayerChoice node) {
        representation = 199;
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        representation = 200;
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        representation = 201;
    }

    @Override
    public void visit(GraphicEffect node) {
        representation = 202;
    }

    @Override
    public void visit(SoundEffect node) {
        representation = 203;
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        representation = 204;
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        representation = 205;
    }

    @Override
    public void visit(SetVolumeTo node) {
        representation = 206;
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        representation = 207;
    }

    @Override
    public void visit(DragMode node) {
        representation = 208;
    }

    @Override
    public void visit(RotationStyle node) {
        representation = 209;
    }

    @Override
    public void visit(SetRotationStyle node) {
        representation = 210;
    }

    @Override
    public void visit(SetDragMode node) {
        representation = 211;
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        representation = 212;
    }

    @Override
    public void visit(DataExpr node) {
        representation = 213;
    }

    @Override
    public void visit(Variable node) {
        representation = 214;
    }

    @Override
    public void visit(ScratchList node) {
        representation = 215;
    }

    @Override
    public void visit(Parameter node) {
        representation = 216;
    }

    @Override
    public void visit(ListContains node) {
        representation = 217;
    }

    @Override
    public void visit(EventAttribute node) {
        representation = 218;
    }

    @Override
    public void visit(Metadata node) {
        representation = 219;
    }

    @Override
    public void visit(MetaMetadata node) {
        representation = 220;
    }

    @Override
    public void visit(ExtensionMetadata node) {
        representation = 221;
    }

    @Override
    public void visit(CommentMetadata node) {
        representation = 222;
    }

    @Override
    public void visit(ProgramMetadata node) {
        representation = 223;
    }

    @Override
    public void visit(ResourceMetadata node) {
        representation = 224;
    }

    @Override
    public void visit(ImageMetadata node) {
        representation = 225;
    }

    @Override
    public void visit(SoundMetadata node) {
        representation = 226;
    }

    @Override
    public void visit(MonitorMetadata node) {
        representation = 227;
    }

    @Override
    public void visit(MonitorSliderMetadata node) {
        representation = 228;
    }

    @Override
    public void visit(MonitorListMetadata node) {
        representation = 229;
    }

    @Override
    public void visit(MonitorParamMetadata node) {
        representation = 230;
    }

    @Override
    public void visit(BlockMetadata node) {
        representation = 231;
    }

    @Override
    public void visit(DataBlockMetadata node) {
        representation = 232;
    }

    @Override
    public void visit(NonDataBlockMetadata node) {
        representation = 233;
    }

    @Override
    public void visit(TopNonDataBlockMetadata node) {
        representation = 234;
    }

    @Override
    public void visit(MutationMetadata node) {
        representation = 235;
    }

    @Override
    public void visit(NoMutationMetadata node) {
        representation = 236;
    }

    @Override
    public void visit(ProcedureMutationMetadata node) {
        representation = 237;
    }

    @Override
    public void visit(ActorMetadata node) {
        representation = 238;
    }

    @Override
    public void visit(StageMetadata node) {
        representation = 239;
    }

    @Override
    public void visit(CommentMetadataList node) {
        representation = 240;
    }

    @Override
    public void visit(ImageMetadataList node) {
        representation = 241;
    }

    @Override
    public void visit(MonitorMetadataList node) {
        representation = 242;
    }

    @Override
    public void visit(MonitorParamMetadataList node) {
        representation = 243;
    }

    @Override
    public void visit(SoundMetadataList node) {
        representation = 244;
    }

    @Override
    public void visit(NoMetadata node) {
        representation = 245;
    }

    @Override
    public void visit(NoBlockMetadata node) {
        representation = 246;
    }

    @Override
    public void visit(ProcedureMetadata node) {
        representation = 247;
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        representation = 248;
    }

    @Override
    public void visit(TopNonDataBlockWithMenuMetadata node) {
        representation = 249;
    }

    @Override
    public void visit(LoopStmt node) {
        representation = 250;
    }

    @Override
    public void visitDefaultVisitor(ASTNode node) {
        representation = 251;
    }

    @Override
    public void visit(ExtensionBlock node) {
        representation = 252;
    }

    @Override
    public void visit(MBlockNode node) {
        representation = 253;
    }

    @Override
    public void visit(MusicBlock node) {
        representation = 254;
    }

    @Override
    public void visit(PenStmt node) {
        representation = 255;
    }

    @Override
    public void visit(TextToSpeechBlock node) {
        representation = 256;
    }

    @Override
    public void visit(TranslateBlock node) {
        representation = 257;
    }
}
