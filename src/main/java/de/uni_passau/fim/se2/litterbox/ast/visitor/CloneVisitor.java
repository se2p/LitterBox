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
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.IRMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.LearnWithTime;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendIR;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ir.SendLearnResult;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetAxis;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.reset.ResetTimer2;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.ExprDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.ExprInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.ExprNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.SetVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.Speak;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.ViewerLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
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
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class CloneVisitor {

    /**
     * Apply the copying or transformation to an ASTNode
     *
     * @param node the original node
     * @return the transformed node
     */
    @SuppressWarnings("unchecked")
    public <T extends ASTNode> T apply(T node) {
        if (node == null) {
            return null;
        }
        ASTNode r = node.accept(this);
        if (r == null) {
            return null;
        }
        return (T) r;
    }

    /**
     * Apply the copying or transformation to a list of ASTNodes
     *
     * @param theList the original list of nodes
     * @return a copy of the list, with transformed content
     */
    public <T extends ASTNode> List<T> applyList(List<T> theList) {
        List<T> result = new ArrayList<>();
        for (T node : theList) {
            result.add(apply(node));
        }
        return result;
    }

    /**
     * Default implementation of visit method for ActorDefinition.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ActorDefinition which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ActorDefinition node) {
        return new ActorDefinition(apply(node.getActorType()), apply(node.getIdent()), apply(node.getDecls()), apply(node.getSetStmtList()), apply(node.getProcedureDefinitionList()), apply(node.getScripts()), apply(node.getActorMetadata()));
    }

    /**
     * Default implementation of visit method for PenDownStmt.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PenDownStmt which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PenDownStmt node) {
        return new PenDownStmt(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PenUpStmt which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PenUpStmt node) {
        return new PenUpStmt(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for PenUpStmt.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PenUpStmt which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PenClearStmt node) {
        return new PenClearStmt(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Equals}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Equals Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Equals node) {
        return new Equals(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LessThan}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LessThan Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LessThan node) {
        return new LessThan(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BiggerThan}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BiggerThan Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BiggerThan node) {
        return new BiggerThan(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ProcedureDefinition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ProcedureDefinition node) {
        return new ProcedureDefinition(apply(node.getIdent()), apply(node.getParameterDefinitionList()), apply(node.getStmtList()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StrId}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StrId which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StrId node) {
        return new StrId(node.getName());
    }

    /**
     * Default implementation of visit method for {@link Script}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Script which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Script node) {
        return new Script(apply(node.getEvent()), apply(node.getStmtList()));
    }

    /**
     * Default implementation of visit method for {@link CreateCloneOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node CreateCloneOf Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(CreateCloneOf node) {
        return new CreateCloneOf(apply(node.getStringExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StartedAsClone}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StartedAsClone Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StartedAsClone node) {
        return new StartedAsClone(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfElseStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IfElseStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IfElseStmt node) {
        return new IfElseStmt(apply(node.getBoolExpr()), apply(node.getThenStmts()), apply(node.getElseStmts()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfThenStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IfThenStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IfThenStmt node) {
        return new IfThenStmt(apply(node.getBoolExpr()), apply(node.getThenStmts()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link WaitUntil}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node WaitUntil Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(WaitUntil node) {
        return new WaitUntil(apply(node.getUntil()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UntilStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UntilStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UntilStmt node) {
        return new UntilStmt(apply(node.getBoolExpr()), apply(node.getStmtList()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Not}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Not Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Not node) {
        return new Not(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link And}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node And Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(And node) {
        return new And(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Or}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node And Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Or node) {
        return new Or(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Broadcast}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Broadcast Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Broadcast node) {
        return new Broadcast(apply(node.getMessage()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BroadcastAndWait}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BroadcastAndWait Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BroadcastAndWait node) {
        return new BroadcastAndWait(apply(node.getMessage()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ReceptionOfMessage}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ReceptionOfMessage Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ReceptionOfMessage node) {
        return new ReceptionOfMessage(apply(node.getMsg()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RepeatForeverStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RepeatForeverStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RepeatForeverStmt node) {
        return new RepeatForeverStmt(apply(node.getStmtList()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link CallStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node CallStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(CallStmt node) {
        return new CallStmt(apply(node.getIdent()), apply(node.getExpressions()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeleteClone}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeleteClone Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeleteClone node) {
        return new DeleteClone(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopAll}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StopAll Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StopAll node) {
        return new StopAll(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StmtList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StmtList Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StmtList node) {
        return new StmtList(applyList(node.getStmts()));
    }

    /**
     * Default implementation of visit method for {@link RepeatTimesStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RepeatTimesStmt Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RepeatTimesStmt node) {
        return new RepeatTimesStmt(apply(node.getTimes()), apply(node.getStmtList()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StringLiteral}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StringLiteral Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StringLiteral node) {
        return new StringLiteral(node.getText());
    }

    /**
     * Default implementation of visit method for {@link BoolLiteral}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BoolLiteral Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BoolLiteral node) {
        return new BoolLiteral(node.getValue());
    }

    /**
     * Default implementation of visit method for {@link NumberLiteral}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NumberLiteral Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NumberLiteral node) {
        return new NumberLiteral(node.getValue());
    }

    /**
     * Default implementation of visit method for {@link ColorLiteral}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ColorLiteral Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ColorLiteral node) {
        return new ColorLiteral(node.getRed(), node.getGreen(), node.getBlue());
    }

    /**
     * Default implementation of visit method for {@link Never}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Never Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Never node) {
        return new Never();
    }

    /**
     * Default implementation of visit method for {@link ParameterDefinitionList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ParameterDefinitionList Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ParameterDefinitionList node) {
        return new ParameterDefinitionList(applyList(node.getParameterDefinitions()));
    }

    /**
     * Default implementation of visit method for {@link ParameterDefinition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ParameterDefiniton Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ParameterDefinition node) {
        return new ParameterDefinition(apply(node.getIdent()), apply(node.getType()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ExpressionList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExpressionList Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ExpressionList node) {
        return new ExpressionList(applyList(node.getExpressions()));
    }

    /**
     * Default implementation of visit method for {@link SwitchBackdrop}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SwitchBackdrop Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SwitchBackdrop node) {
        return new SwitchBackdrop(apply(node.getElementChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NextBackdrop}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NextBackdrop Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NextBackdrop node) {
        return new NextBackdrop(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SwitchBackdropAndWait}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SwitchBackdropAndWait Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SwitchBackdropAndWait node) {
        return new SwitchBackdropAndWait(apply(node.getElementChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BackdropSwitchTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BackdropSwitchTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BackdropSwitchTo node) {
        return new BackdropSwitchTo(apply(node.getBackdrop()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link KeyPressed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node KeyPressed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(KeyPressed node) {
        return new KeyPressed(apply(node.getKey()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveSteps}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MoveSteps Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MoveSteps node) {
        return new MoveSteps(apply(node.getSteps()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeXBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeXBy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeXBy node) {
        return new ChangeXBy(apply(node.getNum()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeYBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeYBy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeYBy node) {
        return new ChangeYBy(apply(node.getNum()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetXTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetXTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetXTo node) {
        return new SetXTo(apply(node.getNum()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetYTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetYTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetYTo node) {
        return new SetYTo(apply(node.getNum()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GoToPos}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GoToPos Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GoToPos node) {
        return new GoToPos(apply(node.getPosition()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GoToPosXY}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GoToPos Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GoToPosXY node) {
        return new GoToPosXY(apply(node.getX()), apply(node.getY()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Qualified}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Qualified Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Qualified node) {
        return new Qualified(apply(node.getFirst()), apply(node.getSecond()));
    }

    /**
     * Default implementation of visit method for {@link SetPenColorToColorStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetPenColorToColorStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetPenColorToColorStmt node) {
        return new SetPenColorToColorStmt(apply(node.getColorExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ColorTouchingColor}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ColorTouchingColor  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ColorTouchingColor node) {
        return new ColorTouchingColor(apply(node.getOperand1()),
                apply(node.getOperand2()),
                apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Touching}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Touching  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Touching node) {
        return new Touching(apply(node.getTouchable()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PenStampStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PenStampStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PenStampStmt node) {
        return new PenStampStmt(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangePenColorParamBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangePenColorParamBy  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangePenColorParamBy node) {
        return new ChangePenColorParamBy(apply(node.getValue()), apply(node.getParam()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetPenColorParamTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetPenColorParamTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetPenColorParamTo node) {
        return new SetPenColorParamTo(apply(node.getValue()), apply(node.getParam()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetAttributeTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetAttributeTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetAttributeTo node) {
        return new SetAttributeTo(apply(node.getStringExpr()), apply(node.getExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ActorDefinitionList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ActorDefinitionList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ActorDefinitionList node) {
        return new ActorDefinitionList(applyList(node.getDefinitions()));
    }

    /**
     * Default implementation of visit method for {@link ActorType}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ActorType  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ActorType node) {
        return new ActorType(node);
    }

    /**
     * Default implementation of visit method for {@link Key}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Key  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Key node) {
        return new Key(apply(node.getKey()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Message}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Message  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Message node) {
        return new Message(apply(node.getMessage()));
    }

    /**
     * Default implementation of visit method for {@link Program}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Program  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Program node) {
        Program program = new Program(apply(node.getIdent()),
                apply(node.getActorDefinitionList()),
                new SymbolTable(node.getSymbolTable()), // TODO: Not a deep copy
                new ProcedureDefinitionNameMapping(node.getProcedureMapping()), // TODO: Not a deep copy
                apply(node.getProgramMetadata()));
        program.accept(new ParentVisitor());
        return program;
    }

    /**
     * Default implementation of visit method for {@link SetStmtList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetStmtList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetStmtList node) {
        return new SetStmtList(applyList(node.getStmts()));
    }

    /**
     * Default implementation of visit method for {@link Next}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Next  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Next node) {
        return new Next((BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link Prev}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Prev  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Prev node) {
        return new Prev((BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link Random}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Random  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Random node) {
        return new Random((BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link WithExpr}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node WithExpr  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(WithExpr node) {
        return new WithExpr(apply(node.getExpression()), (BlockMetadata) node.getMetadata().accept(this));
    }

    /**
     * Default implementation of visit method for {@link GreenFlag}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GreenFlag  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GreenFlag node) {
        return new GreenFlag(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link AttributeAboveValue}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node VariableAboveValue  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AttributeAboveValue node) {
        return new AttributeAboveValue(apply(node.getAttribute()),
                apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedExpression}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UnspecifiedExpression  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UnspecifiedExpression node) {
        return new UnspecifiedExpression();
    }

    /**
     * Default implementation of visit method for {@link StringContains}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExpressionContains  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StringContains node) {
        return new StringContains(apply(node.getContaining()), apply(node.getContained()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IsKeyPressed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IsKeyPressed  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IsKeyPressed node) {
        return new IsKeyPressed(apply(node.getKey()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IsMouseDown}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IsMouseDown  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IsMouseDown node) {
        return new IsMouseDown(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedBoolExpr}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UnspecifiedBoolExpr  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UnspecifiedBoolExpr node) {
        return new UnspecifiedBoolExpr();
    }

    /**
     * Default implementation of visit method for {@link FromNumber}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FromNumber  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FromNumber node) {
        return new FromNumber(apply(node.getValue()));
    }

    /**
     * Default implementation of visit method for {@link Add}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Add  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Add node) {
        return new Add(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AsNumber  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AsNumber node) {
        return new AsNumber(apply(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link AsNumber}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AsNumber  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Current node) {
        return new Current(apply(node.getTimeComp()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DaysSince2000}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DaysSince2000  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DaysSince2000 node) {
        return new DaysSince2000(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DistanceTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DistanceTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DistanceTo node) {
        return new DistanceTo(apply(node.getPosition()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Div}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Div  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Div node) {
        return new Div(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IndexOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IndexOf  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IndexOf node) {
        return new IndexOf(apply(node.getExpr()), apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LengthOfString}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LengthOfString  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LengthOfString node) {
        return new LengthOfString(apply(node.getStringExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LengthOfVar}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LengthOfVar  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LengthOfVar node) {
        return new LengthOfVar(apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Loudness}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Loudness  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Loudness node) {
        return new Loudness(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Minus}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Minus  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Minus node) {
        return new Minus(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Mod}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Mod  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Mod node) {
        return new Mod(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MouseX}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MouseX  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MouseX node) {
        return new MouseX(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MouseY}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MouseY  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MouseY node) {
        return new MouseY(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Mult}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Mult  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Mult node) {
        return new Mult(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NumFunct}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NumFunct  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NumFunct node) {
        return new NumFunct(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link NumFunctOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NumFunctOf  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NumFunctOf node) {
        return new NumFunctOf(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PickRandom}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PickRandom  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PickRandom node) {
        return new PickRandom(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Round}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Round  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Round node) {
        return new Round(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Timer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Timer  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Timer node) {
        return new Timer(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedNumExpr}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UnspecifiedNumExpr  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UnspecifiedNumExpr node) {
        return new UnspecifiedNumExpr();
    }

    /**
     * Default implementation of visit method for {@link AsString}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AsString  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AsString node) {
        return new AsString(apply(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link AttributeOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AttributeOf  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AttributeOf node) {
        return new AttributeOf(apply(node.getAttribute()), apply(node.getElementChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ItemOfVariable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ItemOfVariable  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ItemOfVariable node) {
        return new ItemOfVariable(apply(node.getNum()), apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Join}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Join  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Join node) {
        return new Join(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LetterOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LetterOf  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LetterOf node) {
        return new LetterOf(apply(node.getNum()), apply(node.getStringExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedStringExpr}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UnspecifiedStringExpr  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UnspecifiedStringExpr node) { //FIXME visit StringExpr or Unspecified?
        return new UnspecifiedStringExpr();
    }

    /**
     * Default implementation of visit method for {@link Username}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Username  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Username node) {
        return new Username(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MousePos}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MousePos  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MousePos node) {
        return new MousePos(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FromExpression}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FromExpression  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FromExpression node) {
        return new FromExpression(apply(node.getStringExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RandomPos}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RandomPos  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RandomPos node) {
        return new RandomPos(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ProcedureDefinitionList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ProcedureDefinitionList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ProcedureDefinitionList node) {
        return new ProcedureDefinitionList(applyList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link ExpressionStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExpressionStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ExpressionStmt node) {
        return new ExpressionStmt(apply(node.getExpression()));
    }

    /**
     * Default implementation of visit method for {@link UnspecifiedStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node UnspecifiedStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(UnspecifiedStmt node) {
        return new UnspecifiedStmt();
    }

    /**
     * Default implementation of visit method for {@link AskAndWait}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AskAndWait  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AskAndWait node) {
        return new AskAndWait(apply(node.getQuestion()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ClearGraphicEffects}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ClearGraphicEffects  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ClearGraphicEffects node) {
        return new ClearGraphicEffects(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ClearSoundEffects}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ClearSoundEffects  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ClearSoundEffects node) {
        return new ClearSoundEffects(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PlaySoundUntilDone}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PlaySoundUntilDone  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PlaySoundUntilDone node) {
        return new PlaySoundUntilDone(apply(node.getElementChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StartSound}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StartSound  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StartSound node) {
        return new StartSound(apply(node.getElementChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopAllSounds}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StopAllSounds  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StopAllSounds node) {
        return new StopAllSounds(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeVariableBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeVariableBy  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeVariableBy node) {
        return new ChangeVariableBy(apply(node.getIdentifier()), apply(node.getExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ResetTimer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ResetTimer  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ResetTimer node) {
        return new ResetTimer(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetVariableTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetVariableTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetVariableTo node) {
        return new SetVariableTo(apply(node.getIdentifier()), apply(node.getExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopOtherScriptsInSprite}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StopOtherScriptsInSprite  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StopOtherScriptsInSprite node) {
        return new StopOtherScriptsInSprite(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link WaitSeconds}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node WaitSeconds  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(WaitSeconds node) {
        return new WaitSeconds(apply(node.getSeconds()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationAttributeAsTypeStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeclarationAttributeAsTypeStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeclarationAttributeAsTypeStmt node) {
        return new DeclarationAttributeAsTypeStmt(apply(node.getStringExpr()), apply(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationAttributeOfIdentAsTypeStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeclarationAttributeOfIdentAsTypeStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeclarationAttributeOfIdentAsTypeStmt node) {
        return new DeclarationAttributeOfIdentAsTypeStmt(apply(node.getStringExpr()), apply(node.getIdent()), apply(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationIdentAsTypeStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeclarationIdentAsTypeStmt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeclarationIdentAsTypeStmt node) {
        return new DeclarationIdentAsTypeStmt(apply(node.getIdent()), apply(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationStmtList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeclarationStmtList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeclarationStmtList node) {
        return new DeclarationStmtList(applyList(node.getDeclarationStmts()));
    }

    /**
     * Default implementation of visit method for {@link DeclarationBroadcastStmt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeclarationStmtList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeclarationBroadcastStmt node) {
        return new DeclarationBroadcastStmt(apply(node.getIdent()), apply(node.getType()));
    }

    /**
     * Default implementation of visit method for {@link AddTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AddTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AddTo node) {
        return new AddTo(apply(node.getString()), apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeleteAllOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeleteAllOf  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeleteAllOf node) {
        return new DeleteAllOf(apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DeleteOf}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DeleteOf  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DeleteOf node) {
        return new DeleteOf(apply(node.getNum()), apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link InsertAt}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node InsertAt  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(InsertAt node) {
        return new InsertAt(apply(node.getString()), apply(node.getIndex()), apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ReplaceItem}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ReplaceItem  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ReplaceItem node) {
        return new ReplaceItem(apply(node.getString()), apply(node.getIndex()), apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeLayerBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeLayerBy  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeLayerBy node) {
        return new ChangeLayerBy(apply(node.getNum()), apply(node.getForwardBackwardChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeSizeBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeSizeBy  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeSizeBy node) {
        return new ChangeSizeBy(apply(node.getNum()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GoToLayer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GoToLayer  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GoToLayer node) {
        return new GoToLayer(apply(node.getLayerChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Hide}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Hide  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Hide node) {
        return new Hide(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link HideVariable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node HideVariable  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(HideVariable node) {
        return new HideVariable(apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link HideList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node HideList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(HideList node) {
        return new HideList(apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShowList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ShowList  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ShowList node) {
        return new ShowList(apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Say}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Say  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Say node) {
        return new Say(apply(node.getString()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SayForSecs}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SayForSecs  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SayForSecs node) {
        return new SayForSecs(apply(node.getString()), apply(node.getSecs()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetSizeTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetSizeTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetSizeTo node) {
        return new SetSizeTo(apply(node.getPercent()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Show}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Show  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Show node) {
        return new Show(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShowVariable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ShowVariable  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ShowVariable node) {
        return new ShowVariable(apply(node.getIdentifier()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SwitchCostumeTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SwitchCostumeTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SwitchCostumeTo node) {
        return new SwitchCostumeTo(apply(node.getCostumeChoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NextCostume}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NextCostume  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NextCostume node) {
        return new NextCostume(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Think}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Think  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Think node) {
        return new Think(apply(node.getThought()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ThinkForSecs}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ThinkForSecs  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ThinkForSecs node) {
        return new ThinkForSecs(apply(node.getThought()), apply(node.getSecs()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GlideSecsTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GlideSecsTo  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GlideSecsTo node) {
        return new GlideSecsTo(apply(node.getSecs()), apply(node.getPosition()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GlideSecsToXY}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GlideSecsToXY  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GlideSecsToXY node) {
        return new GlideSecsToXY(apply(node.getSecs()), apply(node.getX()), apply(node.getY()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IfOnEdgeBounce}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IfOnEdgeBounce  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IfOnEdgeBounce node) {
        return new IfOnEdgeBounce(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PointInDirection}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PointInDirection  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PointInDirection node) {
        return new PointInDirection(apply(node.getDirection()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PointTowards}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PointTowards  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PointTowards node) {
        return new PointTowards(apply(node.getPosition()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnLeft}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnLeft  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnLeft node) {
        return new TurnLeft(apply(node.getDegrees()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnRight}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnRight  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnRight node) {
        return new TurnRight(apply(node.getDegrees()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopThisScript}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StopThisScript  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StopThisScript node) {
        return new StopThisScript(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TimeComp}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TimeComp  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TimeComp node) {
        return new TimeComp(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link Edge}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Edge  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Edge node) {
        return new Edge(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MousePointer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MousePointer  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MousePointer node) {
        return new MousePointer(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SpriteTouchable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SpriteTouchable  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SpriteTouchable node) {
        return new SpriteTouchable(apply(node.getStringExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BooleanType}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BooleanType  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BooleanType node) {

        Preconditions.checkArgument(node.getChildren().isEmpty()); // This seems to be the assumption?
        return new BooleanType();
    }

    /**
     * Default implementation of visit method for {@link ListType}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ListType  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ListType node) {
        return new ListType();
    }

    /**
     * Default implementation of visit method for {@link NumberType}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NumberType  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NumberType node) {
        return new NumberType();
    }

    /**
     * Default implementation of visit method for {@link StringType}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StringType  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StringType node) {
        return new StringType();
    }

    /**
     * Default implementation of visit method for {@link AsBool}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AsBool Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AsBool node) {
        return new AsBool(apply(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link AsTouchable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AsTouchable Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AsTouchable node) {
        return new AsTouchable(apply(node.getOperand1()));
    }

    /**
     * Default implementation of visit method for {@link ScriptList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ScriptList Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ScriptList node) {
        return new ScriptList(applyList(node.getScriptList()));
    }

    /**
     * Default implementation of visit method for {@link SpriteClicked}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SpriteClicked Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SpriteClicked node) {
        return new SpriteClicked(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StageClicked}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StageClicked Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StageClicked node) {
        return new StageClicked(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Costume}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Costume Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Costume node) {
        return new Costume(apply(node.getType()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Backdrop}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Backdrop Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Backdrop node) {
        return new Backdrop(apply(node.getType()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Direction}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Direction Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Direction node) {
        return new Direction(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PositionX}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PositionX Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PositionX node) {
        return new PositionX(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PositionY}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PositionY Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PositionY node) {
        return new PositionY(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Size}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Size Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Size node) {
        return new Size(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Volume}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Volume Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Volume node) {
        return new Volume(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Answer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Answer Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Answer node) {
        return new Answer(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link NameNum}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NameNum Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(NameNum node) {
        return new NameNum(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link FixedAttribute}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FixedAttribute Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FixedAttribute node) {
        return new FixedAttribute(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link AttributeFromFixed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AttributeFromFixed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AttributeFromFixed node) {
        return new AttributeFromFixed(apply(node.getAttribute()));
    }

    /**
     * Default implementation of visit method for {@link AttributeFromVariable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AttributeFromVariable Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AttributeFromVariable node) {
        return new AttributeFromVariable(apply(node.getVariable()));
    }

    /**
     * Default implementation of visit method for {@link LayerChoice}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LayerChoice Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LayerChoice node) {
        return new LayerChoice(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link SetPenSizeTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetPenSizeTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetPenSizeTo node) {
        return new SetPenSizeTo(apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangePenSizeBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangePenSizeBy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangePenSizeBy node) {
        return new ChangePenSizeBy(apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetGraphicEffectTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetGraphicEffectTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetGraphicEffectTo node) {
        return new SetGraphicEffectTo(apply(node.getEffect()), apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeGraphicEffectBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeGraphicEffectBy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeGraphicEffectBy node) {
        return new ChangeGraphicEffectBy(apply(node.getEffect()), apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GraphicEffect}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GraphicEffect Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GraphicEffect node) {
        return new GraphicEffect(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link SoundEffect}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SoundEffect Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SoundEffect node) {
        return new SoundEffect(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link SetSoundEffectTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetSoundEffectTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetSoundEffectTo node) {
        return new SetSoundEffectTo(apply(node.getEffect()), apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeSoundEffectBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeSoundEffectBy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeSoundEffectBy node) {
        return new ChangeSoundEffectBy(apply(node.getEffect()), apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetVolumeTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetVolumeTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetVolumeTo node) {
        return new SetVolumeTo(apply(node.getVolumeValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ChangeVolumeBy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeVolumeBy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeVolumeBy node) {
        return new ChangeVolumeBy(apply(node.getVolumeValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DragMode}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DragMode Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DragMode node) {
        return new DragMode(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link RotationStyle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RotationStyle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RotationStyle node) {
        return new RotationStyle(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link SetRotationStyle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetRotationStyle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetRotationStyle node) {
        return new SetRotationStyle(apply(node.getRotation()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetDragMode}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetDragMode Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetDragMode node) {
        return new SetDragMode(apply(node.getDrag()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SpriteTouchingColor}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SpriteTouchingColor Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(SpriteTouchingColor node) {
        return new SpriteTouchingColor(apply(node.getColor()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Variable}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Variable Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(Variable node) {
        return new Variable(apply(node.getName()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ScratchList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ScratchList Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ScratchList node) {
        return new ScratchList(apply(node.getName()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Parameter}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Parameter Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(Parameter node) {
        return new Parameter(apply(node.getName()), apply(node.getType()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ListContains}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ListContains Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ListContains node) {
        return new ListContains(apply(node.getIdentifier()), apply(node.getElement()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link EventAttribute}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node EventAttribute  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(EventAttribute node) {
        return new EventAttribute(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link MetaMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MetaMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(MetaMetadata node) {
        return new MetaMetadata(node.getSemver(), node.getVm(), node.getAgent());
    }

    /**
     * Default implementation of visit method for {@link ExtensionMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExtensionMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ExtensionMetadata node) {
        return new ExtensionMetadata(new ArrayList<>(node.getExtensionNames()));
    }

    /**
     * Default implementation of visit method for {@link CommentMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node CommentMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(CommentMetadata node) {
        return new CommentMetadata(node.getCommentId(), generateUID(), node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.isMinimized(), node.getText());
    }

    /**
     * Default implementation of visit method for {@link NonDataBlockWithMenuMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PenWithParamMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(NonDataBlockWithMenuMetadata node) {
        return new NonDataBlockWithMenuMetadata(node.getCommentId(), node.getBlockId(), node.isShadow(), apply(node.getMutation()), apply(node.getMenuMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ProgramMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ProgramMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ProgramMetadata node) {
        return new ProgramMetadata(apply(node.getMonitor()), apply(node.getExtension()), apply(node.getMeta()));
    }

    /**
     * Default implementation of visit method for {@link ImageMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ImageMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ImageMetadata node) {
        return new ImageMetadata(node.getAssetId(), node.getUniqueName(), node.getMd5ext(), node.getDataFormat(),
                node.getBitmapResolution(), node.getRotationCenterX(), node.getRotationCenterY());
    }

    /**
     * Default implementation of visit method for {@link SoundMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SoundMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(SoundMetadata node) {
        return new SoundMetadata(node.getAssetId(), node.getUniqueName(), node.getMd5ext(), node.getDataFormat(),
                node.getRate(), node.getSampleCount());
    }

    /**
     * Default implementation of visit method for {@link MonitorSliderMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MonitorSliderMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(MonitorSliderMetadata node) {
        return new MonitorSliderMetadata(node.getId(), node.getMode(), node.getMonitorOpcode(),
                apply(node.getParamsMetadata()), node.getSpriteName(), node.getWidth(), node.getHeight(),
                node.getX(), node.getY(), node.isVisible(), node.getValue(), node.getSliderMin(), node.getSliderMax(), node.isDiscrete());
    }

    /**
     * Default implementation of visit method for {@link MonitorListMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MonitorListMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(MonitorListMetadata node) {
        return new MonitorListMetadata(node.getId(), node.getMode(), node.getMonitorOpcode(),
                apply(node.getParamsMetadata()), node.getSpriteName(), node.getWidth(),
                node.getHeight(), node.getX(), node.getY(), node.isVisible(), new ArrayList<>(node.getValues()));
    }

    /**
     * Default implementation of visit method for {@link MonitorParamMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MonitorParamMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(MonitorParamMetadata node) {
        return new MonitorParamMetadata(node.getInputName(), node.getInputValue());
    }

    /**
     * Default implementation of visit method for {@link DataBlockMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DataBlockMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(DataBlockMetadata node) {
        return new DataBlockMetadata(generateUID(), node.getX(), node.getY());
    }

    /**
     * Default implementation of visit method for {@link NonDataBlockMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NonDataBlockMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(NonDataBlockMetadata node) {
        return new NonDataBlockMetadata(node.getCommentId(), generateUID(),
                node.isShadow(), apply(node.getMutation()));
    }

    /**
     * Default implementation of visit method for {@link TopNonDataBlockMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TopNonDataBlockMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(TopNonDataBlockMetadata node) {
        return new TopNonDataBlockMetadata(node.getCommentId(), generateUID(),
                node.isShadow(), apply(node.getMutation()),
                node.getXPos(), node.getYPos());
    }

    /**
     * Default implementation of visit method for {@link NoMutationMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NoMutationMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(NoMutationMetadata node) {
        return new NoMutationMetadata();
    }

    /**
     * Default implementation of visit method for {@link ProcedureMutationMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExistingCallMutationMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ProcedureMutationMetadata node) {
        return new ProcedureMutationMetadata(node);
    }

    /**
     * Default implementation of visit method for {@link StageMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StageMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(StageMetadata node) {
        return new StageMetadata(apply(node.getCommentsMetadata()),
                node.getCurrentCostume(),
                apply(node.getCostumes()),
                apply(node.getSounds()),
                node.getTextToSpeechLanguage());
    }

    /**
     * Default implementation of visit method for {@link ActorMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SpriteMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ActorMetadata node) {
        return new ActorMetadata(apply(node.getCommentsMetadata()),
                node.getCurrentCostume(),
                apply(node.getCostumes()),
                apply(node.getSounds()));
    }

    /**
     * Default implementation of visit method for {@link CommentMetadataList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node CommentMetadataList Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(CommentMetadataList node) {
        return new CommentMetadataList(applyList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link ImageMetadataList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ImageMetadataList Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ImageMetadataList node) {
        return new ImageMetadataList(applyList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link MonitorMetadataList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MonitorMetadataList Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(MonitorMetadataList node) {
        return new MonitorMetadataList(applyList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link MonitorParamMetadataList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MonitorParamMetadataList Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(MonitorParamMetadataList node) {
        return new MonitorParamMetadataList(applyList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link SoundMetadataList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SoundMetadataList Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(SoundMetadataList node) {
        return new SoundMetadataList(applyList(node.getList()));
    }

    /**
     * Default implementation of visit method for {@link NoMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NoMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(NoMetadata node) {
        return new NoMetadata();
    }

    /**
     * Default implementation of visit method for {@link NoBlockMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node NoBlockMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(NoBlockMetadata node) {
        return new NoBlockMetadata();
    }

    /**
     * Default implementation of visit method for {@link ProcedureMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ProcedureMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ProcedureMetadata node) {
        return new ProcedureMetadata(apply(node.getDefinition()), apply(node.getPrototype()));
    }

    /**
     * Default implementation of visit method for {@link ForwardBackwardChoice}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ForwardBackwardChoice Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ForwardBackwardChoice node) {
        return new ForwardBackwardChoice(node.getTypeName());
    }

    /**
     * Default implementation of visit method for {@link TopNonDataBlockWithMenuMetadata}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node CloneOfMetadata Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(TopNonDataBlockWithMenuMetadata node) {
        return new TopNonDataBlockWithMenuMetadata(node.getCommentId(), node.getBlockId(), node.isShadow(), apply(node.getMutation()), node.getXPos(), node.getYPos(), apply(node.getMenuMetadata()));
    }

    /*
     * UID generation based on https://github.com/LLK/scratch-blocks
     */
    public static final String BLOCKLY_SOUP = "!#$%()*+,-./:;=?@[]^_`{|}~"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generate a unique ID.  This should be globally unique.
     * 87 characters ^ 20 length > 128 bits (better than a UUID).
     *
     * @return {string} A globally unique ID string.
     */
    public static String generateUID() {
        int length = 20;
        int soupLength = BLOCKLY_SOUP.length();
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < length; i++) {
            id.append(BLOCKLY_SOUP.charAt(Randomness.nextInt(soupLength)));
        }
        return id.toString();
    }

    ;

    /**
     * Default implementation of visit method for {@link Speak}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SayTextToSpeech Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(Speak node) {
        return new Speak(apply(node.getText()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ExprLanguage}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExprLanguage Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ExprLanguage node) {
        return new ExprLanguage(apply(node.getExpr()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FixedLanguage}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FixedLanguageBlock Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(FixedLanguage node) {
        return new FixedLanguage(node.getType().getType(), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetLanguage}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetLanguage Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(SetLanguage node) {
        return new SetLanguage(apply(node.getLanguage()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetVoice}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetVoice Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(SetVoice node) {
        return new SetVoice(apply(node.getVoice()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FixedVoice}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FixedVoice Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(FixedVoice node) {
        return new FixedVoice(node.getType().getType(), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ExprVoice}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ExprVoice Node of which the children will
     *             be iterated
     * @return the copy of the visited node
     */
    public ASTNode visit(ExprVoice node) {
        return new ExprVoice(apply(node.getExpr()), apply(node.getMetadata()));
    }

    // MBlockNodes
    // MBlockEvents

    /**
     * Default implementation of visit method for {@link BoardButtonAction}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BoardButtonAction  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BoardButtonAction node) {
        return new BoardButtonAction(apply(node.getPressed()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BoardLaunch}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BoardLaunch Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BoardLaunch node) {
        return new BoardLaunch(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BoardShaken}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BoardShaken  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BoardShaken node) {
        return new BoardShaken(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BoardTilted}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BoardTilted  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BoardTilted node) {
        return new BoardTilted(apply(node.getDirection()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BrightnessLess}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BrightnessLess  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BrightnessLess node) {
        return new BrightnessLess(apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LaunchButton}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LaunchButton  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LaunchButton node) {
        return new LaunchButton(apply(node.getButton()), apply(node.getMetadata()));
    }

    // MBlockBoolExpr

    /**
     * Default implementation of visit method for {@link BoardButtonPressed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BoardButtonPressed  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BoardButtonPressed node) {
        return new BoardButtonPressed(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ConnectRobot}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ConnectRobot Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ConnectRobot node) {
        return new ConnectRobot(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link IRButtonPressed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IRButtonPressed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IRButtonPressed node) {
        return new IRButtonPressed(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDMatrixPosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDMatrixPosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDMatrixPosition node) {
        return new LEDMatrixPosition(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ObstaclesAhead}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ObstaclesAhead Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ObstaclesAhead node) {
        return new ObstaclesAhead(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link OrientateTo}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node OrientateTo Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(OrientateTo node) {
        return new OrientateTo(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PortOnLine}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PortOnLine Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PortOnLine node) {
        return new PortOnLine(apply(node.getOperand1()), apply(node.getOperand2()), apply(node.getOperand3()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RobotButtonPressed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotButtonPressed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotButtonPressed node) {
        return new RobotButtonPressed(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RobotShaken}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotShaken Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotShaken node) {
        return new RobotShaken(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RobotTilted}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotTilted Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotTilted node) {
        return new RobotTilted(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SeeColor}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SeeColor Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SeeColor node) {
        return new SeeColor(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    // MBlockNumExpr

    /**
     * Default implementation of visit method for {@link AmbientLight}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node AmbientLight Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(AmbientLight node) {
        return new AmbientLight(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link BatteryEnergy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BatteryEnergy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BatteryEnergy node) {
        return new BatteryEnergy(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectAmbientLight}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectAmbientLight Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectAmbientLight node) {
        return new DetectAmbientLight(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectAmbientLightPort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectAmbientLightPort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectAmbientLightPort node) {
        return new DetectAmbientLightPort(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectDistancePort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectDistancePort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectDistancePort node) {
        return new DetectDistancePort(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectGrey}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectGrey Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectGrey node) {
        return new DetectGrey(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectIRReflection}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectIRReflection Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectIRReflection node) {
        return new DetectIRReflection(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectLinePort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectLinePort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectLinePort node) {
        return new DetectLinePort(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectReflection}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectReflection Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectReflection node) {
        return new DetectReflection(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link DetectRGBValue}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node DetectRGBValue Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(DetectRGBValue node) {
        return new DetectRGBValue(apply(node.getOperand1()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GyroPitchAngle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GyroPitchAngle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GyroPitchAngle node) {
        return new GyroPitchAngle(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link GyroRollAngle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node GyroRollAngle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(GyroRollAngle node) {
        return new GyroRollAngle(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Potentiometer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Potentiometer Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Potentiometer node) {
        return new Potentiometer(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RobotTimer}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotTimer Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotTimer node) {
        return new RobotTimer(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RotateXAngle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RotateXAngle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RotateXAngle node) {
        return new RotateXAngle(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RotateYAngle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RotateYAngle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RotateYAngle node) {
        return new RotateYAngle(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RotateZAngle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RotateZAngle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RotateZAngle node) {
        return new RotateZAngle(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShakingStrength}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ShakingStrength Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ShakingStrength node) {
        return new ShakingStrength(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SoundVolume}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SoundVolume Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SoundVolume node) {
        return new SoundVolume(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SpeakerVolume}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SpeakerVolume Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SpeakerVolume node) {
        return new SpeakerVolume(apply(node.getMetadata()));
    }

    // MBlockStringExpr

    /**
     * Default implementation of visit method for {@link IRMessage}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IRMessage Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IRMessage node) {
        return new IRMessage(apply(node.getMetadata()));
    }

    // MBlockOptions

    /**
     * Default implementation of visit method for {@link BlackWhite}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node BlackWhite Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(BlackWhite node) {
        return new BlackWhite(node.getBlackWhiteDefinition());
    }

    /**
     * Default implementation of visit method for {@link IRRemoteButton}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node IRRemoteButton Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(IRRemoteButton node) {
        return new IRRemoteButton(node.getButtonName());
    }

    /**
     * Default implementation of visit method for {@link LEDColor}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDColor Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDColor node) {
        return new LEDColor(node.getColorName());
    }

    /**
     * Default implementation of visit method for {@link LEDMatrix}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDMatrix Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDMatrix node) {
        return new LEDMatrix(node.getFaceString());
    }

    /**
     * Default implementation of visit method for {@link LEDPosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDPosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDPosition node) {
        return new LEDPosition(node.getPortDefinition());
    }

    /**
     * Default implementation of visit method for {@link LineFollowState}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LineFollowState Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LineFollowState node) {
        return new LineFollowState(node.getLineFollowDefinition());
    }

    /**
     * Default implementation of visit method for {@link MCorePort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MCorePort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MCorePort node) {
        return new MCorePort(node.getPortDefinition());
    }

    /**
     * Default implementation of visit method for {@link PadOrientation}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PadOrientation Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PadOrientation node) {
        return new PadOrientation(node.getOrientationName());
    }

    /**
     * Default implementation of visit method for {@link PressedState}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PressedState  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PressedState node) {
        return new PressedState(node.getPressedState());
    }

    /**
     * Default implementation of visit method for {@link RGB}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RGB Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RGB node) {
        return new RGB(node.getRGBName());
    }

    /**
     * Default implementation of visit method for {@link RobotAxis}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotAxis Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotAxis node) {
        return new RobotAxis(node.getAxisName());
    }

    /**
     * Default implementation of visit method for {@link RobotButton}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotButton Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotButton node) {
        return new RobotButton(node.getButtonName());
    }

    /**
     * Default implementation of visit method for {@link RobotDirection}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RobotDirection Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RobotDirection node) {
        return new RobotDirection(node.getDirectionName());
    }

    /**
     * Default implementation of visit method for {@link SoundList}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SoundList Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SoundList node) {
        return new SoundList(node.getFileName());
    }

    /**
     * Default implementation of visit method for {@link SoundNote}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SoundNote Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SoundNote node) {
        return new SoundNote(node.getNoteName());
    }

    // MBlockEmotionStmt

    /**
     * Default implementation of visit method for {@link Aggrieved}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Aggrieved Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Aggrieved node) {
        return new Aggrieved(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Agree}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Agree Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Agree node) {
        return new Agree(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Angry}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Angry Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Angry node) {
        return new Angry(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Awkward}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Awkward Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Awkward node) {
        return new Awkward(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Coquetry}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Coquetry Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Coquetry node) {
        return new Coquetry(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Deny}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Deny Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Deny node) {
        return new Deny(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Dizzy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Dizzy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Dizzy node) {
        return new Dizzy(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Exclaim}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Exclaim Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Exclaim node) {
        return new Exclaim(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Greeting}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Greeting Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Greeting node) {
        return new Greeting(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LookAround}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LookAround Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LookAround node) {
        return new LookAround(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LookDown}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LookDown Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LookDown node) {
        return new LookDown(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LookLeft}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LookLeft Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LookLeft node) {
        return new LookLeft(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LookRight}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LookRight Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LookRight node) {
        return new LookRight(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LookUp}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LookUp Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LookUp node) {
        return new LookUp(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Naughty}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Naughty Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Naughty node) {
        return new Naughty(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Proud}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Proud Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Proud node) {
        return new Proud(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Revive}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Revive Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Revive node) {
        return new Revive(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Sad}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Sad Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Sad node) {
        return new Sad(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Shiver}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Shiver Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Shiver node) {
        return new Shiver(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Sleeping}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Sleeping Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Sleeping node) {
        return new Sleeping(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Sleepy}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Sleepy Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Sleepy node) {
        return new Sleepy(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Smile}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Smile Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Smile node) {
        return new Smile(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Sprint}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Sprint  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Sprint node) {
        return new Sprint(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Startle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Startle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Startle node) {
        return new Startle(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Wink}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Wink Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Wink node) {
        return new Wink(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Yeah}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Yeah Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Yeah node) {
        return new Yeah(apply(node.getMetadata()));
    }

    // MBlockIRStmt

    /**
     * Default implementation of visit method for {@link LearnWithTime}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LearnWithTime Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LearnWithTime node) {
        return new LearnWithTime(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SendIR}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SendIR Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SendIR node) {
        return new SendIR(apply(node.getText()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SendLearnResult}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SendLearnResult Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SendLearnResult node) {
        return new SendLearnResult(apply(node.getMetadata()));
    }

    // MBlockLEDStmt

    /**
     * Default implementation of visit method for {@link LEDColorShow}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDColorShow Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDColorShow node) {
        return new LEDColorShow(apply(node.getColorString()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDColorShowPosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDColorShowPosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDColorShowPosition node) {
        return new LEDColorShowPosition(apply(node.getPosition()), apply(node.getColorString()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDColorTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDColorTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDColorTimed node) {
        return new LEDColorTimed(apply(node.getColorString()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDColorTimedPosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDColorTimedPosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDColorTimedPosition node) {
        return new LEDColorTimedPosition(apply(node.getPosition()), apply(node.getColorString()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDOff}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDOff Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDOff node) {
        return new LEDOff(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RGBValue}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RGBValue Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RGBValue node) {
        return new RGBValue(apply(node.getRgb()), apply(node.getValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RGBValuesPosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RGBValuesPosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RGBValuesPosition node) {
        return new RGBValuesPosition(apply(node.getPosition()), apply(node.getRed()), apply(node.getGreen()), apply(node.getBlue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RockyLight}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RockyLight  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RockyLight node) {
        return new RockyLight(apply(node.getColor()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link RockyLightOff}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node RockyLightOff  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(RockyLightOff node) {
        return new RockyLightOff(apply(node.getMetadata()));
    }

    // MBlockLEDMatrixStmt

    /**
     * Default implementation of visit method for {@link FacePosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FacePosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FacePosition node) {
        return new FacePosition(apply(node.getLedMatrix()), apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FacePositionPort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FacePositionPort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FacePositionPort node) {
        return new FacePositionPort(apply(node.getPort()), apply(node.getLedMatrix()), apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FaceTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FaceTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FaceTimed node) {
        return new FaceTimed(apply(node.getLedMatrix()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link FaceTimedPort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node FaceTimedPort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(FaceTimedPort node) {
        return new FaceTimedPort(apply(node.getPort()), apply(node.getLedMatrix()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDNumPort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDNumPort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDNumPort node) {
        return new LEDNumPort(apply(node.getPort()), apply(node.getNumber()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDString}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDString Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDString node) {
        return new LEDString(apply(node.getText()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDStringPort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDStringPort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDStringPort node) {
        return new LEDStringPort(apply(node.getPort()), apply(node.getText()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDStringPosition}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDStringPosition Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDStringPosition node) {
        return new LEDStringPosition(apply(node.getText()), apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDStringPositionPort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDStringPositionPort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDStringPositionPort node) {
        return new LEDStringPositionPort(apply(node.getPort()), apply(node.getText()), apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDStringScrolling}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDStringScrolling Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDStringScrolling node) {
        return new LEDStringScrolling(apply(node.getText()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDSwitchOff}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDSwitchOff Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDSwitchOff node) {
        return new LEDSwitchOff(apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDSwitchOn}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDSwitchOn Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDSwitchOn node) {
        return new LEDSwitchOn(apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDTimePort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDTimePort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDTimePort node) {
        return new LEDTimePort(apply(node.getPort()), apply(node.getHour()), apply(node.getMinute()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link LEDToggle}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node LEDToggle Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(LEDToggle node) {
        return new LEDToggle(apply(node.getxAxis()), apply(node.getyAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShowFace}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ShowFace Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ShowFace node) {
        return new ShowFace(apply(node.getLedMatrix()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ShowFacePort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ShowFacePort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ShowFacePort node) {
        return new ShowFacePort(apply(node.getPort()), apply(node.getLedMatrix()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnOffFace}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnOffFace Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnOffFace node) {
        return new TurnOffFace(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnOffFacePort}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnOffFacePort Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnOffFacePort node) {
        return new TurnOffFacePort(apply(node.getPort()), apply(node.getMetadata()));
    }

    // MBlockRobotMoveStmt

    /**
     * Default implementation of visit method for {@link KeepBackwardTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node KeepBackwardTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(KeepBackwardTimed node) {
        return new KeepBackwardTimed(apply(node.getPercent()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link KeepForwardTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node KeepForwardTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(KeepForwardTimed node) {
        return new KeepForwardTimed(apply(node.getPercent()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveBackwardTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MoveBackwardTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MoveBackwardTimed node) {
        return new MoveBackwardTimed(apply(node.getPercent()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveDirection}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MoveDirection Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MoveDirection node) {
        return new MoveDirection(apply(node.getDirection()), apply(node.getPercent()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveForwardTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MoveForwardTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MoveForwardTimed node) {
        return new MoveForwardTimed(apply(node.getPercent()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveSides}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MoveSides Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MoveSides node) {
        return new MoveSides(apply(node.getLeftPower()), apply(node.getRightPower()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link MoveStop}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node MoveStop Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(MoveStop node) {
        return new MoveStop(apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnLeft2}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnLeft2 Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnLeft2 node) {
        return new TurnLeft2(apply(node.getDegree()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnLeftTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnLeftTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnLeftTimed node) {
        return new TurnLeftTimed(apply(node.getPercent()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnRight2}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnRight2 Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnRight2 node) {
        return new TurnRight2(apply(node.getDegree()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link TurnRightTimed}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node TurnRightTimed Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(TurnRightTimed node) {
        return new TurnRightTimed(apply(node.getPercent()), apply(node.getTime()), apply(node.getMetadata()));
    }

    // MBlockResetStmt

    /**
     * Default implementation of visit method for {@link ResetAxis}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ResetAxis Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ResetAxis node) {
        return new ResetAxis(apply(node.getAxis()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link ResetTimer2}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ResetTimer2 Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ResetTimer2 node) {
        return new ResetTimer2(apply(node.getMetadata()));
    }
    // MBlockRobotSpeaker

    /**
     * Default implementation of visit method for {@link ChangeVolumeBy2}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node ChangeVolumeBy2 Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(ChangeVolumeBy2 node) {
        return new ChangeVolumeBy2(apply(node.getVolumeValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link Pause}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node Pause Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(Pause node) {
        return new Pause(apply(node.getBeat()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PlayFrequency}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PlayFrequency Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PlayFrequency node) {
        return new PlayFrequency(apply(node.getFrequency()), apply(node.getTime()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PlayNote}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PlayNote  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PlayNote node) {
        return new PlayNote(apply(node.getNote()), apply(node.getBeat()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PlaySound}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PlaySound  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PlaySound node) {
        return new PlaySound(apply(node.getSoundList()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link PlaySoundWait}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node PlaySoundWait  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(PlaySoundWait node) {
        return new PlaySoundWait(apply(node.getSoundList()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link SetVolumeTo2}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node SetVolumeTo2  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(SetVolumeTo2 node) {
        return new SetVolumeTo2(apply(node.getVolumeValue()), apply(node.getMetadata()));
    }

    /**
     * Default implementation of visit method for {@link StopAllSounds2}.
     *
     * <p>
     * Creates a deep copy of this node.
     * </p>
     *
     * @param node StopAllSounds2  Node which will be copied
     * @return the copy of the visited node
     */
    public ASTNode visit(StopAllSounds2 node) {
        return new StopAllSounds2(apply(node.getMetadata()));
    }

    public ASTNode visit(Tempo node) {
        return new Tempo(apply(node.getMetadata()));
    }

    public ASTNode visit(ChangeTempoBy node) {
        return new ChangeTempoBy(apply(node.getTempo()), apply(node.getMetadata()));
    }

    public ASTNode visit(SetTempoTo node) {
        return new SetTempoTo(apply(node.getTempo()), apply(node.getMetadata()));
    }

    public ASTNode visit(SetInstrumentTo node) {
        return new SetInstrumentTo(apply(node.getInstrument()), apply(node.getMetadata()));
    }

    public ASTNode visit(PlayDrumForBeats node) {
        return new PlayDrumForBeats(apply(node.getDrum()), apply(node.getBeats()), apply(node.getMetadata()));
    }

    public ASTNode visit(PlayNoteForBeats node) {
        return new PlayNoteForBeats(apply(node.getNote()), apply(node.getBeats()), apply(node.getMetadata()));
    }

    public ASTNode visit(RestForBeats node) {
        return new RestForBeats(apply(node.getBeats()), apply(node.getMetadata()));
    }

    public ASTNode visit(FixedNote node) {
        return new FixedNote(node.getNote(), node.getMetadata());
    }

    public ASTNode visit(ExprNote node) {
        return new ExprNote(apply(node.getExpr()), apply(node.getMetadata()));
    }

    public ASTNode visit(FixedInstrument node) {

        return new FixedInstrument(node.getType().getType(), apply(node.getMetadata()));
    }

    public ASTNode visit(ExprInstrument node) {

        return new ExprInstrument(apply(node.getExpr()), apply(node.getMetadata()));
    }

    public ASTNode visit(FixedDrum node) {
        return new FixedDrum(node.getType().getType(), apply(node.getMetadata()));
    }

    public ASTNode visit(ExprDrum node) {
        return new ExprDrum(apply(node.getExpr()), apply(node.getMetadata()));
    }

    //Translate

    public ASTNode visit(TFixedLanguage node) {

        return new TFixedLanguage(node.getType().getType(), apply(node.getMetadata()));
    }

    public ASTNode visit(TExprLanguage node) {

        return new TExprLanguage(apply(node.getExpr()), apply(node.getMetadata()));
    }

    public ASTNode visit(TranslateTo node) {
        return new TranslateTo(apply(node.getText()), apply(node.getLanguage()), apply(node.getMetadata()));
    }

    public ASTNode visit(ViewerLanguage node) {
        return new ViewerLanguage(apply(node.getMetadata()));
    }
}
