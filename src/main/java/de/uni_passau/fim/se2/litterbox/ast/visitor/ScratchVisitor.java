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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.AsListIndex;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionListPlain;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ListExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ParameterListPlain;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ListOfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.AsTouchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.type.Type;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;

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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ListOfStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListOfStmt Node of which the children will be iterated
     */
    default void visit(ListOfStmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ParameterList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterList Node of which the children will be iterated
     */
    default void visit(ParameterList node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ParameterListPlain}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterListPlain Node of which the children will be iterated
     */
    default void visit(ParameterListPlain node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Parameter}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Parameter Node of which the children will be iterated
     */
    default void visit(Parameter node) {
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
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ExpressionListPlain}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionListPlain Node of which the children will be iterated
     */
    default void visit(ExpressionListPlain node) {
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SetPenColorToColorStmt }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorToColorStmt  Node of which the children will be iterated
     */
    default void visit(SetPenColorToColorStmt node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ColorTouches }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ColorTouches  Node of which the children will be iterated
     */
    default void visit(ColorTouches node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Touching }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Touching  Node of which the children will be iterated
     */
    default void visit(Touching node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link Clicked }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Clicked  Node of which the children will be iterated
     */
    default void visit(Clicked node) {
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
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
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link ChangeAttributeBy }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeAttributeBy  Node of which the children will be iterated
     */
    default void visit(ChangeAttributeBy node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link SetAttributeTo }.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetAttributeTo  Node of which the children will be iterated
     */
    default void visit(SetAttributeTo node) {
        visit((ASTNode) node);
    }

    /**
     * Default implementation of visit method for {@link AsString}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsString Node of which the children will be iterated
     */
    default void visit(AsString node) {
        visit((StringExpr) node);
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
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsNumber Node of which the children will be iterated
     */
    default void visit(AsNumber node) {
        visit((NumExpr) node);
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
}
