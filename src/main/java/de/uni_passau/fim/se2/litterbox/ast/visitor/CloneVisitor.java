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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.*;
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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.type.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class CloneVisitor {

    @SuppressWarnings("unchecked")
    public <T extends ASTNode> T cloneNode(T node) {
        if (node == null) {
            return null;
        }
        ASTNode r = node.accept(this);
        if (r == null) {
            return null;
        }
        return (T) r;
    }

    public <T extends ASTNode> List<T> cloneList(List<T> theList) {
        List<T> result = new ArrayList<>();
        for (T node : theList) {
            result.add(cloneNode(node));
        }
        return result;
    }

    /**
     * Default implementation of visit method for ActorDefinition.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorDefinition of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ActorDefinition node) {
        return new ActorDefinition(cloneNode(node.getActorType()), cloneNode(node.getIdent()), cloneNode(node.getDecls()), cloneNode(node.getSetStmtList()), cloneNode(node.getProcedureDefinitionList()), cloneNode(node.getScripts()), cloneNode(node.getActorMetadata()));
    }

    /**
     * Default implementation of visit method for PenDownStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenDownStmt of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PenDownStmt node) {
        return new PenDownStmt(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PenUpStmt node) {
        return new PenUpStmt(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenUpStmt of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PenClearStmt node) {
        return new PenClearStmt(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Equals}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Equals Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Equals node) {
        return new Equals(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LessThan}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LessThan Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(LessThan node) {
        return new LessThan(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BiggerThan}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BiggerThan Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(BiggerThan node) {
        return new BiggerThan(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProcedureDefinition Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ProcedureDefinition node) {
        return new ProcedureDefinition(cloneNode(node.getIdent()), cloneNode(node.getParameterDefinitionList()), cloneNode(node.getStmtList()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StrId}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StrId of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StrId node) {
        return new StrId(node.getName());
    }

    /**
     * Default implementation of visit method for {@link Script}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Script of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Script node) {
        return new Script(cloneNode(node.getEvent()), cloneNode(node.getStmtList()));
    }

    /**
     * Default implementation of visit method for {@link CreateCloneOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CreateCloneOf Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(CreateCloneOf node) {
        return new CreateCloneOf(cloneNode(node.getStringExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StartedAsClone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StartedAsClone Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StartedAsClone node) {
        return new StartedAsClone(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfElseStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfElseStmt Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(IfElseStmt node) {
        return new IfElseStmt(cloneNode(node.getBoolExpr()), cloneNode(node.getStmtList()), cloneNode(node.getElseStmts()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfThenStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfThenStmt Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(IfThenStmt node) {
        return new IfThenStmt(cloneNode(node.getBoolExpr()), cloneNode(node.getThenStmts()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link WaitUntil}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node WaitUntil Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(WaitUntil node) {
        return new WaitUntil(cloneNode(node.getUntil()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UntilStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UntilStmt Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UntilStmt node) {
        return new UntilStmt(cloneNode(node.getBoolExpr()), cloneNode(node.getStmtList()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Not}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Not Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Not node) {
        return new Not(cloneNode(node.getOperand1()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link And}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node And Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(And node) {
        return new And(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Or}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node And Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Or node) {
        return new Or(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Broadcast}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Broadcast Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Broadcast node) {
        return new Broadcast(cloneNode(node.getMessage()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BroadcastAndWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BroadcastAndWait Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(BroadcastAndWait node) {
        return new BroadcastAndWait(cloneNode(node.getMessage()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ReceptionOfMessage}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ReceptionOfMessage Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ReceptionOfMessage visit(ReceptionOfMessage node) {
        return new ReceptionOfMessage(cloneNode(node.getMsg()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RepeatForeverStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RepeatForeverStmt Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(RepeatForeverStmt node) {
        return new RepeatForeverStmt(cloneNode(node.getStmtList()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link CallStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node CallStmt Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(CallStmt node) {
        return new CallStmt(cloneNode(node.getIdent()), cloneNode(node.getExpressions()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeleteClone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeleteClone Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeleteClone node) {
        return new DeleteClone(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopAll}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopAll Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StopAll node) {
        return new StopAll(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StmtList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StmtList Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StmtList node) {
        return new StmtList(cloneList(node.getStmts()));
    }

    /**
     * Default implementation of visit method for {@link RepeatTimesStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RepeatTimesStmt Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(RepeatTimesStmt node) {
        return new RepeatTimesStmt(cloneNode(node.getTimes()), cloneNode(node.getStmtList()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StringLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StringLiteral Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StringLiteral node) {
        return new StringLiteral(node.getText());
    }

    /**
     * Default implementation of visit method for {@link BoolLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BoolLiteral Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(BoolLiteral node) {
        return new BoolLiteral(node.getValue());
    }

    /**
     * Default implementation of visit method for {@link NumberLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumberLiteral Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NumberLiteral node) {
        return new NumberLiteral(node.getValue());
    }

    /**
     * Default implementation of visit method for {@link ColorLiteral}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ColorLiteral Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ColorLiteral node) {
        return new ColorLiteral(node.getRed(), node.getGreen(), node.getBlue());
    }

    /**
     * Default implementation of visit method for {@link Never}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Never Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Never node) {
        return new Never();
    }

    /**
     * Default implementation of visit method for {@link ParameterDefinitionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterDefinitionList Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ParameterDefinitionList node) {
        return new ParameterDefinitionList(cloneList(node.getParameterDefinitions()));
    }

    /**
     * Default implementation of visit method for {@link ParameterDefinition}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ParameterDefiniton Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ParameterDefinition node) {
        return new ParameterDefinition(cloneNode(node.getIdent()), cloneNode(node.getType()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ExpressionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionList Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ExpressionList node) {
        return new ExpressionList(cloneList(node.getExpressions()));
    }

    /**
     * Default implementation of visit method for {@link SwitchBackdrop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SwitchBackdrop Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SwitchBackdrop node) {
        return new SwitchBackdrop(cloneNode(node.getElementChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NextBackdrop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NextBackdrop Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NextBackdrop node) {
        return new NextBackdrop(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SwitchBackdropAndWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SwitchBackdropAndWait Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SwitchBackdropAndWait node) {
        return new SwitchBackdropAndWait(cloneNode(node.getElementChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BackdropSwitchTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BackdropSwitchTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(BackdropSwitchTo node) {
        return new BackdropSwitchTo(cloneNode(node.getBackdrop()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link KeyPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node KeyPressed Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(KeyPressed node) {
        return new KeyPressed(cloneNode(node.getKey()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveSteps}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MoveSteps Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(MoveSteps node) {
        return new MoveSteps(cloneNode(node.getSteps()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeXBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeXBy Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeXBy node) {
        return new ChangeXBy(cloneNode(node.getNum()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeYBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeYBy Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeYBy node) {
        return new ChangeYBy(cloneNode(node.getNum()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetXTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetXTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetXTo node) {
        return new SetXTo(cloneNode(node.getNum()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetYTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetYTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetYTo node) {
        return new SetYTo(cloneNode(node.getNum()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GoToPos}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToPos Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GoToPos node) {
        return new GoToPos(cloneNode(node.getPosition()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GoToPosXY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToPos Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GoToPosXY node) {
        return new GoToPosXY(cloneNode(node.getX()), cloneNode(node.getY()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Qualified}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Qualified Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Qualified node) {
        return new Qualified(cloneNode(node.getFirst()), cloneNode(node.getSecond()));
    }

    /**
     * Default implementation of visit method for {@link SetPenColorToColorStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorToColorStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetPenColorToColorStmt node) {
        return new SetPenColorToColorStmt(cloneNode(node.getColorExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ColorTouchingColor}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ColorTouchingColor  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ColorTouchingColor node) {
        return new ColorTouchingColor(cloneNode(node.getOperand1()),
                cloneNode(node.getOperand2()),
                cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Touching}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Touching  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Touching node) {
        return new Touching(cloneNode(node.getTouchable()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PenStampStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenStampStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PenStampStmt node) {
        return new PenStampStmt(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangePenColorParamBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangePenColorParamBy  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangePenColorParamBy node) {
        return new ChangePenColorParamBy(cloneNode(node.getValue()), cloneNode(node.getParam()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetPenColorParamTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenColorParamTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetPenColorParamTo node) {
        return new SetPenColorParamTo(cloneNode(node.getValue()), cloneNode(node.getParam()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetAttributeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetAttributeTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetAttributeTo node) {
        return new SetAttributeTo(cloneNode(node.getStringExpr()), cloneNode(node.getExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ActorDefinitionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorDefinitionList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ActorDefinitionList node) {
        return new ActorDefinitionList(cloneList(node.getDefinitions()));
    }

    /**
     * Default implementation of visit method for {@link ActorType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ActorType  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ActorType node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link Key}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Key  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Key node) {
        return new Key(cloneNode(node.getKey()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Message}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Message  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Message node) {
        return new Message(cloneNode(node.getMessage()));
    }

    /**
     * Default implementation of visit method for {@link Program}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Program  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Program node) {
        SymbolTable copyTable = new SymbolTable();
        return new Program(cloneNode(node.getIdent()),
                cloneNode(node.getActorDefinitionList()),
                new SymbolTable(node.getSymbolTable()), // TODO: Not a deep copy
                new ProcedureDefinitionNameMapping(node.getProcedureMapping()), // TODO: Not a deep copy
                cloneNode(node.getProgramMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetStmtList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetStmtList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetStmtList node) {
        return new SetStmtList(cloneList(node.getStmts()));
    }

    /**
     * Default implementation of visit method for {@link Next}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Next  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Next node) {
        return new Next((BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link Prev}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Prev  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Prev node) {
        return new Prev((BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link Random}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Random  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Random node) {
        return new Random((BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link WithExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node WithExpr  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(WithExpr node) {
        return new WithExpr(cloneNode(node.getExpression()), (BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link GreenFlag}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GreenFlag  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GreenFlag node) {
        return new GreenFlag((BlockMetadata) cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link AttributeAboveValue}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node VariableAboveValue  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AttributeAboveValue node) {
        return new AttributeAboveValue(cloneNode(node.getAttribute()),
                cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedExpression}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedExpression  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UnspecifiedExpression node) {
        return new UnspecifiedExpression();
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedId}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedId  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UnspecifiedId node) {
        return new UnspecifiedId();
    }

    /**
     * Default implementation of visit method for {@link StringContains}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionContains  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StringContains node) {
        return new StringContains(cloneNode(node.getContaining()), cloneNode(node.getContained()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IsKeyPressed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IsKeyPressed  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(IsKeyPressed node) {
        return new IsKeyPressed(cloneNode(node.getKey()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IsMouseDown}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IsMouseDown  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(IsMouseDown node) {
        return new IsMouseDown(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedBoolExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedBoolExpr  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UnspecifiedBoolExpr node) {
        return new UnspecifiedBoolExpr();
    }

    /**
     * Default implementation of visit method for {@link FromNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FromNumber  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(FromNumber node) {
        return new FromNumber(cloneNode(node.getValue()));
    }

    /**
     * Default implementation of visit method for {@link Add}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Add  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Add node) {
        return new Add(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsNumber  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AsNumber node) {
        return new AsNumber(cloneNode(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsNumber  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Current node) {
        return new Current(cloneNode(node.getTimeComp()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DaysSince2000}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DaysSince2000  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DaysSince2000 node) {
        return new DaysSince2000(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DistanceTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DistanceTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DistanceTo node) {
        return new DistanceTo(cloneNode(node.getPosition()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Div}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Div  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Div node) {
        return new Div(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IndexOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IndexOf  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(IndexOf node) {
        return new IndexOf(cloneNode(node.getExpr()), cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LengthOfString}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LengthOfString  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(LengthOfString node) {
        return new LengthOfString(cloneNode(node.getStringExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LengthOfVar}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LengthOfVar  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(LengthOfVar node) {
        return new LengthOfVar(cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Loudness}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Loudness  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Loudness node) {
        return new Loudness(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Minus}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Minus  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Minus node) {
        return new Minus(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Mod}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Mod  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Mod node) {
        return new Mod(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MouseX}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MouseX  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(MouseX node) {
        return new MouseX(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MouseY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MouseY  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(MouseY node) {
        return new MouseY(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Mult}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Mult  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Mult node) {
        return new Mult(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NumFunct}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumFunct  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NumFunct node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link NumFunctOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumFunctOf  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NumFunctOf node) {
        return new NumFunctOf(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PickRandom}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PickRandom  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PickRandom node) {
        return new PickRandom(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Round}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Round  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Round node) {
        return new Round(cloneNode(node.getOperand1()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Timer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Timer  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Timer node) {
        return new Timer(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedNumExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedNumExpr  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UnspecifiedNumExpr node) {
        return new UnspecifiedNumExpr();
    }

    /**
     * Default implementation of visit method for {@link AsString}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsString  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AsString node) {
        return new AsString(cloneNode(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link AttributeOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AttributeOf  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AttributeOf node) {
        return new AttributeOf(cloneNode(node.getAttribute()), cloneNode(node.getElementChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ItemOfVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ItemOfVariable  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ItemOfVariable node) {
        return new ItemOfVariable(cloneNode(node.getNum()), cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Join}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Join  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Join node) {
        return new Join(cloneNode(node.getOperand1()), cloneNode(node.getOperand2()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LetterOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LetterOf  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(LetterOf node) {
        return new LetterOf(cloneNode(node.getNum()), cloneNode(node.getStringExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedStringExpr}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedStringExpr  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UnspecifiedStringExpr node) { //FIXME visit StringExpr or Unspecified?
        return new UnspecifiedStringExpr();
    }

    /**
     * Default implementation of visit method for {@link Username}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Username  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Username node) {
        return new Username(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MousePos}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MousePos  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(MousePos node) {
        return new MousePos(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FromExpression}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FromExpression  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(FromExpression node) {
        return new FromExpression(cloneNode(node.getStringExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RandomPos}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RandomPos  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(RandomPos node) {
        return new RandomPos(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinitionList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ProcedureDefinitionList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ProcedureDefinitionList node) {
        return new ProcedureDefinitionList(cloneList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link ExpressionStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExpressionStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ExpressionStmt node) {
        return new ExpressionStmt(cloneNode(node.getExpression()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node UnspecifiedStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(UnspecifiedStmt node) {
        return new UnspecifiedStmt();
    }

    /**
     * Default implementation of visit method for {@link AskAndWait}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AskAndWait  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AskAndWait node) {
        return new AskAndWait(cloneNode(node.getQuestion()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ClearGraphicEffects}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ClearGraphicEffects  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ClearGraphicEffects node) {
        return new ClearGraphicEffects(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ClearSoundEffects}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ClearSoundEffects  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ClearSoundEffects node) {
        return new ClearSoundEffects(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PlaySoundUntilDone}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PlaySoundUntilDone  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PlaySoundUntilDone node) {
        return new PlaySoundUntilDone(cloneNode(node.getElementChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StartSound}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StartSound  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StartSound node) {
        return new StartSound(cloneNode(node.getElementChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopAllSounds}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopAllSounds  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StopAllSounds node) {
        return new StopAllSounds(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeVariableBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeVariableBy  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeVariableBy node) {
        return new ChangeVariableBy(cloneNode(node.getIdentifier()), cloneNode(node.getExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ResetTimer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ResetTimer  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ResetTimer node) {
        return new ResetTimer(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetVariableTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetVariableTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetVariableTo node) {
        return new SetVariableTo(cloneNode(node.getIdentifier()), cloneNode(node.getExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopOtherScriptsInSprite}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopOtherScriptsInSprite  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StopOtherScriptsInSprite node) {
        return new StopOtherScriptsInSprite(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link WaitSeconds}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node WaitSeconds  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(WaitSeconds node) {
        return new WaitSeconds(cloneNode(node.getSeconds()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationAttributeAsTypeStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationAttributeAsTypeStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeclarationAttributeAsTypeStmt node) {
        return new DeclarationAttributeAsTypeStmt(cloneNode(node.getStringExpr()), cloneNode(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationAttributeOfIdentAsTypeStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationAttributeOfIdentAsTypeStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeclarationAttributeOfIdentAsTypeStmt node) {
        return new DeclarationAttributeOfIdentAsTypeStmt(cloneNode(node.getStringExpr()), cloneNode(node.getIdent()), cloneNode(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationIdentAsTypeStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationIdentAsTypeStmt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeclarationIdentAsTypeStmt node) {
        return new DeclarationIdentAsTypeStmt(cloneNode(node.getIdent()), cloneNode(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationStmtList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationStmtList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeclarationStmtList node) {
        return new DeclarationStmtList(cloneList(node.getDeclarationStmtList()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationBroadcastStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeclarationStmtList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeclarationBroadcastStmt node) {
        return new DeclarationBroadcastStmt(cloneNode(node.getIdent()), cloneNode(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link AddTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AddTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AddTo node) {
        return new AddTo(cloneNode(node.getString()), cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeleteAllOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeleteAllOf  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeleteAllOf node) {
        return new DeleteAllOf(cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeleteOf}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DeleteOf  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DeleteOf node) {
        return new DeleteOf(cloneNode(node.getNum()), cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link InsertAt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node InsertAt  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(InsertAt node) {
        return new InsertAt(cloneNode(node.getString()), cloneNode(node.getIndex()), cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ReplaceItem}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ReplaceItem  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ReplaceItem node) {
        return new ReplaceItem(cloneNode(node.getString()), cloneNode(node.getIndex()), cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeLayerBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeLayerBy  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeLayerBy node) {
        return new ChangeLayerBy(cloneNode(node.getNum()), cloneNode(node.getForwardBackwardChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeSizeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeSizeBy  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeSizeBy node) {
        return new ChangeSizeBy(cloneNode(node.getNum()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GoToLayer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GoToLayer  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GoToLayer node) {
        return new GoToLayer(cloneNode(node.getLayerChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Hide}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Hide  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Hide node) {
        return new Hide(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link HideVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node HideVariable  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(HideVariable node) {
        return new HideVariable(cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link HideList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node HideList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(HideList node) {
        return new HideList(cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShowList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShowList  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ShowList node) {
        return new ShowList(cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Say}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Say  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Say node) {
        return new Say(cloneNode(node.getString()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SayForSecs}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SayForSecs  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SayForSecs node) {
        return new SayForSecs(cloneNode(node.getString()), cloneNode(node.getSecs()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetSizeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetSizeTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetSizeTo node) {
        return new SetSizeTo(cloneNode(node.getPercent()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Show}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Show  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Show node) {
        return new Show(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShowVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ShowVariable  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ShowVariable node) {
        return new ShowVariable(cloneNode(node.getIdentifier()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SwitchCostumeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SwitchCostumeTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SwitchCostumeTo node) {
        return new SwitchCostumeTo(cloneNode(node.getCostumeChoice()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NextCostume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NextCostume  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NextCostume node) {
        return new NextCostume(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Think}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Think  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Think node) {
        return new Think(cloneNode(node.getThought()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ThinkForSecs}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ThinkForSecs  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ThinkForSecs node) {
        return new ThinkForSecs(cloneNode(node.getThought()), cloneNode(node.getSecs()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GlideSecsTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GlideSecsTo  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GlideSecsTo node) {
        return new GlideSecsTo(cloneNode(node.getSecs()), cloneNode(node.getPosition()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GlideSecsToXY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GlideSecsToXY  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GlideSecsToXY node) {
        return new GlideSecsToXY(cloneNode(node.getSecs()), cloneNode(node.getX()), cloneNode(node.getY()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfOnEdgeBounce}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node IfOnEdgeBounce  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(IfOnEdgeBounce node) {
        return new IfOnEdgeBounce(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PointInDirection}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PointInDirection  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PointInDirection node) {
        return new PointInDirection(cloneNode(node.getDirection()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PointTowards}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PointTowards  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PointTowards node) {
        return new PointTowards(cloneNode(node.getPosition()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnLeft}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnLeft  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(TurnLeft node) {
        return new TurnLeft(cloneNode(node.getDegrees()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnRight}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TurnRight  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(TurnRight node) {
        return new TurnRight(cloneNode(node.getDegrees()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopThisScript}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopThisScript  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StopThisScript node) {
        return new StopThisScript(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TimeComp}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TimeComp  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(TimeComp node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link Edge}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Edge  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Edge node) {
        return new Edge(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MousePointer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node MousePointer  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(MousePointer node) {
        return new MousePointer(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SpriteTouchable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteTouchable  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SpriteTouchable node) {
        return new SpriteTouchable(cloneNode(node.getStringExpr()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BooleanType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node BooleanType  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(BooleanType node) {

        Preconditions.checkArgument(node.getChildren().isEmpty()); // This seems to be the assumption?
        return new BooleanType();
    }

    /**
     * Default implementation of visit method for {@link ListType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ListType  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ListType node) {
        return new ListType();
    }

    /**
     * Default implementation of visit method for {@link NumberType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NumberType  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NumberType node) {
        return new NumberType();
    }

    /**
     * Default implementation of visit method for {@link StringType}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StringType  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StringType node) {
        return new StringType();
    }

    /**
     * Default implementation of visit method for {@link AsBool}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsBool Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AsBool node) {
        return new AsBool(cloneNode(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link AsTouchable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AsTouchable Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AsTouchable node) {
        return new AsTouchable(cloneNode(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link ScriptList}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ScriptList Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ScriptList node) {
        return new ScriptList(cloneList(node.getScriptList()));
    }

    /**
     * Default implementation of visit method for {@link SpriteClicked}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SpriteClicked Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SpriteClicked node) {
        return new SpriteClicked(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StageClicked}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StageClicked Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StageClicked node) {
        return new StageClicked(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Costume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Costume Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Costume node) {
        return new Costume(cloneNode(node.getType()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Backdrop}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Backdrop Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Backdrop node) {
        return new Backdrop(cloneNode(node.getType()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Direction}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Direction Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Direction node) {
        return new Direction(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PositionX}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PositionX Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PositionX node) {
        return new PositionX(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PositionY}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PositionY Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PositionY node) {
        return new PositionY(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Size}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Size Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Size node) {
        return new Size(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Volume}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Volume Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Volume node) {
        return new Volume(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Answer}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node Answer Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(Answer node) {
        return new Answer(cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NameNum}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node NameNum Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(NameNum node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link FixedAttribute}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node FixedAttribute Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(FixedAttribute node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link AttributeFromFixed}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AttributeFromFixed Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AttributeFromFixed node) {
        return new AttributeFromFixed(cloneNode(node.getAttribute()));
    }

    /**
     * Default implementation of visit method for {@link AttributeFromVariable}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node AttributeFromVariable Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(AttributeFromVariable node) {
        return new AttributeFromVariable(cloneNode(node.getVariable()));
    }

    /**
     * Default implementation of visit method for {@link LayerChoice}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LayerChoice Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(LayerChoice node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link SetPenSizeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetPenSizeTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetPenSizeTo node) {
        return new SetPenSizeTo(cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangePenSizeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangePenSizeBy Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangePenSizeBy node) {
        return new ChangePenSizeBy(cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetGraphicEffectTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetGraphicEffectTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetGraphicEffectTo node) {
        return new SetGraphicEffectTo(cloneNode(node.getEffect()), cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeGraphicEffectBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeGraphicEffectBy Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeGraphicEffectBy node) {
        return new ChangeGraphicEffectBy(cloneNode(node.getEffect()), cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GraphicEffect}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node GraphicEffect Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(GraphicEffect node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link SoundEffect}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SoundEffect Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SoundEffect node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link SetSoundEffectTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetSoundEffectTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetSoundEffectTo node) {
        return new SetSoundEffectTo(cloneNode(node.getEffect()), cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeSoundEffectBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeSoundEffectBy Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeSoundEffectBy node) {
        return new ChangeSoundEffectBy(cloneNode(node.getEffect()), cloneNode(node.getValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetVolumeTo}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetVolumeTo Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetVolumeTo node) {
        return new SetVolumeTo(cloneNode(node.getVolumeValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeVolumeBy}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ChangeVolumeBy Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ChangeVolumeBy node) {
        return new ChangeVolumeBy(cloneNode(node.getVolumeValue()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DragMode}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node DragMode Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(DragMode node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link RotationStyle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node RotationStyle Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(RotationStyle node) {
        return node; // enum
    }

    /**
     * Default implementation of visit method for {@link SetRotationStyle}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetRotationStyle Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetRotationStyle node) {
        return new SetRotationStyle(cloneNode(node.getRotation()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetDragMode}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node SetDragMode Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(SetDragMode node) {
        return new SetDragMode(cloneNode(node.getDrag()), cloneNode(node.getMetadata()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(SpriteTouchingColor node) {
        return new SpriteTouchingColor(cloneNode(node.getColor()), cloneNode(node.getMetadata()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(Variable node) {
        return new Variable(cloneNode(node.getName()), cloneNode(node.getMetadata()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ScratchList node) {
        return new ScratchList(cloneNode(node.getName()), cloneNode(node.getMetadata()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(Parameter node) {
        return new Parameter(cloneNode(node.getName()), cloneNode(node.getMetadata()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ListContains node) {
        return new ListContains(cloneNode(node.getIdentifier()), cloneNode(node.getElement()), cloneNode(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link EventAttribute}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node EventAttribute  Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(EventAttribute node) {
        return node; // don't need to copy enum
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(VariableMetadata node) {
        return new VariableMetadata(node.getVariableId(), node.getVariableName(), node.getValue());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(MetaMetadata node) {
        return new MetaMetadata(node.getSemver(), node.getVm(), node.getAgent());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ListMetadata node) {
        return new ListMetadata(node.getListId(), node.getListName(), new ArrayList<>(node.getValues()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ExtensionMetadata node) {
        return new ExtensionMetadata(new ArrayList<>(node.getExtensionNames()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(CommentMetadata node) {
        return new CommentMetadata(node.getCommentId(), node.getBlockId(), node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.isMinimized(), node.getText());
    }


    /**
     * Default implementation of visit method for {@link PenWithParamMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node PenWithParamMetadata Node of which the children will
     *             be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(PenWithParamMetadata node) {
        return new PenWithParamMetadata(cloneNode(node.getPenBlockMetadata()), cloneNode(node.getParamMetadata()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ProgramMetadata node) {
        return new ProgramMetadata(cloneNode(node.getMonitor()), cloneNode(node.getExtension()), cloneNode(node.getMeta()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(BroadcastMetadata node) {
        return new BroadcastMetadata(node.getBroadcastID(), node.getBroadcastName());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ImageMetadata node) {
        return new ImageMetadata(node.getAssetId(), node.getUniqueName(), node.getMd5ext(), node.getDataFormat(),
                node.getBitmapResolution(), node.getRotationCenterX(), node.getRotationCenterY());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(SoundMetadata node) {
        return new SoundMetadata(node.getAssetId(), node.getUniqueName(), node.getMd5ext(), node.getDataFormat(),
                node.getRate(), node.getSampleCount());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(MonitorSliderMetadata node) {
        return new MonitorSliderMetadata(node.getId(), node.getMode(), node.getOpcode(),
                cloneNode(node.getParamsMetadata()), node.getSpriteName(), node.getWidth(), node.getHeight(),
                node.getX(), node.getY(), node.isVisible(), node.getValue(), node.getSliderMin(), node.getSliderMax(), node.isDiscrete());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(MonitorListMetadata node) {
        return new MonitorListMetadata(node.getId(), node.getMode(), node.getOpcode(),
                cloneNode(node.getParamsMetadata()), node.getSpriteName(), node.getWidth(),
                node.getHeight(), node.getX(), node.getY(), node.isVisible(), new ArrayList<>(node.getValues()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(MonitorParamMetadata node) {
        return new MonitorParamMetadata(node.getInputName(), node.getInputValue());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ReferenceInputMetadata node) {
        return new ReferenceInputMetadata(node.getInputName(), node.getReference());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(TypeInputMetadata node) {
        return new TypeInputMetadata(node.getInputName(), node.getType(), node.getValue());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(DataInputMetadata node) {
        return new DataInputMetadata(node.getInputName(), node.getDataType(), node.getDataName(), node.getDataReference());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(DataBlockMetadata node) {
        return new DataBlockMetadata(node.getBlockId(), node.getDataType(), node.getDataName(), node.getDataReference(), node.getX(), node.getY());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(NonDataBlockMetadata node) {
        return new NonDataBlockMetadata(node.getCommentId(), node.getBlockId(), node.getOpcode(), node.getNext(), node.getParent(),
                cloneNode(node.getInputMetadata()), cloneNode(node.getFields()),
                node.isTopLevel(), node.isShadow(), cloneNode(node.getMutation()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(TopNonDataBlockMetadata node) {
        return new TopNonDataBlockMetadata(node.getCommentId(), node.getBlockId(), node.getOpcode(), node.getNext(), node.getParent(),
                cloneNode(node.getInputMetadata()), cloneNode(node.getFields()),
                node.isTopLevel(), node.isShadow(), cloneNode(node.getMutation()),
                node.getXPos(), node.getYPos());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(FieldsMetadata node) {
        return new FieldsMetadata(node.getFieldsName(), node.getFieldsValue(), node.getFieldsReference());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(NoMutationMetadata node) {
        return new NoMutationMetadata();
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(CallMutationMetadata node) {
        return new CallMutationMetadata(node);
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(PrototypeMutationMetadata node) {
        return new PrototypeMutationMetadata(node.getTagName(), new ArrayList<>(node.getChild()),
                node.getProcCode(), new ArrayList<>(node.getArgumentIds()),
                node.isWarp(), node.getArgumentNames(), node.getArgumentDefaults());
    }

    /**
     * Default implementation of visit method for {@link StopMutationMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node StopMutationMetadata Node of which the children will
     *             be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(StopMutationMetadata node) {
        return new StopMutationMetadata(node.getTagName(), new ArrayList<>(node.getChild()), node.isHasNext());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(StageMetadata node) {
        return new StageMetadata(cloneNode(node.getCommentsMetadata()), cloneNode(node.getVariables()),
                cloneNode(node.getLists()),
                cloneNode(node.getBroadcasts()),
                node.getCurrentCostume(),
                cloneNode(node.getCostumes()),
                cloneNode(node.getSounds()),
                node.getVolume(), node.getLayerOrder(), node.getTempo(), node.getVideoTransparency(),
                node.getVideoState(), node.getTextToSpeechLanguage());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(SpriteMetadata node) {
        return new SpriteMetadata(cloneNode(node.getCommentsMetadata()), cloneNode(node.getVariables()),
                cloneNode(node.getLists()),
                cloneNode(node.getBroadcasts()),
                node.getCurrentCostume(),
                cloneNode(node.getCostumes()),
                cloneNode(node.getSounds()),
                node.getVolume(), node.getLayerOrder(), node.isVisible(), node.getX(), node.getY(), node.getSize(),
                node.getDirection(), node.isDraggable(), node.getRotationStyle());
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(BroadcastMetadataList node) {
        return new BroadcastMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(CommentMetadataList node) {
        return new CommentMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(FieldsMetadataList node) {
        return new FieldsMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ImageMetadataList node) {
        return new ImageMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(InputMetadataList node) {
        return new InputMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ListMetadataList node) {
        return new ListMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(MonitorMetadataList node) {
        return new MonitorMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(MonitorParamMetadataList node) {
        return new MonitorParamMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(SoundMetadataList node) {
        return new SoundMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(VariableMetadataList node) {
        return new VariableMetadataList(cloneList(node.getList()));
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(NoMetadata node) {
        return new NoMetadata();
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(NoBlockMetadata node) {
        return new NoBlockMetadata();
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(ProcedureMetadata node) {
        return new ProcedureMetadata(cloneNode(node.getDefinition()), cloneNode(node.getPrototype()));
    }

    /**
     * Default implementation of visit method for {@link ForwardBackwardChoice}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ForwardBackwardChoice Node of which the children will be iterated
     * @return     the copy of the visited node
     */
    public ASTNode visit(ForwardBackwardChoice node) {
        return node; // enum
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
     * @return     the copy of the visited node
     */
    public ASTNode visit(CloneOfMetadata node) {
        return new CloneOfMetadata(cloneNode(node.getCloneBlockMetadata()), cloneNode(node.getCloneMenuMetadata()));
    }
}
