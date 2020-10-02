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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.SpriteMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.StageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.DataInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.InputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.ReferenceInputMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.input.TypeInputMetadata;
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
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

public interface ScratchVisitor {

    default void visitChildren(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

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
        visitChildren(node);
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
        visit((LocalIdentifier) node);
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
     * Default implementation of visit method for {@link LocalIdentifier}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Identifier Node of which the children will be iterated
     */
    default void visit(LocalIdentifier node) {
        visit((Identifier) node);
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
     * Default implementation of visit method for {@link ParameterDefinition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterDefiniton Node of which the children will be iterated
     */
    default void visit(ParameterDefinition node) {
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
        visit((Expression) node);
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
     * Default implementation of visit method for {@link GoToPosXY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToPos Node of which the children will be iterated
     */
    default void visit(GoToPosXY node) {
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
        visit((Identifier) node);
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
     * Default implementation of visit method for {@link AttributeAboveValue}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node VariableAboveValue  Node of which the children will be iterated
     */
    default void visit(AttributeAboveValue node) {
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
     * Default implementation of visit method for {@link StringContains}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionContains  Node of which the children will be iterated
     */
    default void visit(StringContains node) {
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
     * Default implementation of visit method for {@link FromExpression}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FromExpression  Node of which the children will be iterated
     */
    default void visit(FromExpression node) {
        visit((Position) node);
    }

    /**
     * Default implementation of visit method for {@link RandomPos}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RandomPos  Node of which the children will be iterated
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
     * Default implementation of visit method for {@link DeclarationBroadcastStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationStmtList  Node of which the children will be iterated
     */
    default void visit(DeclarationBroadcastStmt node) {
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
     * Default implementation of visit method for {@link HideList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node HideList  Node of which the children will be iterated
     */
    default void visit(HideList node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ShowList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShowList  Node of which the children will be iterated
     */
    default void visit(ShowList node) {
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
     * Default implementation of visit method for {@link GlideSecsToXY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GlideSecsToXY  Node of which the children will be iterated
     */
    default void visit(GlideSecsToXY node) {
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
     * Default implementation of visit method for {@link Identifier }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Identifier   Node of which the children will be iterated
     */
    default void visit(Identifier node) {
        visit((Expression) node);
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

    /**
     * Default implementation of visit method for {@link Costume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Costume Node of which the children will be iterated
     */
    default void visit(Costume node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Backdrop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Backdrop Node of which the children will be iterated
     */
    default void visit(Backdrop node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Direction}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Direction Node of which the children will be iterated
     */
    default void visit(Direction node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link PositionX}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PositionX Node of which the children will be iterated
     */
    default void visit(PositionX node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link PositionY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PositionY Node of which the children will be iterated
     */
    default void visit(PositionY node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Size}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Size Node of which the children will be iterated
     */
    default void visit(Size node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Volume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Volume Node of which the children will be iterated
     */
    default void visit(Volume node) {
        visit((NumExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Answer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Answer Node of which the children will be iterated
     */
    default void visit(Answer node) {
        visit((StringExpr) node);
    }

    /**
     * Default implementation of visit method for {@link NameNum}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NameNum Node of which the children will be iterated
     */
    default void visit(NameNum node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link FixedAttribute}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FixedAttribute Node of which the children will be iterated
     */
    default void visit(FixedAttribute node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Attribute}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Attribute Node of which the children will be iterated
     */
    default void visit(Attribute node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link AttributeFromFixed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AttributeFromFixed Node of which the children will be iterated
     */
    default void visit(AttributeFromFixed node) {
        visit((Attribute) node);
    }

    /**
     * Default implementation of visit method for {@link AttributeFromVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AttributeFromVariable Node of which the children will be iterated
     */
    default void visit(AttributeFromVariable node) {
        visit((Attribute) node);
    }

    /**
     * Default implementation of visit method for {@link LayerChoice}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LayerChoice Node of which the children will be iterated
     */
    default void visit(LayerChoice node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenSizeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenSizeTo Node of which the children will be iterated
     */
    default void visit(SetPenSizeTo node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangePenSizeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangePenSizeBy Node of which the children will be iterated
     */
    default void visit(ChangePenSizeBy node) {
        visit((PenStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetGraphicEffectTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetGraphicEffectTo Node of which the children will be iterated
     */
    default void visit(SetGraphicEffectTo node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeGraphicEffectBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeGraphicEffectBy Node of which the children will be iterated
     */
    default void visit(ChangeGraphicEffectBy node) {
        visit((ActorLookStmt) node);
    }

    /**
     * Default implementation of visit method for {@link GraphicEffect}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GraphicEffect Node of which the children will be iterated
     */
    default void visit(GraphicEffect node) {
        visit((ASTLeaf) node);
    }

    /**
     * Default implementation of visit method for {@link SoundEffect}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundEffect Node of which the children will be iterated
     */
    default void visit(SoundEffect node) {
        visit((ASTLeaf) node);
    }

    /**
     * Default implementation of visit method for {@link SetSoundEffectTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetSoundEffectTo Node of which the children will be iterated
     */
    default void visit(SetSoundEffectTo node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeSoundEffectBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeSoundEffectBy Node of which the children will be iterated
     */
    default void visit(ChangeSoundEffectBy node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetVolumeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetVolumeTo Node of which the children will be iterated
     */
    default void visit(SetVolumeTo node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeVolumeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeVolumeBy Node of which the children will be iterated
     */
    default void visit(ChangeVolumeBy node) {
        visit((ActorSoundStmt) node);
    }

    /**
     * Default implementation of visit method for {@link DragMode}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DragMode Node of which the children will be iterated
     */
    default void visit(DragMode node) {
        visit((ASTLeaf) node);
    }

    /**
     * Default implementation of visit method for {@link RotationStyle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RotationStyle Node of which the children will be iterated
     */
    default void visit(RotationStyle node) {
        visit((ASTLeaf) node);
    }

    /**
     * Default implementation of visit method for {@link SetRotationStyle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetRotationStyle Node of which the children will be iterated
     */
    default void visit(SetRotationStyle node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SetDragMode}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetDragMode Node of which the children will be iterated
     */
    default void visit(SetDragMode node) {
        visit((SpriteMotionStmt) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteTouchingColor}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteTouchingColor Node of which the children will
     *             be iterated
     */
    default void visit(SpriteTouchingColor node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link DataExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DataExpr Node of which the children will
     *             be iterated
     */
    default void visit(DataExpr node) {
        visit((Expression) node);
    }

    /**
     * Default implementation of visit method for {@link Variable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Variable Node of which the children will
     *             be iterated
     */
    default void visit(Variable node) {
        visit((DataExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ScratchList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ScratchList Node of which the children will
     *             be iterated
     */
    default void visit(ScratchList node) {
        visit((DataExpr) node);
    }

    /**
     * Default implementation of visit method for {@link Parameter}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Parameter Node of which the children will
     *             be iterated
     */
    default void visit(Parameter node) {
        visit((DataExpr) node);
    }

    /**
     * Default implementation of visit method for {@link ListContains}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListContains Node of which the children will
     *             be iterated
     */
    default void visit(ListContains node) {
        visit((BoolExpr) node);
    }

    /**
     * Default implementation of visit method for {@link EventAttribute}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node EventAttribute  Node of which the children will be iterated
     */
    default void visit(EventAttribute node) { //TODO enum
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Metadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Metadata Node of which the children will
     *             be iterated
     */
    default void visit(Metadata node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link VariableMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node VariableMetadata Node of which the children will
     *             be iterated
     */
    default void visit(VariableMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link MetaMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MetaMetadata Node of which the children will
     *             be iterated
     */
    default void visit(MetaMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ListMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ListMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ExtensionMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExtensionMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ExtensionMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link CommentMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CommentMetadata Node of which the children will
     *             be iterated
     */
    default void visit(CommentMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ProgramMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProgramMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ProgramMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link BroadcastMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BroadcastMetadata Node of which the children will
     *             be iterated
     */
    default void visit(BroadcastMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ResourceMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResourceMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ResourceMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ImageMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ImageMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ImageMetadata node) {
        visit((ResourceMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link SoundMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundMetadata Node of which the children will
     *             be iterated
     */
    default void visit(SoundMetadata node) {
        visit((ResourceMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link MonitorMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MonitorMetadata Node of which the children will
     *             be iterated
     */
    default void visit(MonitorMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link MonitorSliderMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MonitorSliderMetadata Node of which the children will
     *             be iterated
     */
    default void visit(MonitorSliderMetadata node) {
        visit((MonitorMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link MonitorListMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MonitorListMetadata Node of which the children will
     *             be iterated
     */
    default void visit(MonitorListMetadata node) {
        visit((MonitorMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link MonitorParamMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MonitorParamMetadata Node of which the children will
     *             be iterated
     */
    default void visit(MonitorParamMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link InputMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node InputMetadata Node of which the children will
     *             be iterated
     */
    default void visit(InputMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ReferenceInputMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ReferenceInputMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ReferenceInputMetadata node) {
        visit((InputMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link TypeInputMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TypeInputMetadata Node of which the children will
     *             be iterated
     */
    default void visit(TypeInputMetadata node) {
        visit((InputMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link DataInputMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DataInputMetadata Node of which the children will
     *             be iterated
     */
    default void visit(DataInputMetadata node) {
        visit((InputMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link BlockMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BlockMetadata Node of which the children will
     *             be iterated
     */
    default void visit(BlockMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link DataBlockMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DataBlockMetadata Node of which the children will
     *             be iterated
     */
    default void visit(DataBlockMetadata node) {
        visit((BlockMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link NonDataBlockMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NonDataBlockMetadata Node of which the children will
     *             be iterated
     */
    default void visit(NonDataBlockMetadata node) {
        visit((BlockMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link TopNonDataBlockMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TopNonDataBlockMetadata Node of which the children will
     *             be iterated
     */
    default void visit(TopNonDataBlockMetadata node) {
        visit((NonDataBlockMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link FieldsMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FieldsMetadata Node of which the children will
     *             be iterated
     */
    default void visit(FieldsMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link MutationMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MutationMetadata Node of which the children will
     *             be iterated
     */
    default void visit(MutationMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link NoMutationMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NoMutationMetadata Node of which the children will
     *             be iterated
     */
    default void visit(NoMutationMetadata node) {
        visit((MutationMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link CallMutationMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExistingCallMutationMetadata Node of which the children will
     *             be iterated
     */
    default void visit(CallMutationMetadata node) {
        visit((MutationMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link PrototypeMutationMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExistingPrototypeMutationMetadata Node of which the children will
     *             be iterated
     */
    default void visit(PrototypeMutationMetadata node) {
        visit((CallMutationMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link ActorMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ActorMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link StageMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StageMetadata Node of which the children will
     *             be iterated
     */
    default void visit(StageMetadata node) {
        visit((ActorMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link SpriteMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteMetadata Node of which the children will
     *             be iterated
     */
    default void visit(SpriteMetadata node) {
        visit((ActorMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link BroadcastMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BroadcastMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(BroadcastMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link CommentMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CommentMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(CommentMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link FieldsMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FieldsMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(FieldsMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ImageMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ImageMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(ImageMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link InputMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node InputMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(InputMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ListMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(ListMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link MonitorMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MonitorMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(MonitorMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link MonitorParamMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MonitorParamMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(MonitorParamMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SoundMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(SoundMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link VariableMetadataList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node VariableMetadataList Node of which the children will
     *             be iterated
     */
    default void visit(VariableMetadataList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link NoMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NoMetadata Node of which the children will
     *             be iterated
     */
    default void visit(NoMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link NoBlockMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NoBlockMetadata Node of which the children will
     *             be iterated
     */
    default void visit(NoBlockMetadata node) {
        visit((BlockMetadata) node);
    }

    /**
     * Default implementation of visit method for {@link ProcedureMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProcedureMetadata Node of which the children will
     *             be iterated
     */
    default void visit(ProcedureMetadata node) {
        visit((Metadata) node);
    }

    /**
     * Default implementation of visit method for {@link ForwardBackwardChoice}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ForwardBackwardChoice Node of which the children will be iterated
     */
    default void visit(ForwardBackwardChoice node) {
        visit((ASTLeaf) node);
    }

    /**
     * Default implementation of visit method for {@link CloneOfMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CloneOfMetadata Node of which the children will
     *             be iterated
     */
    default void visit(CloneOfMetadata node) {
        visit((BlockMetadata) node);
    }
}
