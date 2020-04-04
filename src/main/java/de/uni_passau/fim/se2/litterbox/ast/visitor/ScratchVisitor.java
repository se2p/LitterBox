/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorType;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.URI;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.event.BackdropSwitchTo;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Clicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StageClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.event.VariableAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.AsBool;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ExpressionContains;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsKeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.IsMouseDown;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.AsListIndex;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ListExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Current;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DaysSince2000;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Div;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.IndexOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.LengthOfVar;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Loudness;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Minus;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Mod;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseX;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.MouseY;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Mult;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunct;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumFunctOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.PickRandom;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Round;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Timer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.ItemOfVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.CoordinatePosition;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.PivotOf;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterDefiniton;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinitionList;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.ImageResource;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.Resource;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.ResourceList;
import de.uni_passau.fim.se2.litterbox.ast.model.resource.SoundResource;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ActorLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ClearGraphicEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ActorSoundStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ClearSoundEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StartSound;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeAttributeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CommonStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationAttributeAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationAttributeOfIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationIdentAsTypeStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteAllOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.DeleteOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ListStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.ChangePenColorParamBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStampStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.SetPenColorParamTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeLayerBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToBackLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToFrontLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.GoToLayer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.NextCostume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Show;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GlideSecsTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.IfOnEdgeBounce;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SpriteMotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.SpriteTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Rgba;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ImageType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.ListType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.NumberType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.SoundType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.StringType;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Id;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

public interface ScratchVisitor {

    /**
     * Default implementation of visit method for ASTNode.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ASTNode of which the children will be iterated
     */
    default void visit(ASTNode node) {
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }

