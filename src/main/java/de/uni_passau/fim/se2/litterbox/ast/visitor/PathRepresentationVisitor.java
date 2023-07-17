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

public class PathRepresentationVisitor implements ScratchVisitor {
    private int representation = -1;

    public int getRepresentation() {
        return representation;
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
    @Override
    public void visit(ASTNode node) {
        throw new UnsupportedOperationException("Should not be called with this visitor! Unique Name:" + node.getUniqueName());
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
    @Override
    public void visit(ActorDefinition node) {
        representation = 1;
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
    @Override
    public void visit(SetStmt node) {
        representation = 2;
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
    @Override
    public void visit(Equals node) {
        representation = 3;
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
    @Override
    public void visit(LessThan node) {
        representation = 4;
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
    @Override
    public void visit(BiggerThan node) {
        representation = 5;
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
    @Override
    public void visit(ProcedureDefinition node) {
        representation = 6;
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
    @Override
    public void visit(StrId node) {
        representation = 7;
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
    @Override
    public void visit(Script node) {
        representation = 8;
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
    @Override
    public void visit(CreateCloneOf node) {
        representation = 9;
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
    @Override
    public void visit(StartedAsClone node) {
        representation = 10;
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
    @Override
    public void visit(IfElseStmt node) {
        representation = 11;
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
    @Override
    public void visit(IfThenStmt node) {
        representation = 12;
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
    @Override
    public void visit(WaitUntil node) {
        representation = 13;
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
    @Override
    public void visit(UntilStmt node) {
        representation = 14;
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
    @Override
    public void visit(Not node) {
        representation = 15;
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
    @Override
    public void visit(And node) {
        representation = 16;
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
    @Override
    public void visit(Or node) {
        representation = 17;
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
    @Override
    public void visit(Broadcast node) {
        representation = 18;
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
    @Override
    public void visit(BroadcastAndWait node) {
        representation = 19;
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
    @Override
    public void visit(ReceptionOfMessage node) {
        representation = 20;
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
    @Override
    public void visit(RepeatForeverStmt node) {
        representation = 21;
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
    @Override
    public void visit(CallStmt node) {
        representation = 22;
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
    @Override
    public void visit(DeleteClone node) {
        representation = 23;
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
    @Override
    public void visit(StopAll node) {
        representation = 24;
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
    @Override
    public void visit(StmtList node) {
        representation = 25;
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
    @Override
    public void visit(RepeatTimesStmt node) {
        representation = 26;
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
    @Override
    public void visit(StringLiteral node) {
        representation = 27;
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
    @Override
    public void visit(BoolLiteral node) {
        representation = 28;
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
    @Override
    public void visit(NumberLiteral node) {
        representation = 29;
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
    @Override
    public void visit(ColorLiteral node) {
        representation = 30;
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
    @Override
    public void visit(LocalIdentifier node) {
        representation = 31;
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
    @Override
    public void visit(Never node) {
        representation = 32;
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
    @Override
    public void visit(ParameterDefinitionList node) {
        representation = 33;
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
    @Override
    public void visit(ParameterDefinition node) {
        representation = 34;
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
    @Override
    public void visit(ExpressionList node) {
        representation = 35;
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
    @Override
    public void visit(Type node) {
        representation = 36;
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
    @Override
    public void visit(SwitchBackdrop node) {
        representation = 37;
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
    @Override
    public void visit(NextBackdrop node) {
        representation = 38;
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
    @Override
    public void visit(SwitchBackdropAndWait node) {
        representation = 39;
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
    @Override
    public void visit(BackdropSwitchTo node) {
        representation = 40;
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
    @Override
    public void visit(KeyPressed node) {
        representation = 41;
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
    @Override
    public void visit(MoveSteps node) {
        representation = 42;
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
    @Override
    public void visit(ChangeXBy node) {
        representation = 43;
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
    @Override
    public void visit(ChangeYBy node) {
        representation = 44;
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
    @Override
    public void visit(SetXTo node) {
        representation = 45;
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
    @Override
    public void visit(SetYTo node) {
        representation = 46;
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
    @Override
    public void visit(GoToPos node) {
        representation = 47;
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
    @Override
    public void visit(GoToPosXY node) {
        representation = 48;
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
    @Override
    public void visit(TerminationStmt node) {
        representation = 49;
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
    @Override
    public void visit(Qualified node) {
        representation = 50;
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
    @Override
    public void visit(ColorTouchingColor node) {
        representation = 51;
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
    @Override
    public void visit(Touching node) {
        representation = 52;
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
    @Override
    public void visit(Clicked node) {
        representation = 53;
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
    @Override
    public void visit(SetAttributeTo node) {
        representation = 54;
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
    @Override
    public void visit(ActorDefinitionList node) {
        representation = 55;
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
    @Override
    public void visit(ActorType node) {
        representation = 56;
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
    @Override
    public void visit(Key node) {
        representation = 57;
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
    @Override
    public void visit(Message node) {
        representation = 58;
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
    @Override
    public void visit(Program node) {
        representation = 59;
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
    @Override
    public void visit(SetStmtList node) {
        representation = 60;
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
    @Override
    public void visit(ElementChoice node) {
        representation = 61;
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
    @Override
    public void visit(Next node) {
        representation = 62;
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
    @Override
    public void visit(Prev node) {
        representation = 63;
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
    @Override
    public void visit(Random node) {
        representation = 64;
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
    @Override
    public void visit(WithExpr node) {
        representation = 65;
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
    @Override
    public void visit(Event node) {
        representation = 66;
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
    @Override
    public void visit(GreenFlag node) {
        representation = 67;
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
    @Override
    public void visit(AttributeAboveValue node) {
        representation = 68;
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
    @Override
    public void visit(ComparableExpr node) {
        representation = 69;
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
    @Override
    public void visit(Expression node) {
        representation = 70;
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
    @Override
    public void visit(UnspecifiedExpression node) {
        representation = 71;
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
    @Override
    public void visit(BoolExpr node) {
        representation = 72;
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
    @Override
    public void visit(StringContains node) {
        representation = 73;
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
    @Override
    public void visit(IsKeyPressed node) {
        representation = 74;
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
    @Override
    public void visit(IsMouseDown node) {
        representation = 75;
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
    @Override
    public void visit(UnspecifiedBoolExpr node) {
        representation = 76;
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
    @Override
    public void visit(Color node) {
        representation = 77;
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
    @Override
    public void visit(FromNumber node) {
        representation = 78;
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
    @Override
    public void visit(NumExpr node) {
        representation = 79;
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
    @Override
    public void visit(Add node) {
        representation = 80;
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
    @Override
    public void visit(AsNumber node) {
        representation = 81;
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
    @Override
    public void visit(Current node) {
        representation = 82;
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
    @Override
    public void visit(DaysSince2000 node) {
        representation = 83;
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
    @Override
    public void visit(DistanceTo node) {
        representation = 84;
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
    @Override
    public void visit(Div node) {
        representation = 85;
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
    @Override
    public void visit(IndexOf node) {
        representation = 86;
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
    @Override
    public void visit(LengthOfString node) {
        representation = 87;
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
    @Override
    public void visit(LengthOfVar node) {
        representation = 88;
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
    @Override
    public void visit(Loudness node) {
        representation = 89;
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
    @Override
    public void visit(Minus node) {
        representation = 90;
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
    @Override
    public void visit(Mod node) {
        representation = 91;
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
    @Override
    public void visit(MouseX node) {
        representation = 92;
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
    @Override
    public void visit(MouseY node) {
        representation = 93;
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
    @Override
    public void visit(Mult node) {
        representation = 94;
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
    @Override
    public void visit(NumFunct node) {
        representation = 95;
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
    @Override
    public void visit(NumFunctOf node) {
        representation = 96;
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
    @Override
    public void visit(PickRandom node) {
        representation = 97;
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
    @Override
    public void visit(Round node) {
        representation = 98;
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
    @Override
    public void visit(Timer node) {
        representation = 99;
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
    @Override
    public void visit(UnspecifiedNumExpr node) {
        representation = 100;
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
    @Override
    public void visit(StringExpr node) {
        representation = 101;
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
    @Override
    public void visit(AsString node) {
        representation = 102;
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
    @Override
    public void visit(AttributeOf node) {
        representation = 103;
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
    @Override
    public void visit(ItemOfVariable node) {
        representation = 104;
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
    @Override
    public void visit(Join node) {
        representation = 105;
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
    @Override
    public void visit(LetterOf node) {
        representation = 106;
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
    @Override
    public void visit(UnspecifiedStringExpr node) {
        representation = 107;
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
    @Override
    public void visit(Username node) {
        representation = 108;
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
    @Override
    public void visit(Position node) {
        representation = 109;
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
    @Override
    public void visit(MousePos node) {
        representation = 110;
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
    @Override
    public void visit(FromExpression node) {
        representation = 111;
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
    @Override
    public void visit(RandomPos node) {
        representation = 112;
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
    @Override
    public void visit(ProcedureDefinitionList node) {
        representation = 113;
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
    @Override
    public void visit(Stmt node) {
        representation = 114;
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
    @Override
    public void visit(ExpressionStmt node) {
        representation = 115;
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
    @Override
    public void visit(UnspecifiedStmt node) {
        representation = 116;
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
    @Override
    public void visit(ActorLookStmt node) {
        representation = 117;
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
    @Override
    public void visit(AskAndWait node) {
        representation = 118;
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
    @Override
    public void visit(ClearGraphicEffects node) {
        representation = 119;
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
    @Override
    public void visit(ActorSoundStmt node) {
        representation = 120;
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
    @Override
    public void visit(ClearSoundEffects node) {
        representation = 121;
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
    @Override
    public void visit(PlaySoundUntilDone node) {
        representation = 122;
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
    @Override
    public void visit(StartSound node) {
        representation = 123;
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
    @Override
    public void visit(StopAllSounds node) {
        representation = 124;
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
    @Override
    public void visit(CommonStmt node) {
        representation = 125;
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
    @Override
    public void visit(ChangeVariableBy node) {
        representation = 126;
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
    @Override
    public void visit(ResetTimer node) {
        representation = 127;
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
    @Override
    public void visit(SetVariableTo node) {
        representation = 128;
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
    @Override
    public void visit(StopOtherScriptsInSprite node) {
        representation = 129;
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
    @Override
    public void visit(WaitSeconds node) {
        representation = 130;
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
    @Override
    public void visit(ControlStmt node) {
        representation = 131;
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
    @Override
    public void visit(IfStmt node) {
        representation = 132;
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
    @Override
    public void visit(DeclarationStmt node) {
        representation = 133;
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
    @Override
    public void visit(DeclarationAttributeAsTypeStmt node) {
        representation = 134;
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
    @Override
    public void visit(DeclarationAttributeOfIdentAsTypeStmt node) {
        representation = 135;
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
    @Override
    public void visit(DeclarationIdentAsTypeStmt node) {
        representation = 136;
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
    @Override
    public void visit(DeclarationStmtList node) {
        representation = 137;
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
    @Override
    public void visit(DeclarationBroadcastStmt node) {
        representation = 138;
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
    @Override
    public void visit(ListStmt node) {
        representation = 139;
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
    @Override
    public void visit(AddTo node) {
        representation = 140;
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
    @Override
    public void visit(DeleteAllOf node) {
        representation = 141;
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
    @Override
    public void visit(DeleteOf node) {
        representation = 142;
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
    @Override
    public void visit(InsertAt node) {
        representation = 143;
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
    @Override
    public void visit(ReplaceItem node) {
        representation = 144;
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
    @Override
    public void visit(SpriteLookStmt node) {
        representation = 145;
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
    @Override
    public void visit(ChangeLayerBy node) {
        representation = 146;
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
    @Override
    public void visit(ChangeSizeBy node) {
        representation = 147;
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
    @Override
    public void visit(GoToLayer node) {
        representation = 148;
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
    @Override
    public void visit(Hide node) {
        representation = 149;
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
    @Override
    public void visit(HideVariable node) {
        representation = 150;
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
    @Override
    public void visit(HideList node) {
        representation = 151;
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
    @Override
    public void visit(ShowList node) {
        representation = 152;
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
    @Override
    public void visit(Say node) {
        representation = 153;
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
    @Override
    public void visit(SayForSecs node) {
        representation = 154;
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
    @Override
    public void visit(SetSizeTo node) {
        representation = 155;
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
    @Override
    public void visit(Show node) {
        representation = 156;
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
    @Override
    public void visit(ShowVariable node) {
        representation = 157;
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
    @Override
    public void visit(SwitchCostumeTo node) {
        representation = 158;
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
    @Override
    public void visit(NextCostume node) {
        representation = 159;
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
    @Override
    public void visit(Think node) {
        representation = 160;
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
    @Override
    public void visit(ThinkForSecs node) {
        representation = 161;
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
    @Override
    public void visit(SpriteMotionStmt node) {
        representation = 162;
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
    @Override
    public void visit(GlideSecsTo node) {
        representation = 163;
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
    @Override
    public void visit(GlideSecsToXY node) {
        representation = 164;
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
    @Override
    public void visit(IfOnEdgeBounce node) {
        representation = 165;
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
    @Override
    public void visit(PointInDirection node) {
        representation = 166;
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
    @Override
    public void visit(PointTowards node) {
        representation = 167;
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
    @Override
    public void visit(TurnLeft node) {
        representation = 168;
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
    @Override
    public void visit(TurnRight node) {
        representation = 169;
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
    @Override
    public void visit(StopThisScript node) {
        representation = 170;
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
    @Override
    public void visit(TimeComp node) {
        representation = 171;
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
    @Override
    public void visit(Touchable node) {
        representation = 172;
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
    @Override
    public void visit(Edge node) {
        representation = 173;
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
    @Override
    public void visit(MousePointer node) {
        representation = 174;
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
    @Override
    public void visit(SpriteTouchable node) {
        representation = 175;
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
    @Override
    public void visit(BooleanType node) {
        representation = 176;
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
    @Override
    public void visit(ListType node) {
        representation = 177;
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
    @Override
    public void visit(NumberType node) {
        representation = 178;
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
    @Override
    public void visit(StringType node) {
        representation = 179;
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
    @Override
    public void visit(Identifier node) {
        representation = 180;
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
    @Override
    public void visit(AsBool node) {
        representation = 181;
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
    @Override
    public void visit(AsTouchable node) {
        representation = 182;
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
    @Override
    public void visit(ScriptList node) {
        representation = 183;
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
    @Override
    public void visit(SpriteClicked node) {
        representation = 184;
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
    @Override
    public void visit(StageClicked node) {
        representation = 185;
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
    @Override
    public void visit(Costume node) {
        representation = 186;
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
    @Override
    public void visit(Backdrop node) {
        representation = 187;
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
    @Override
    public void visit(Direction node) {
        representation = 188;
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
    @Override
    public void visit(PositionX node) {
        representation = 189;
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
    @Override
    public void visit(PositionY node) {
        representation = 190;
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
    @Override
    public void visit(Size node) {
        representation = 191;
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
    @Override
    public void visit(Volume node) {
        representation = 192;
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
    @Override
    public void visit(Answer node) {
        representation = 193;
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
    @Override
    public void visit(NameNum node) {
        representation = 194;
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
    @Override
    public void visit(FixedAttribute node) {
        representation = 195;
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
    @Override
    public void visit(Attribute node) {
        representation = 196;
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
    @Override
    public void visit(AttributeFromFixed node) {
        representation = 197;
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
    @Override
    public void visit(AttributeFromVariable node) {
        representation = 198;
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
    @Override
    public void visit(LayerChoice node) {
        representation = 199;
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
    @Override
    public void visit(SetGraphicEffectTo node) {
        representation = 200;
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
    @Override
    public void visit(ChangeGraphicEffectBy node) {
        representation = 201;
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
    @Override
    public void visit(GraphicEffect node) {
        representation = 202;
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
    @Override
    public void visit(SoundEffect node) {
        representation = 203;
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
    @Override
    public void visit(SetSoundEffectTo node) {
        representation = 204;
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
    @Override
    public void visit(ChangeSoundEffectBy node) {
        representation = 205;
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
    @Override
    public void visit(SetVolumeTo node) {
        representation = 206;
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
    @Override
    public void visit(ChangeVolumeBy node) {
        representation = 207;
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
    @Override
    public void visit(DragMode node) {
        representation = 208;
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
    @Override
    public void visit(RotationStyle node) {
        representation = 209;
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
    @Override
    public void visit(SetRotationStyle node) {
        representation = 210;
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
    @Override
    public void visit(SetDragMode node) {
        representation = 211;
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
    @Override
    public void visit(SpriteTouchingColor node) {
        representation = 212;
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
    @Override
    public void visit(DataExpr node) {
        representation = 213;
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
    @Override
    public void visit(Variable node) {
        representation = 214;
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
    @Override
    public void visit(ScratchList node) {
        representation = 215;
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
    @Override
    public void visit(Parameter node) {
        representation = 216;
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
    @Override
    public void visit(ListContains node) {
        representation = 217;
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
    @Override
    public void visit(EventAttribute node) {
        representation = 218;
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
    @Override
    public void visit(Metadata node) {
        representation = 219;
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
    @Override
    public void visit(MetaMetadata node) {
        representation = 220;
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
    @Override
    public void visit(ExtensionMetadata node) {
        representation = 221;
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
    @Override
    public void visit(CommentMetadata node) {
        representation = 222;
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
    @Override
    public void visit(ProgramMetadata node) {
        representation = 223;
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
    @Override
    public void visit(ResourceMetadata node) {
        representation = 224;
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
    @Override
    public void visit(ImageMetadata node) {
        representation = 225;
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
    @Override
    public void visit(SoundMetadata node) {
        representation = 226;
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
    @Override
    public void visit(MonitorMetadata node) {
        representation = 227;
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
    @Override
    public void visit(MonitorSliderMetadata node) {
        representation = 228;
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
    @Override
    public void visit(MonitorListMetadata node) {
        representation = 229;
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
    @Override
    public void visit(MonitorParamMetadata node) {
        representation = 230;
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
    @Override
    public void visit(BlockMetadata node) {
        representation = 231;
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
    @Override
    public void visit(DataBlockMetadata node) {
        representation = 232;
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
    @Override
    public void visit(NonDataBlockMetadata node) {
        representation = 233;
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
    @Override
    public void visit(TopNonDataBlockMetadata node) {
        representation = 234;
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
    @Override
    public void visit(MutationMetadata node) {
        representation = 235;
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
    @Override
    public void visit(NoMutationMetadata node) {
        representation = 236;
    }

    /**
     * Default implementation of visit method for {@link ProcedureMutationMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExistingCallMutationMetadata Node of which the children will
     *             be iterated
     */
    @Override
    public void visit(ProcedureMutationMetadata node) {
        representation = 237;
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
    @Override
    public void visit(ActorMetadata node) {
        representation = 238;
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
    @Override
    public void visit(StageMetadata node) {
        representation = 239;
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
    @Override
    public void visit(CommentMetadataList node) {
        representation = 240;
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
    @Override
    public void visit(ImageMetadataList node) {
        representation = 241;
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
    @Override
    public void visit(MonitorMetadataList node) {
        representation = 242;
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
    @Override
    public void visit(MonitorParamMetadataList node) {
        representation = 243;
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
    @Override
    public void visit(SoundMetadataList node) {
        representation = 244;
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
    @Override
    public void visit(NoMetadata node) {
        representation = 245;
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
    @Override
    public void visit(NoBlockMetadata node) {
        representation = 246;
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
    @Override
    public void visit(ProcedureMetadata node) {
        representation = 247;
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
    @Override
    public void visit(ForwardBackwardChoice node) {
        representation = 248;
    }

    /**
     * Default implementation of visit method for {@link TopNonDataBlockWithMenuMetadata}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node TopNonDataBlockWithMenuMetadata Node of which the children will
     *             be iterated
     */
    @Override
    public void visit(TopNonDataBlockWithMenuMetadata node) {
        representation = 249;
    }

    /**
     * Default implementation of visit method for {@link LoopStmt}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node LoopStmt  Node of which the children will be iterated
     */
    @Override
    public void visit(LoopStmt node) {
        representation = 250;
    }

    /**
     * All blocks are either {@link Expression expressions}, {@link Stmt statements}, or {@link Event events}.
     * Other nodes that are for drop down menus are handled as {@link ASTNode AST nodes}.
     *
     * @param node A node that should be visited.
     */
    @Override
    public void visitDefaultVisitor(ASTNode node) {
        representation = 251;
    }

    /**
     * Default implementation of visit method for {@link ExtensionBlock}.
     *
     * <p>
     * Iterates all children of this node without performing any action.
     * </p>
     *
     * @param node ExtensionBlock Node of which the children will
     *             be iterated
     */
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
