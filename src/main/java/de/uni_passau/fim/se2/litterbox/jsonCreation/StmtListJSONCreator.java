package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.UnspecifiedStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.FieldsMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.MutationMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.StopMutation;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeSoundEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ClearSoundEffects;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenStampStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.*;

public class StmtListJSONCreator implements ScratchVisitor {
    private String previousBlockId = null;
    private List<String> finishedJSONStrings;
    private List<Stmt> stmtList;
    private int counter;
    private IdVisitor idVis;
    private SymbolTable symbolTable;
    public final static String EMPTY_VALUE = "{}";

    public StmtListJSONCreator(String parentID, StmtList stmtList, SymbolTable symbolTable) {
        previousBlockId = parentID;
        finishedJSONStrings = new ArrayList<>();
        this.stmtList = stmtList.getStmts();
        counter = 0;
        idVis = new IdVisitor();
        this.symbolTable = symbolTable;
    }

    public StmtListJSONCreator(StmtList stmtList, SymbolTable symbolTable) {
        finishedJSONStrings = new ArrayList<>();
        this.stmtList = stmtList.getStmts();
        counter = 0;
        idVis = new IdVisitor();
        this.symbolTable = symbolTable;
    }

    public String createStmtListJSONString() {
        for (Stmt stmt : stmtList) {
            stmt.accept(this);
            counter++;
        }
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; i < finishedJSONStrings.size() - 1; i++) {
            jsonString.append(finishedJSONStrings.get(i)).append(",");
        }
        if (finishedJSONStrings.size() > 0) {
            jsonString.append(finishedJSONStrings.get(finishedJSONStrings.size() - 1));
        }
        return jsonString.toString();
    }

    private String getNextId() {
        String nextId = null;
        if (counter < stmtList.size() - 1) {
            nextId = idVis.getBlockId(stmtList.get(counter + 1));
        }
        return nextId;
    }

    @Override
    public void visit(IfOnEdgeBounce node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(NextCostume node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(NextBackdrop node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Show node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Hide node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StopAllSounds node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteClone node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ResetTimer node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetRotationStyle node) {
        String rotation = node.getRotation().toString();
        FieldsMetadata fieldsMeta = ((NonDataBlockMetadata) node.getMetadata()).getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), rotation, null);
        finishedJSONStrings.add(createBlockWithoutMutationString((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GoToLayer node) {
        FieldsMetadata fieldsMeta = ((NonDataBlockMetadata) node.getMetadata()).getFields().getList().get(0);
        String layer = node.getLayerChoice().getType();
        String fieldsString = createFields(fieldsMeta.getFieldsName(), layer, null);
        finishedJSONStrings.add(createBlockWithoutMutationString((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetDragMode node) {
        FieldsMetadata fieldsMeta = ((NonDataBlockMetadata) node.getMetadata()).getFields().getList().get(0);
        String drag = node.getDrag().getToken();
        String fieldsString = createFields(fieldsMeta.getFieldsName(), drag, null);
        finishedJSONStrings.add(createBlockWithoutMutationString((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteAllOf node) {
        getListDataFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
    }

    @Override
    public void visit(ShowList node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    private String getListDataFields(NonDataBlockMetadata metadata, Identifier identifier) {
        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        Preconditions.checkArgument(identifier instanceof Qualified, "Identifier of list has to be in Qualified");
        Qualified qual = (Qualified) identifier;
        Preconditions.checkArgument(qual.getSecond() instanceof ScratchList, "Qualified has to hold Scratch List");
        ScratchList list = (ScratchList) qual.getSecond();
        String id = symbolTable.getListIdentifierFromActorAndName(qual.getFirst().getName(), list.getName().getName());
        return createFields(fieldsMeta.getFieldsName(), list.getName().getName(), id);
    }

    @Override
    public void visit(HideList node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ShowVariable node) {
        getVariableFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
    }

    @Override
    public void visit(HideVariable node) {
        getVariableFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
    }

    private void getVariableFields(NonDataBlockMetadata metadata, Identifier identifier) {
        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        Preconditions.checkArgument(identifier instanceof Qualified, "Identifier of variable has to be in Qualified");
        Qualified qual = (Qualified) identifier;
        Preconditions.checkArgument(qual.getSecond() instanceof Variable, "Qualified has to hold Variable");
        Variable variable = (Variable) qual.getSecond();
        String id = symbolTable.getVariableIdentifierFromActorAndName(qual.getFirst().getName(),
                variable.getName().getName());
        String fieldsString = createFields(fieldsMeta.getFieldsName(), variable.getName().getName(), id);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(StopAll node) {
        FieldsMetadata fieldsMeta = ((NonDataBlockMetadata) node.getMetadata()).getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), "all", null);
        getStopMutation(fieldsString, (NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(StopThisScript node) {
        FieldsMetadata fieldsMeta = ((NonDataBlockMetadata) node.getMetadata()).getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), "this script", null);
        getStopMutation(fieldsString, (NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        FieldsMetadata fieldsMeta = ((NonDataBlockMetadata) node.getMetadata()).getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), "other scripts in sprite", null);
        getStopMutation(fieldsString, (NonDataBlockMetadata) node.getMetadata());
    }

    private void getStopMutation(String fieldsString, NonDataBlockMetadata metadata) {
        MutationMetadata mutation = metadata.getMutation();
        Preconditions.checkArgument(mutation instanceof StopMutation);
        StopMutation stopMutation = (StopMutation) mutation;
        String mutationString = createStopMetadata(stopMutation.getTagName(), stopMutation.isHasNext());
        finishedJSONStrings.add(createBlockWithMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, mutationString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(PenDownStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenUpStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenClearStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenStampStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        StmtListJSONCreator creator = null;
        List<String> inputs = new ArrayList<>();
        String insideBlockId = createSubstackJSON(stmtList, metadata);
        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(UntilStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        List<String> inputs = new ArrayList<>();
        String conditionBlockId = null;

        BoolExpr condition = node.getBoolExpr();

        if (condition instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, CONDITION_KEY));
        } else {
            //todo expression handling
        }

        String insideBlockId = createSubstackJSON(stmtList, metadata);

        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(IfElseStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        StmtList elseStmtList = node.getElseStmts();
        List<String> inputs = new ArrayList<>();
        String conditionBlockId = null;

        BoolExpr condition = node.getBoolExpr();

        if (condition instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, CONDITION_KEY));
        } else {
            //todo expression handling
        }

        String insideBlockId = createSubstackJSON(stmtList, metadata);
        String elseInsideBlockId = createSubstackJSON(elseStmtList, metadata);

        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY));
        inputs.add(createReferenceJSON(elseInsideBlockId, SUBSTACK2_KEY));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(IfThenStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getThenStmts();
        List<String> inputs = new ArrayList<>();
        String conditionBlockId = null;

        BoolExpr condition = node.getBoolExpr();

        if (condition instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, CONDITION_KEY));
        } else {
            //todo expression handling
        }

        String insideBlockId = createSubstackJSON(stmtList, metadata);
        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(TIMES_KEY, node.getTimes(), WHOLE_NUM_PRIMITIVE));

        String insideBlockId = createSubstackJSON(stmtList, metadata);
        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(WaitUntil node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        BoolExpr condition = node.getUntil();

        List<String> inputs = new ArrayList<>();
        if (condition instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, CONDITION_KEY));
        } else {
            //todo expression handling
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }


    @Override
    public void visit(WaitSeconds node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, DURATION_KEY, node.getSeconds(), POSITIVE_NUM_PRIMITIVE);
    }

    @Override
    public void visit(MoveSteps node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, STEPS_KEY, node.getSteps(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(TurnLeft node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, DEGREES_KEY, node.getDegrees(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(TurnRight node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, DEGREES_KEY, node.getDegrees(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(PointInDirection node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, DIRECTION_KEY_CAP, node.getDirection(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(ChangeXBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, DX_KEY, node.getNum(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(ChangeYBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, DY_KEY, node.getNum(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(SetYTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, Y, node.getNum(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(SetXTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, X, node.getNum(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(GoToPosXY node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(X, node.getX(), MATH_NUM_PRIMITIVE));
        inputs.add(createNumExpr(Y, node.getY(), MATH_NUM_PRIMITIVE));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(SECS_KEY, node.getSecs(), MATH_NUM_PRIMITIVE));
        inputs.add(createNumExpr(X, node.getX(), MATH_NUM_PRIMITIVE));
        inputs.add(createNumExpr(Y, node.getY(), MATH_NUM_PRIMITIVE));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, CHANGE_KEY, node.getNum(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(SetSizeTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeNumExprBlock(metadata, SIZE_KEY_CAP, node.getPercent(), MATH_NUM_PRIMITIVE);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(),
                node.getEffect().getToken(), VALUE_KEY);
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(),
                node.getEffect().getToken(), CHANGE_KEY);
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(),
                node.getEffect().getToken(), VALUE_KEY);
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(),
                node.getEffect().getToken(), VALUE_KEY);
    }

    @Override
    public void visit(Say node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeStringExprBlock(metadata, MESSAGE_KEY, node.getString());
    }

    @Override
    public void visit(Think node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeStringExprBlock(metadata, MESSAGE_KEY, node.getThought());
    }

    @Override
    public void visit(AskAndWait node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingeStringExprBlock(metadata, QUESTION_KEY, node.getQuestion());
    }

    @Override
    public void visit(AddTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(metadata, node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createStringExpr(ITEM_KEY, node.getString()));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(DeleteOf node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(metadata, node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(INDEX_KEY, node.getNum(), INTEGER_NUM_PRIMITIVE));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(InsertAt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(metadata, node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createStringExpr(ITEM_KEY, node.getString()));
        inputs.add(createNumExpr(INDEX_KEY, node.getIndex(), INTEGER_NUM_PRIMITIVE));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ReplaceItem node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(metadata, node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(INDEX_KEY, node.getIndex(), INTEGER_NUM_PRIMITIVE));
        inputs.add(createStringExpr(ITEM_KEY, node.getString()));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    private void createNumExprFieldsBlockJson(NonDataBlockMetadata metadata, NumExpr value, String fieldsValue,
                                              String inputName) {
        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(inputName, value, MATH_NUM_PRIMITIVE));
        String fields = createFields(fieldsMeta.getFieldsName(), fieldsValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fields));

        previousBlockId = metadata.getBlockId();
    }


    private String createSubstackJSON(StmtList stmtList, NonDataBlockMetadata metadata) {
        String insideBlockId = null;
        StmtListJSONCreator creator = null;
        if (stmtList.getStmts().size() > 0) {
            creator = new StmtListJSONCreator(metadata.getBlockId(), stmtList, symbolTable);
            insideBlockId = idVis.getBlockId(stmtList.getStmts().get(0));
        }
        if (creator != null) {
            finishedJSONStrings.add(creator.createStmtListJSONString());
        }
        return insideBlockId;
    }


    private void createSingeNumExprBlock(NonDataBlockMetadata metadata, String inputKey, NumExpr numExpr,
                                         int primitive) {
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(inputKey, numExpr, primitive));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    private void createSingeStringExprBlock(NonDataBlockMetadata metadata, String inputKey, StringExpr stringExpr) {
        List<String> inputs = new ArrayList<>();

        inputs.add(createStringExpr(inputKey, stringExpr));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE));

        previousBlockId = metadata.getBlockId();
    }

    private String createNumExpr(String inputKey, NumExpr numExpr, int primitive) {
        if (numExpr instanceof UnspecifiedNumExpr) {
            return createTypeInput(inputKey, INPUT_SAME_BLOCK_SHADOW, primitive, "");
        } else if (numExpr instanceof NumberLiteral) {
            return createTypeInput(inputKey, INPUT_SAME_BLOCK_SHADOW, primitive,
                    String.valueOf((float) ((NumberLiteral) numExpr).getValue()));
        } else {
            //todo expression handling
            return "";
        }
    }

    private String createStringExpr(String inputKey, StringExpr numExpr) {
        if (numExpr instanceof UnspecifiedStringExpr) {
            return createTypeInput(inputKey, INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE, "");
        } else if (numExpr instanceof StringLiteral) {
            return createTypeInput(inputKey, INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE,
                    ((StringLiteral) numExpr).getText());
        } else {
            //todo expression handling
            return "";
        }
    }
}