    /**
     * Default implementation of visit method for ActorDefinition.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorDefinition of which the children will be iterated
     */
    default void visit(ActorDefinition node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for PenDownStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenDownStmt of which the children will be iterated
     */
    default void visit(PenDownStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     */
    default void visit(PenUpStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     */
    default void visit(PenClearStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for SetStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetStmt of which the children will be iterated
     */
    default void visit(SetStmt node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Equals}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Equals Node of which the children will be iterated
     */
    default void visit(Equals node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link LessThan}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LessThan Node of which the children will be iterated
     */
    default void visit(LessThan node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link BiggerThan}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BiggerThan Node of which the children will be iterated
     */
    default void visit(BiggerThan node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProcedureDefinition Node of which the children will be iterated
     */
    default void visit(ProcedureDefinition node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link StrId}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StrId of which the children will be iterated
     */
    default void visit(StrId node) {
        visit((Identifier) node);
    }

    /**
     * Default implementation of visit method for {@link Script}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Script of which the children will be iterated
     */
    default void visit(Script node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link CreateCloneOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CreateCloneOf Node of which the children will be iterated
     */
    default void visit(CreateCloneOf node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StartedAsClone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StartedAsClone Node of which the children will be iterated
     */
    default void visit(StartedAsClone node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link IfElseStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfElseStmt Node of which the children will be iterated
     */
    default void visit(IfElseStmt node) {
        visit((IfStmt) node);
    }

    /**
     * Default implementation of visit method for {@link IfThenStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfThenStmt Node of which the children will be iterated
     */
    default void visit(IfThenStmt node) {
        visit((IfStmt) node);
    }

    /**
     * Default implementation of visit method for {@link WaitUntil}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node WaitUntil Node of which the children will be iterated
     */
    default void visit(WaitUntil node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link UntilStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UntilStmt Node of which the children will be iterated
     */
    default void visit(UntilStmt node) {
        visit((ControlStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Not}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Not Node of which the children will be iterated
     */
    default void visit(Not node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link And}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node And Node of which the children will be iterated
     */
    default void visit(And node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Or}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node And Node of which the children will be iterated
     */
    default void visit(Or node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Broadcast}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Broadcast Node of which the children will be iterated
     */
    default void visit(Broadcast node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link BroadcastAndWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BroadcastAndWait Node of which the children will be iterated
     */
    default void visit(BroadcastAndWait node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ReceptionOfMessage}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ReceptionOfMessage Node of which the children will be iterated
     */
    default void visit(ReceptionOfMessage node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link RepeatForeverStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RepeatForeverStmt Node of which the children will be iterated
     */
    default void visit(RepeatForeverStmt node) {
        visit((ControlStmt) node);
    }

    /**
     * Default implementation of visit method for {@link CallStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CallStmt Node of which the children will be iterated
     */
    default void visit(CallStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeleteClone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeleteClone Node of which the children will be iterated
     */
    default void visit(DeleteClone node) {
        visit((TerminationStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StopAll}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopAll Node of which the children will be iterated
     */
    default void visit(StopAll node) {
        visit((TerminationStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StmtList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StmtList Node of which the children will be iterated
     */
    default void visit(StmtList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link RepeatTimesStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RepeatTimesStmt Node of which the children will be iterated
     */
    default void visit(RepeatTimesStmt node) {
        visit((ControlStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StringLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StringLiteral Node of which the children will be iterated
     */
    default void visit(StringLiteral node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link BoolLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoolLiteral Node of which the children will be iterated
     */
    default void visit(BoolLiteral node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link NumberLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumberLiteral Node of which the children will be iterated
     */
    default void visit(NumberLiteral node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ColorLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ColorLiteral Node of which the children will be iterated
     */
    default void visit(ColorLiteral node) {
        visit((Color) node);
    }

    /**
     * Default implementation of visit method for {@link Identifier}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Identifier Node of which the children will be iterated
     */
    default void visit(Identifier node) {
        visit((Variable) node);
    }

    /**
     * Default implementation of visit method for {@link Never}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Never Node of which the children will be iterated
     */
    default void visit(Never node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link ParameterDefinitionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterDefinitionList Node of which the children will be iterated
     */
    default void visit(ParameterDefinitionList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ParameterDefiniton}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterDefiniton Node of which the children will be iterated
     */
    default void visit(ParameterDefiniton node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ExpressionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionList Node of which the children will be iterated
     */
    default void visit(ExpressionList node) {
        visit((ListExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Type}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Type Node of which the children will be iterated
     */
    default void visit(Type node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SwitchBackdrop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SwitchBackdrop Node of which the children will be iterated
     */
    default void visit(SwitchBackdrop node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link NextBackdrop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NextBackdrop Node of which the children will be iterated
     */
    default void visit(NextBackdrop node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SwitchBackdropAndWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SwitchBackdropAndWait Node of which the children will be iterated
     */
    default void visit(SwitchBackdropAndWait node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link BackdropSwitchTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BackdropSwitchTo Node of which the children will be iterated
     */
    default void visit(BackdropSwitchTo node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link KeyPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node KeyPressed Node of which the children will be iterated
     */
    default void visit(KeyPressed node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link MoveSteps}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveSteps Node of which the children will be iterated
     */
    default void visit(MoveSteps node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeXBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeXBy Node of which the children will be iterated
     */
    default void visit(ChangeXBy node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeYBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeYBy Node of which the children will be iterated
     */
    default void visit(ChangeYBy node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetXTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetXTo Node of which the children will be iterated
     */
    default void visit(SetXTo node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetYTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetYTo Node of which the children will be iterated
     */
    default void visit(SetYTo node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link GoToPos}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToPos Node of which the children will be iterated
     */
    default void visit(GoToPos node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TerminationStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TerminationStmt Node of which the children will be iterated
     */
    default void visit(TerminationStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link Qualified}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Qualified Node of which the children will be iterated
     */
    default void visit(Qualified node) {
        visit((Variable) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenColorToColorStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorToColorStmt  Node of which the children will be iterated
     */
    default void visit(SetPenColorToColorStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ColorTouchingColor}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ColorTouchingColor  Node of which the children will be iterated
     */
    default void visit(ColorTouchingColor node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Touching}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Touching  Node of which the children will be iterated
     */
    default void visit(Touching node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Clicked}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Clicked  Node of which the children will be iterated
     */
    default void visit(Clicked node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link PenStampStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenStampStmt  Node of which the children will be iterated
     */
    default void visit(PenStampStmt node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangePenColorParamBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangePenColorParamBy  Node of which the children will be iterated
     */
    default void visit(ChangePenColorParamBy node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenColorParamTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorParamTo Node of which the children will be iterated
     */
    default void visit(SetPenColorParamTo node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeAttributeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeAttributeBy  Node of which the children will be iterated
     */
    default void visit(ChangeAttributeBy node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetAttributeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetAttributeTo  Node of which the children will be iterated
     */
    default void visit(SetAttributeTo node) {
        visit((SetStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ActorDefinitionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorDefinitionList  Node of which the children will be iterated
     */
    default void visit(ActorDefinitionList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ActorType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorType  Node of which the children will be iterated
     */
    default void visit(ActorType node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Key}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Key  Node of which the children will be iterated
     */
    default void visit(Key node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Message}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Message  Node of which the children will be iterated
     */
    default void visit(Message node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Program}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Program  Node of which the children will be iterated
     */
    default void visit(Program node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SetStmtList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetStmtList  Node of which the children will be iterated
     */
    default void visit(SetStmtList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link URI}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node URI  Node of which the children will be iterated
     */
    default void visit(URI node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ElementChoice}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ElementChoice  Node of which the children will be iterated
     */
    default void visit(ElementChoice node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Next}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Next  Node of which the children will be iterated
     */
    default void visit(Next node) {
        visit((ElementChoice) node);
    }

    /**
     * Default implementation of visit method for {@link Prev}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Prev  Node of which the children will be iterated
     */
    default void visit(Prev node) {
        visit((ElementChoice) node);
    }

    /**
     * Default implementation of visit method for {@link Random}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Random  Node of which the children will be iterated
     */
    default void visit(Random node) {
        visit((ElementChoice) node);
    }

    /**
     * Default implementation of visit method for {@link WithExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node WithExpr  Node of which the children will be iterated
     */
    default void visit(WithExpr node) {
        visit((ElementChoice) node);
    }

    /**
     * Default implementation of visit method for {@link Event}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Event  Node of which the children will be iterated
     */
    default void visit(Event node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link GreenFlag}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GreenFlag  Node of which the children will be iterated
     */
    default void visit(GreenFlag node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link VariableAboveValue}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node VariableAboveValue  Node of which the children will be iterated
     */
    default void visit(VariableAboveValue node) {
        visit((Event) node);
    }

    /**
     * Default implementation of visit method for {@link ComparableExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ComparableExpr  Node of which the children will be iterated
     */
    default void visit(ComparableExpr node) {
        visit((Expression) node);
    }

    /**
     * Default implementation of visit method for {@link Expression}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Expression  Node of which the children will be iterated
     */
    default void visit(Expression node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedExpression}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedExpression  Node of which the children will be iterated
     */
    default void visit(UnspecifiedExpression node) {
        visit((Expression) node);
    }

    /**
     * Default implementation of visit method for {@link BoolExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoolExpr  Node of which the children will be iterated
     */
    default void visit(BoolExpr node) {
        visit((Expression) node);
    }

    /**
     * Default implementation of visit method for {@link ExpressionContains}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionContains  Node of which the children will be iterated
     */
    default void visit(ExpressionContains node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link IsKeyPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IsKeyPressed  Node of which the children will be iterated
     */
    default void visit(IsKeyPressed node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link IsMouseDown}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IsMouseDown  Node of which the children will be iterated
     */
    default void visit(IsMouseDown node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedBoolExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedBoolExpr  Node of which the children will be iterated
     */
    default void visit(UnspecifiedBoolExpr node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Color}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Color  Node of which the children will be iterated
     */
    default void visit(Color node) {
        visit((Touchable) node);
    }

    /**
     * Default implementation of visit method for {@link FromNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FromNumber  Node of which the children will be iterated
     */
    default void visit(FromNumber node) {
        visit((Color) node);
    }

    /**
     * Default implementation of visit method for {@link Rgba}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Rgba  Node of which the children will be iterated
     */
    default void visit(Rgba node) {
        visit((Color) node);
    }

    /**
     * Default implementation of visit method for {@link ListExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListExpr  Node of which the children will be iterated
     */
    default void visit(ListExpr node) {
        visit((Expression) node);
    }

    /**
     * Default implementation of visit method for {@link NumExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumExpr  Node of which the children will be iterated
     */
    default void visit(NumExpr node) {
        visit((ComparableExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Add}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Add  Node of which the children will be iterated
     */
    default void visit(Add node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsNumber  Node of which the children will be iterated
     */
    default void visit(AsNumber node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsNumber  Node of which the children will be iterated
     */
    default void visit(Current node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DaysSince2000}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DaysSince2000  Node of which the children will be iterated
     */
    default void visit(DaysSince2000 node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DistanceTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DistanceTo  Node of which the children will be iterated
     */
    default void visit(DistanceTo node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Div}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Div  Node of which the children will be iterated
     */
    default void visit(Div node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link IndexOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IndexOf  Node of which the children will be iterated
     */
    default void visit(IndexOf node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link LengthOfString}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LengthOfString  Node of which the children will be iterated
     */
    default void visit(LengthOfString node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link LengthOfVar}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LengthOfVar  Node of which the children will be iterated
     */
    default void visit(LengthOfVar node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Loudness}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Loudness  Node of which the children will be iterated
     */
    default void visit(Loudness node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Minus}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Minus  Node of which the children will be iterated
     */
    default void visit(Minus node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Mod}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Mod  Node of which the children will be iterated
     */
    default void visit(Mod node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link MouseX}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MouseX  Node of which the children will be iterated
     */
    default void visit(MouseX node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link MouseY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MouseY  Node of which the children will be iterated
     */
    default void visit(MouseY node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Mult}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Mult  Node of which the children will be iterated
     */
    default void visit(Mult node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link NumFunct}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumFunct  Node of which the children will be iterated
     */
    default void visit(NumFunct node) { // TODO how to deal with enums
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link NumFunctOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumFunctOf  Node of which the children will be iterated
     */
    default void visit(NumFunctOf node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link PickRandom}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PickRandom  Node of which the children will be iterated
     */
    default void visit(PickRandom node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Round}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Round  Node of which the children will be iterated
     */
    default void visit(Round node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Timer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Timer  Node of which the children will be iterated
     */
    default void visit(Timer node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedNumExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedNumExpr  Node of which the children will be iterated
     */
    default void visit(UnspecifiedNumExpr node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link StringExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StringExpr  Node of which the children will be iterated
     */
    default void visit(StringExpr node) {
        visit((ComparableExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AsString}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsString  Node of which the children will be iterated
     */
    default void visit(AsString node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AttributeOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AttributeOf  Node of which the children will be iterated
     */
    default void visit(AttributeOf node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ItemOfVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ItemOfVariable  Node of which the children will be iterated
     */
    default void visit(ItemOfVariable node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Join}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Join  Node of which the children will be iterated
     */
    default void visit(Join node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link LetterOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LetterOf  Node of which the children will be iterated
     */
    default void visit(LetterOf node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedStringExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedStringExpr  Node of which the children will be iterated
     */
    default void visit(UnspecifiedStringExpr node) { //FIXME visit StringExpr or Unspecified?
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Username}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Username  Node of which the children will be iterated
     */
    default void visit(Username node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Position}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Position  Node of which the children will be iterated
     */
    default void visit(Position node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link CoordinatePosition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CoordinatePosition  Node of which the children will be iterated
     */
    default void visit(CoordinatePosition node) {
        visit((Position) node);
    }

    /**
     * Default implementation of visit method for {@link MousePos}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MousePos  Node of which the children will be iterated
     */
    default void visit(MousePos node) {
        visit((Position) node);
    }

    /**
     * Default implementation of visit method for {@link PivotOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PivotOf  Node of which the children will be iterated
     */
    default void visit(PivotOf node) {
        visit((Position) node);
    }

    /**
     * Default implementation of visit method for {@link PivotOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PivotOf  Node of which the children will be iterated
     */
    default void visit(RandomPos node) {
        visit((Position) node);
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinitionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProcedureDefinitionList  Node of which the children will be iterated
     */
    default void visit(ProcedureDefinitionList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Resource}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Resource  Node of which the children will be iterated
     */
    default void visit(Resource node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ImageResource}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ImageResource  Node of which the children will be iterated
     */
    default void visit(ImageResource node) {
        visit((Resource) node);
    }

    /**
     * Default implementation of visit method for {@link ResourceList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResourceList  Node of which the children will be iterated
     */
    default void visit(ResourceList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SoundResource}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundResource  Node of which the children will be iterated
     */
    default void visit(SoundResource node) {
        visit((Resource) node);
    }

    /**
     * Default implementation of visit method for {@link Stmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Stmt  Node of which the children will be iterated
     */
    default void visit(Stmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ExpressionStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionStmt  Node of which the children will be iterated
     */
    default void visit(ExpressionStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedStmt  Node of which the children will be iterated
     */
    default void visit(UnspecifiedStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link ActorLookStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorLookStmt  Node of which the children will be iterated
     */
    default void visit(ActorLookStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link AskAndWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AskAndWait  Node of which the children will be iterated
     */
    default void visit(AskAndWait node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ClearGraphicEffects}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ClearGraphicEffects  Node of which the children will be iterated
     */
    default void visit(ClearGraphicEffects node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ActorSoundStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorSoundStmt  Node of which the children will be iterated
     */
    default void visit(ActorSoundStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link ClearSoundEffects}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ClearSoundEffects  Node of which the children will be iterated
     */
    default void visit(ClearSoundEffects node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PlaySoundUntilDone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PlaySoundUntilDone  Node of which the children will be iterated
     */
    default void visit(PlaySoundUntilDone node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StartSound}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StartSound  Node of which the children will be iterated
     */
    default void visit(StartSound node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StopAllSounds}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopAllSounds  Node of which the children will be iterated
     */
    default void visit(StopAllSounds node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link CommonStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CommonStmt  Node of which the children will be iterated
     */
    default void visit(CommonStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeVariableBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeVariableBy  Node of which the children will be iterated
     */
    default void visit(ChangeVariableBy node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ResetTimer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResetTimer  Node of which the children will be iterated
     */
    default void visit(ResetTimer node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetVariableTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetVariableTo  Node of which the children will be iterated
     */
    default void visit(SetVariableTo node) {
        visit((SetStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StopOtherScriptsInSprite}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopOtherScriptsInSprite  Node of which the children will be iterated
     */
    default void visit(StopOtherScriptsInSprite node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link WaitSeconds}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node WaitSeconds  Node of which the children will be iterated
     */
    default void visit(WaitSeconds node) {
        visit((CommonStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ControlStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ControlStmt  Node of which the children will be iterated
     */
    default void visit(ControlStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link IfStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfStmt  Node of which the children will be iterated
     */
    default void visit(IfStmt node) {
        visit((ControlStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeclarationStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationStmt  Node of which the children will be iterated
     */
    default void visit(DeclarationStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeclarationAttributeAsTypeStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationAttributeAsTypeStmt  Node of which the children will be iterated
     */
    default void visit(DeclarationAttributeAsTypeStmt node) {
        visit((DeclarationStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeclarationAttributeOfIdentAsTypeStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationAttributeOfIdentAsTypeStmt  Node of which the children will be iterated
     */
    default void visit(DeclarationAttributeOfIdentAsTypeStmt node) {
        visit((DeclarationStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeclarationIdentAsTypeStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationIdentAsTypeStmt  Node of which the children will be iterated
     */
    default void visit(DeclarationIdentAsTypeStmt node) {
        visit((DeclarationStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeclarationStmtList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationStmtList  Node of which the children will be iterated
     */
    default void visit(DeclarationStmtList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ListStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListStmt  Node of which the children will be iterated
     */
    default void visit(ListStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link AddTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AddTo  Node of which the children will be iterated
     */
    default void visit(AddTo node) {
        visit((ListStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeleteAllOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeleteAllOf  Node of which the children will be iterated
     */
    default void visit(DeleteAllOf node) {
        visit((ListStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DeleteOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeleteOf  Node of which the children will be iterated
     */
    default void visit(DeleteOf node) {
        visit((ListStmt) node);
    }

    /**
     * Default implementation of visit method for {@link InsertAt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node InsertAt  Node of which the children will be iterated
     */
    default void visit(InsertAt node) {
        visit((ListStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ReplaceItem}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ReplaceItem  Node of which the children will be iterated
     */
    default void visit(ReplaceItem node) {
        visit((ListStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PenStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenStmt  Node of which the children will be iterated
     */
    default void visit(PenStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteLookStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteLookStmt  Node of which the children will be iterated
     */
    default void visit(SpriteLookStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeLayerBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeLayerBy  Node of which the children will be iterated
     */
    default void visit(ChangeLayerBy node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeSizeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeSizeBy  Node of which the children will be iterated
     */
    default void visit(ChangeSizeBy node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link GoToBackLayer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToBackLayer  Node of which the children will be iterated
     */
    default void visit(GoToBackLayer node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link GoToFrontLayer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToFrontLayer  Node of which the children will be iterated
     */
    default void visit(GoToFrontLayer node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link GoToLayer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToLayer  Node of which the children will be iterated
     */
    default void visit(GoToLayer node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Hide}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Hide  Node of which the children will be iterated
     */
    default void visit(Hide node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link HideVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node HideVariable  Node of which the children will be iterated
     */
    default void visit(HideVariable node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Say}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Say  Node of which the children will be iterated
     */
    default void visit(Say node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SayForSecs}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SayForSecs  Node of which the children will be iterated
     */
    default void visit(SayForSecs node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetSizeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetSizeTo  Node of which the children will be iterated
     */
    default void visit(SetSizeTo node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Show}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Show  Node of which the children will be iterated
     */
    default void visit(Show node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ShowVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShowVariable  Node of which the children will be iterated
     */
    default void visit(ShowVariable node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SwitchCostumeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SwitchCostumeTo  Node of which the children will be iterated
     */
    default void visit(SwitchCostumeTo node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link NextCostume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NextCostume  Node of which the children will be iterated
     */
    default void visit(NextCostume node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link Think}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Think  Node of which the children will be iterated
     */
    default void visit(Think node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ThinkForSecs}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ThinkForSecs  Node of which the children will be iterated
     */
    default void visit(ThinkForSecs node) {
        visit((SpriteLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteMotionStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteMotionStmt  Node of which the children will be iterated
     */
    default void visit(SpriteMotionStmt node) {
        visit((Stmt) node);
    }

    /**
     * Default implementation of visit method for {@link GlideSecsTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GlideSecsTo  Node of which the children will be iterated
     */
    default void visit(GlideSecsTo node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link IfOnEdgeBounce}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfOnEdgeBounce  Node of which the children will be iterated
     */
    default void visit(IfOnEdgeBounce node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PointInDirection}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PointInDirection  Node of which the children will be iterated
     */
    default void visit(PointInDirection node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link PointTowards}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PointTowards  Node of which the children will be iterated
     */
    default void visit(PointTowards node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnLeft}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnLeft  Node of which the children will be iterated
     */
    default void visit(TurnLeft node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TurnRight}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnRight  Node of which the children will be iterated
     */
    default void visit(TurnRight node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link StopThisScript}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopThisScript  Node of which the children will be iterated
     */
    default void visit(StopThisScript node) {
        visit((TerminationStmt) node);
    }

    /**
     * Default implementation of visit method for {@link TimeComp}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TimeComp  Node of which the children will be iterated
     */
    default void visit(TimeComp node) { //TODO enum
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Touchable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Touchable  Node of which the children will be iterated
     */
    default void visit(Touchable node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Edge}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Edge  Node of which the children will be iterated
     */
    default void visit(Edge node) {
        visit((Touchable) node);
    }

    /**
     * Default implementation of visit method for {@link MousePointer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MousePointer  Node of which the children will be iterated
     */
    default void visit(MousePointer node) {
        visit((Touchable) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteTouchable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteTouchable  Node of which the children will be iterated
     */
    default void visit(SpriteTouchable node) {
        visit((Touchable) node);
    }

    /**
     * Default implementation of visit method for {@link BooleanType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BooleanType  Node of which the children will be iterated
     */
    default void visit(BooleanType node) {
        visit((Type) node);
    }

    /**
     * Default implementation of visit method for {@link ImageType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ImageType  Node of which the children will be iterated
     */
    default void visit(ImageType node) {
        visit((Type) node);
    }

    /**
     * Default implementation of visit method for {@link ListType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListType  Node of which the children will be iterated
     */
    default void visit(ListType node) {
        visit((Type) node);
    }

    /**
     * Default implementation of visit method for {@link NumberType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumberType  Node of which the children will be iterated
     */
    default void visit(NumberType node) {
        visit((Type) node);
    }

    /**
     * Default implementation of visit method for {@link SoundType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundType  Node of which the children will be iterated
     */
    default void visit(SoundType node) {
        visit((Type) node);
    }

    /**
     * Default implementation of visit method for {@link StringType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StringType  Node of which the children will be iterated
     */
    default void visit(StringType node) {
        visit((Type) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteTouchable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteTouchable  Node of which the children will be iterated
     */
    default void visit(Variable node) {
        visit((Expression) node);
    }

    /**
     * Default implementation of visit method for {@link Id}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Id  Node of which the children will be iterated
     */
    default void visit(Id node) {
        visit((Identifier) node);
    }

    /**
     * Default implementation of visit method for {@link AsBool}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsBool Node of which the children will be iterated
     */
    default void visit(AsBool node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AsListIndex}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsListIndex Node of which the children will be iterated
     */
    default void visit(AsListIndex node) {
        visit((ListExpr) node);
    }

    /**
     * Default implementation of visit method for {@link AsTouchable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsTouchable Node of which the children will be iterated
     */
    default void visit(AsTouchable node) {
        visit((Touchable) node);
    }

    /**
     * Default implementation of visit method for {@link ScriptList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ScriptList Node of which the children will be iterated
     */
    default void visit(ScriptList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteClicked}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteClicked Node of which the children will be iterated
     */
    default void visit(SpriteClicked node) {
        visit((Clicked) node);
    }

    /**
     * Default implementation of visit method for {@link StageClicked}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StageClicked Node of which the children will be iterated
     */
    default void visit(StageClicked node) {
        visit((Clicked) node);
    }
}
