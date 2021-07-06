/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.PenExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.JSONStringCreator.createField;

public class StmtListJSONCreator implements ScratchVisitor, PenExtensionVisitor, TextToSpeechExtensionVisitor {
    private String previousBlockId = null;
    private List<String> finishedJSONStrings;
    private List<Stmt> stmtList;
    private int counter;
    private IdVisitor idVis;
    private SymbolTable symbolTable;
    private ExpressionJSONCreator exprCreator;
    private FixedExpressionJSONCreator fixedExprCreator;

    public StmtListJSONCreator(String parentId, StmtList stmtList, SymbolTable symbolTable) {
        previousBlockId = parentId;
        finishedJSONStrings = new ArrayList<>();
        this.stmtList = stmtList.getStmts();
        counter = 0;
        idVis = new IdVisitor();
        this.symbolTable = symbolTable;
        exprCreator = new ExpressionJSONCreator();
        fixedExprCreator = new FixedExpressionJSONCreator();
    }

    public StmtListJSONCreator(StmtList stmtList, SymbolTable symbolTable) {
        finishedJSONStrings = new ArrayList<>();
        this.stmtList = stmtList.getStmts();
        counter = 0;
        idVis = new IdVisitor();
        this.symbolTable = symbolTable;
        exprCreator = new ExpressionJSONCreator();
        fixedExprCreator = new FixedExpressionJSONCreator();
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
        if (!finishedJSONStrings.isEmpty()) {
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
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(NextCostume node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(NextBackdrop node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ClearGraphicEffects node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Show node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(Hide node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(StopAllSounds node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ClearSoundEffects node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteClone node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ResetTimer node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetRotationStyle node) {
        String rotation = node.getRotation().toString();
        String fieldsString = createFields(STYLE_KEY, rotation, null);
        finishedJSONStrings.add(createBlockWithoutMutationString((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(GoToLayer node) {
        String layer = node.getLayerChoice().getTypeName();
        String fieldsString = createFields(FRONT_BACK, layer, null);
        finishedJSONStrings.add(createBlockWithoutMutationString((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(ChangeLayerBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(metadata, NUM_KEY, node.getNum(), INTEGER_NUM_PRIMITIVE));
        String fields = createFields(FORWARD_BACKWARD, node.getForwardBackwardChoice().getTypeName(), null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fields, node.getOpcode()));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(SetDragMode node) {
        String drag = node.getDrag().getTypeName();
        String fieldsString = createFields(DRAGMODE_KEY, drag, null);
        finishedJSONStrings.add(createBlockWithoutMutationString((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(DeleteAllOf node) {
        getListDataFields(node.getIdentifier());
    }

    @Override
    public void visit(ShowList node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    private String getListDataFields(Identifier identifier) {
        Preconditions.checkArgument(identifier instanceof Qualified, "Identifier of list has to be in Qualified");
        Qualified qual = (Qualified) identifier;
        Preconditions.checkArgument(qual.getSecond() instanceof ScratchList, "Qualified has to hold Scratch List");
        ScratchList list = (ScratchList) qual.getSecond();
        String id = symbolTable.getListIdentifierFromActorAndName(qual.getFirst().getName(), list.getName().getName());
        return createFields(LIST_KEY, list.getName().getName(), id);
    }

    @Override
    public void visit(HideList node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ShowVariable node) {
        getVariableFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier(), EMPTY_VALUE, node.getOpcode());
    }

    @Override
    public void visit(HideVariable node) {
        getVariableFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier(), EMPTY_VALUE, node.getOpcode());
    }

    private void getVariableFields(NonDataBlockMetadata metadata, Identifier identifier, String inputsString, Opcode opcode) {
        String fieldsString;
        Preconditions.checkArgument(identifier instanceof Qualified, "Identifier of variable has to be in Qualified");
        Qualified qual = (Qualified) identifier;
        Preconditions.checkArgument(qual.getSecond() instanceof Variable, "Qualified has to hold Variable");
        Variable variable = (Variable) qual.getSecond();
        String id = symbolTable.getVariableIdentifierFromActorAndName(qual.getFirst().getName(),
                variable.getName().getName());
        fieldsString = createFields(VARIABLE_KEY, variable.getName().getName(), id);

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, inputsString, fieldsString, opcode));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(StopAll node) {
        String fieldsString = createFields(STOP_OPTION, "all", null);
        getStopMutation(fieldsString, (NonDataBlockMetadata) node.getMetadata(), node.getOpcode(), false);
    }

    @Override
    public void visit(StopThisScript node) {
        String fieldsString = createFields(STOP_OPTION, "this script", null);
        getStopMutation(fieldsString, (NonDataBlockMetadata) node.getMetadata(), node.getOpcode(), false);
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        String fieldsString = createFields(STOP_OPTION, "other scripts in sprite", null);
        getStopMutation(fieldsString, (NonDataBlockMetadata) node.getMetadata(), node.getOpcode(), true);
    }

    private void getStopMutation(String fieldsString, NonDataBlockMetadata metadata, Opcode opcode, boolean hasNext) {
        String mutationString = createStopMetadata(hasNext);
        finishedJSONStrings.add(createBlockWithMutationString(metadata, getNextId(),
                previousBlockId, EMPTY_VALUE, fieldsString, mutationString, opcode));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        List<String> inputs = new ArrayList<>();
        String insideBlockId = createSubstackJSON(stmtList, metadata);
        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(UntilStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        List<String> inputs = new ArrayList<>();

        createBoolSubstackExpr(metadata, stmtList, inputs, node.getBoolExpr(), node.getOpcode());
    }

    private void createBoolSubstackExpr(NonDataBlockMetadata metadata, StmtList stmtList, List<String> inputs,
                                        BoolExpr boolExpr, Opcode opcode) {

        createBoolExpr(metadata, inputs, boolExpr);
        String insideBlockId = createSubstackJSON(stmtList, metadata);

        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));

        previousBlockId = metadata.getBlockId();
    }

    private void createBoolExpr(NonDataBlockMetadata metadata, List<String> inputs, BoolExpr condition) {
        if (condition instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, CONDITION_KEY, false));
        } else {
            IdJsonStringTuple tuple = exprCreator.createExpressionJSON(metadata.getBlockId(),
                    condition, symbolTable);
            if (tuple.getId() == null) {
                StringBuilder jsonString = new StringBuilder();
                createField(jsonString, CONDITION_KEY).append(tuple.getJsonString());
                inputs.add(jsonString.toString());
            } else {
                finishedJSONStrings.add(tuple.getJsonString());
                inputs.add(createReferenceJSON(tuple.getId(), CONDITION_KEY, false));
            }
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getThenStmts();
        StmtList elseStmtList = node.getElseStmts();
        List<String> inputs = new ArrayList<>();

        BoolExpr condition = node.getBoolExpr();

        createBoolExpr(metadata, inputs, condition);
        String elseInsideBlockId = createSubstackJSON(elseStmtList, metadata);
        String insideBlockId = createSubstackJSON(stmtList, metadata);
        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY, false));
        inputs.add(createReferenceJSON(elseInsideBlockId, SUBSTACK2_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(IfThenStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getThenStmts();
        List<String> inputs = new ArrayList<>();

        createBoolSubstackExpr(metadata, stmtList, inputs, node.getBoolExpr(), node.getOpcode());
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();

        StmtList stmtList = node.getStmtList();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(metadata, TIMES_KEY, node.getTimes(), WHOLE_NUM_PRIMITIVE));

        String insideBlockId = createSubstackJSON(stmtList, metadata);
        inputs.add(createReferenceJSON(insideBlockId, SUBSTACK_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(WaitUntil node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        BoolExpr condition = node.getUntil();

        List<String> inputs = new ArrayList<>();
        if (condition instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, CONDITION_KEY, false));
        } else {
            IdJsonStringTuple tuple = exprCreator.createExpressionJSON(metadata.getBlockId(),
                    condition, symbolTable);
            if (tuple.getId() == null) {
                StringBuilder jsonString = new StringBuilder();
                createField(jsonString, CONDITION_KEY).append(tuple.getJsonString());
                inputs.add(jsonString.toString());
            } else {
                finishedJSONStrings.add(tuple.getJsonString());
                inputs.add(createReferenceJSON(tuple.getId(), CONDITION_KEY, false));
            }
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(WaitSeconds node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, DURATION_KEY, node.getSeconds(), POSITIVE_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(MoveSteps node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, STEPS_KEY, node.getSteps(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(TurnLeft node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, DEGREES_KEY, node.getDegrees(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(TurnRight node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, DEGREES_KEY, node.getDegrees(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(PointInDirection node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, DIRECTION_KEY_CAP, node.getDirection(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(ChangeXBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, DX_KEY, node.getNum(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(ChangeYBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, DY_KEY, node.getNum(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(SetYTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, Y, node.getNum(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(SetXTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, X, node.getNum(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(GoToPosXY node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(metadata, X, node.getX(), MATH_NUM_PRIMITIVE));
        inputs.add(createNumExpr(metadata, Y, node.getY(), MATH_NUM_PRIMITIVE));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(GlideSecsToXY node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(metadata, SECS_KEY, node.getSecs(), MATH_NUM_PRIMITIVE));
        inputs.add(createNumExpr(metadata, X, node.getX(), MATH_NUM_PRIMITIVE));
        inputs.add(createNumExpr(metadata, Y, node.getY(), MATH_NUM_PRIMITIVE));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));

        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ChangeSizeBy node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, CHANGE_KEY, node.getNum(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(SetSizeTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleNumExprBlock(metadata, SIZE_KEY_CAP, node.getPercent(), MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(), EFFECT_KEY,
                node.getEffect().getTypeName(), VALUE_KEY, node.getOpcode());
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(), EFFECT_KEY,
                node.getEffect().getTypeName(), CHANGE_KEY, node.getOpcode());
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(), EFFECT_KEY,
                node.getEffect().getTypeName(), VALUE_KEY, node.getOpcode());
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        createNumExprFieldsBlockJson((NonDataBlockMetadata) node.getMetadata(), node.getValue(), EFFECT_KEY,
                node.getEffect().getTypeName(), VALUE_KEY, node.getOpcode());
    }

    @Override
    public void visit(Say node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleStringExprBlock(metadata, MESSAGE_KEY, node.getString(), node.getOpcode());
    }

    @Override
    public void visit(Think node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleStringExprBlock(metadata, MESSAGE_KEY, node.getThought(), node.getOpcode());
    }

    @Override
    public void visit(AskAndWait node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        createSingleStringExprBlock(metadata, QUESTION_KEY, node.getQuestion(), node.getOpcode());
    }

    @Override
    public void visit(SayForSecs node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, MESSAGE_KEY, node.getString()));
        inputs.add(createNumExpr(metadata, SECS_KEY, node.getSecs(), MATH_NUM_PRIMITIVE));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ThinkForSecs node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, MESSAGE_KEY, node.getThought()));
        inputs.add(createNumExpr(metadata, SECS_KEY, node.getSecs(), MATH_NUM_PRIMITIVE));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(AddTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, ITEM_KEY, node.getString()));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(DeleteOf node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(metadata, INDEX_KEY, node.getNum(), INTEGER_NUM_PRIMITIVE));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(InsertAt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, ITEM_KEY, node.getString()));
        inputs.add(createNumExpr(metadata, INDEX_KEY, node.getIndex(), INTEGER_NUM_PRIMITIVE));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ReplaceItem node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        String fieldsString = getListDataFields(node.getIdentifier());
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(metadata, INDEX_KEY, node.getIndex(), INTEGER_NUM_PRIMITIVE));
        inputs.add(createExpr(metadata, ITEM_KEY, node.getString()));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(GoToPos node) {
        createStatementWithPosition((NonDataBlockMetadata) node.getMetadata(), node.getPosition(), TO_KEY, node.getOpcode(), node.getGoToMenuOpcode());
    }

    @Override
    public void visit(GlideSecsTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(metadata, SECS_KEY, node.getSecs(), MATH_NUM_PRIMITIVE));
        inputs.add(addPositionReference(metadata, node.getPosition(), TO_KEY, node.getGlideToMenuOpcode()));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(PointTowards node) {
        createStatementWithPosition((NonDataBlockMetadata) node.getMetadata(), node.getPosition(), TOWARDS_KEY, node.getOpcode(), node.getPointTowardsMenuOpcode());
    }

    @Override
    public void visit(SwitchCostumeTo node) {
        createStatementWithElementChoice((NonDataBlockMetadata) node.getMetadata(), node.getCostumeChoice(),
                COSTUME_INPUT, node.getOpcode(), node.getCostumeMenuOpcode());
    }

    @Override
    public void visit(SwitchBackdrop node) {
        createStatementWithElementChoice((NonDataBlockMetadata) node.getMetadata(), node.getElementChoice(),
                BACKDROP_INPUT, node.getOpcode(), node.getBackdropMenuOpcode());
    }

    @Override
    public void visit(SwitchBackdropAndWait node) {
        createStatementWithElementChoice((NonDataBlockMetadata) node.getMetadata(), node.getElementChoice(),
                BACKDROP_INPUT, node.getOpcode(), node.getBackdropMenuOpcode());
    }

    @Override
    public void visit(PlaySoundUntilDone node) {
        createStatementWithElementChoice((NonDataBlockMetadata) node.getMetadata(), node.getElementChoice(),
                SOUND_MENU, node.getOpcode(), node.getSoundMenuOpcode());
    }

    @Override
    public void visit(StartSound node) {
        createStatementWithElementChoice((NonDataBlockMetadata) node.getMetadata(), node.getElementChoice(),
                SOUND_MENU, node.getOpcode(), node.getSoundMenuOpcode());
    }

    @Override
    public void visit(CreateCloneOf node) {
        TopNonDataBlockWithParamMetadata metadata = (TopNonDataBlockWithParamMetadata) node.getMetadata();
        NonDataBlockMetadata cloneBlockMetadata = (NonDataBlockMetadata) metadata.getCloneBlockMetadata();
        List<String> inputs = new ArrayList<>();
        StringExpr stringExpr = node.getStringExpr();
        IdJsonStringTuple tuple;

        if (!(metadata.getCloneMenuMetadata() instanceof NoBlockMetadata)) {
            tuple = fixedExprCreator.createFixedExpressionJSON(cloneBlockMetadata.getBlockId(), node, node.getCloneMenuOpcode());
            inputs.add(createReferenceInput(CLONE_OPTION, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
            finishedJSONStrings.add(tuple.getJsonString());
        } else {
            inputs.add(createExpr(cloneBlockMetadata, CLONE_OPTION, stringExpr));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(cloneBlockMetadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));

        previousBlockId = cloneBlockMetadata.getBlockId();
    }

    @Override
    public void visit(Broadcast node) {
        createBroadcastStmt((NonDataBlockMetadata) node.getMetadata(), node.getMessage().getMessage(), node.getOpcode());
    }

    @Override
    public void visit(BroadcastAndWait node) {
        createBroadcastStmt((NonDataBlockMetadata) node.getMetadata(), node.getMessage().getMessage(), node.getOpcode());
    }

    @Override
    public void visit(ExpressionStmt node) {
        IdJsonStringTuple tuple = exprCreator.createExpressionJSON(null,
                node.getExpression(), symbolTable);
        finishedJSONStrings.add(tuple.getJsonString());
        previousBlockId = tuple.getId();
    }

    @Override
    public void visit(ChangeVariableBy node) {
        createVariableWithInputBlock((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier(), node.getExpr(), node.getOpcode());
    }

    @Override
    public void visit(SetVariableTo node) {
        createVariableWithInputBlock((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier(), node.getExpr(), node.getOpcode());
    }

    @Override
    public void visit(CallStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        ProcedureMutationMetadata mutationMetadata = (ProcedureMutationMetadata) metadata.getMutation();
        List<String> argumentIds = new ArrayList<>();

        StrId name = (StrId) node.getIdent();
        List<String> inputs = new ArrayList<>();
        IdJsonStringTuple tuple;
        List<Expression> expressionList = node.getExpressions().getExpressions();

        for (int i = 0; i < expressionList.size(); i++) {
            Expression current = expressionList.get(i);
            String argumentId = name.getName().replace(" ", "_") + "_argument_" + i;
            argumentIds.add(argumentId);

            if (current instanceof UnspecifiedBoolExpr) {
                inputs.add(createReferenceJSON(null, argumentIds.get(i), false));
            } else if (current instanceof BoolExpr) {
                tuple = exprCreator.createExpressionJSON(metadata.getBlockId(),
                        current, symbolTable);

                if (tuple.getId() == null) {
                    StringBuilder jsonString = new StringBuilder();
                    createField(jsonString, argumentId).append(tuple.getJsonString());
                    inputs.add(jsonString.toString());
                } else {
                    finishedJSONStrings.add(tuple.getJsonString());
                    inputs.add(createReferenceJSON(tuple.getId(), argumentId, false));
                }
            } else {
                tuple = exprCreator.createExpressionJSON(metadata.getBlockId(),
                        current, symbolTable);

                if (tuple.getId() == null) {
                    StringBuilder jsonString = new StringBuilder();
                    createField(jsonString, argumentId).append(tuple.getJsonString());
                    inputs.add(jsonString.toString());
                } else {
                    finishedJSONStrings.add(tuple.getJsonString());
                    inputs.add(createReferenceJSON(tuple.getId(), argumentId, true));
                }
            }
        }

        String mutationString = createCallMetadata(name.getName(), argumentIds,
                mutationMetadata.isWarp());
        finishedJSONStrings.add(createBlockWithMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, mutationString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    private void createPenWithParamStmt(NonDataBlockWithParamMetadata metadata, StringExpr stringExpr,
                                        NumExpr numExpr, Stmt node, Opcode opcode, Opcode dependentOpcode) {
        NonDataBlockMetadata penBlockMetadata = (NonDataBlockMetadata) metadata.getPenBlockMetadata();
        List<String> inputs = new ArrayList<>();
        IdJsonStringTuple tuple;

        if (!(metadata.getParamMetadata() instanceof NoBlockMetadata)) {
            tuple = fixedExprCreator.createFixedExpressionJSON(penBlockMetadata.getBlockId(), node, dependentOpcode);
            inputs.add(createReferenceInput(COLOR_PARAM_BIG_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
            finishedJSONStrings.add(tuple.getJsonString());
        } else {
            inputs.add(createExpr(penBlockMetadata, COLOR_PARAM_BIG_KEY, stringExpr));
        }

        inputs.add(createNumExpr(penBlockMetadata, VALUE_KEY, numExpr, MATH_NUM_PRIMITIVE));

        finishedJSONStrings.add(createBlockWithoutMutationString(penBlockMetadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));

        previousBlockId = penBlockMetadata.getBlockId();
    }

    @Override
    public void visit(SetVolumeTo node) {
        createSingleNumExprBlock((NonDataBlockMetadata) node.getMetadata(), VOLUME_KEY_CAPS, node.getVolumeValue(),
                MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        createSingleNumExprBlock((NonDataBlockMetadata) node.getMetadata(), VOLUME_KEY_CAPS, node.getVolumeValue(),
                MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    private void createVariableWithInputBlock(NonDataBlockMetadata metadata, Identifier identifier, Expression expr, Opcode opcode) {
        List<String> inputs = new ArrayList<>();
        if (expr instanceof NumberLiteral) {
            NumberFormat format = DecimalFormat.getInstance(Locale.ROOT);
            format.setGroupingUsed(false);
            format.setMinimumFractionDigits(0);
            inputs.add(createTypeInputWithName(VALUE_KEY, INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE,
                    format.format(((NumberLiteral) expr).getValue())));
        } else {
            inputs.add(createExpr(metadata, VALUE_KEY, expr));
        }
        getVariableFields(metadata, identifier, createInputs(inputs), opcode);
    }

    private void createBroadcastStmt(NonDataBlockMetadata metadata, StringExpr stringExpr, Opcode opcode) {
        List<String> inputs = new ArrayList<>();
        if (stringExpr instanceof StringLiteral) {
            String message = ((StringLiteral) stringExpr).getText();
            String messageId;
            if (symbolTable.getMessage(message).isPresent()) {
                messageId = symbolTable.getMessage(message).get().getIdentifier();
            } else {
                messageId = "unspecified" + message;
            }
            inputs.add(createReferenceTypeInput(BROADCAST_INPUT_KEY, INPUT_SAME_BLOCK_SHADOW, BROADCAST_PRIMITIVE,
                    message, messageId, false));
        } else {
            inputs.add(createExpr(metadata, BROADCAST_INPUT_KEY, stringExpr));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));
        previousBlockId = metadata.getBlockId();
    }

    private void createStatementWithElementChoice(NonDataBlockMetadata metadata, ElementChoice elem, String inputName, Opcode opcode, Opcode dependentOpcode) {
        List<String> inputs = new ArrayList<>();
        inputs.add(addElementChoiceReference(metadata, elem, inputName, dependentOpcode));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));
        previousBlockId = metadata.getBlockId();
    }

    private String addElementChoiceReference(NonDataBlockMetadata metadata, ElementChoice elem,
                                             String inputName, Opcode dependentOpcode) {
        IdJsonStringTuple tuple;
        if (elem instanceof Prev || elem instanceof Next || elem instanceof Random) {
            tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), elem, dependentOpcode);
            finishedJSONStrings.add(tuple.getJsonString());
            return createReferenceInput(inputName, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false);
        } else {
            WithExpr withExpr = (WithExpr) elem;
            //if metadata are NoBlockMetadata the WithExpr is simply a wrapper of another block
            if (withExpr.getMetadata() instanceof NoBlockMetadata) {
                return createExpr(metadata, inputName, withExpr.getExpression());
            } else {
                tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), elem, dependentOpcode);
                finishedJSONStrings.add(tuple.getJsonString());
                return createReferenceInput(inputName, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false);
            }
        }
    }

    private void createStatementWithPosition(NonDataBlockMetadata metadata, Position position, String inputName, Opcode opcode, Opcode dependentOpcode) {
        List<String> inputs = new ArrayList<>();
        inputs.add(addPositionReference(metadata, position, inputName, dependentOpcode));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));
        previousBlockId = metadata.getBlockId();
    }

    private String addPositionReference(NonDataBlockMetadata metadata, Position pos,
                                        String inputName, Opcode dependentOpcode) {
        IdJsonStringTuple tuple;

        if (pos instanceof RandomPos || pos instanceof MousePos) {
            tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), pos, dependentOpcode);
            finishedJSONStrings.add(tuple.getJsonString());
            return createReferenceInput(inputName, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false);
        } else {
            FromExpression fromPos = (FromExpression) pos;

            //if metadata are NoBlockMetadata the FromExpression is simply a wrapper of
            // another block
            if (fromPos.getMetadata() instanceof NoBlockMetadata) {
                return createExpr(metadata, inputName, fromPos.getStringExpr());
            } else {
                tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), pos, dependentOpcode);
                finishedJSONStrings.add(tuple.getJsonString());
                return createReferenceInput(inputName, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false);
            }
        }
    }

    private void createNumExprFieldsBlockJson(NonDataBlockMetadata metadata, NumExpr value, String fieldsName, String fieldsValue,
                                              String inputName, Opcode opcode) {
        List<String> inputs = new ArrayList<>();
        inputs.add(createNumExpr(metadata, inputName, value, MATH_NUM_PRIMITIVE));
        String fields = createFields(fieldsName, fieldsValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), fields, opcode));

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

    private void createSingleNumExprBlock(NonDataBlockMetadata metadata, String inputKey, NumExpr numExpr,
                                          int primitive, Opcode opcode) {
        List<String> inputs = new ArrayList<>();

        inputs.add(createNumExpr(metadata, inputKey, numExpr, primitive));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));

        previousBlockId = metadata.getBlockId();
    }

    private void createSingleStringExprBlock(NonDataBlockMetadata metadata, String inputKey, StringExpr stringExpr, Opcode opcode) {
        List<String> inputs = new ArrayList<>();

        inputs.add(createExpr(metadata, inputKey, stringExpr));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));

        previousBlockId = metadata.getBlockId();
    }

    private String createNumExpr(NonDataBlockMetadata metadata, String inputKey, NumExpr numExpr, int primitive) {
        if (numExpr instanceof NumberLiteral) {
            NumberFormat format = DecimalFormat.getInstance(Locale.ROOT);
            format.setGroupingUsed(false);
            format.setMinimumFractionDigits(0);
            return createTypeInputWithName(inputKey, INPUT_SAME_BLOCK_SHADOW, primitive,
                    format.format(((NumberLiteral) numExpr).getValue()));
        } else {
            return createExpr(metadata, inputKey, numExpr);
        }
    }

    private String createExpr(NonDataBlockMetadata metadata, String inputKey, Expression expr) {
        IdJsonStringTuple tuple = exprCreator.createExpressionJSON(metadata.getBlockId(),
                expr, symbolTable);
        if (tuple.getId() == null) {
            StringBuilder jsonString = new StringBuilder();
            createField(jsonString, inputKey).append(tuple.getJsonString());
            return jsonString.toString();
        } else {
            finishedJSONStrings.add(tuple.getJsonString());
            return createReferenceJSON(tuple.getId(), inputKey, true);
        }
    }

    @Override
    public void visit(PenStmt node) {
        node.accept((PenExtensionVisitor) this);
    }

    @Override
    public void visit(SetPenSizeTo node) {
        createSingleNumExprBlock((NonDataBlockMetadata) node.getMetadata(), SIZE_KEY_CAP, node.getValue(),
                MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        createSingleNumExprBlock((NonDataBlockMetadata) node.getMetadata(), SIZE_KEY_CAP, node.getValue(),
                MATH_NUM_PRIMITIVE, node.getOpcode());
    }

    @Override
    public void visit(PenDownStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenUpStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenClearStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(PenStampStmt node) {
        finishedJSONStrings.add(createFixedBlock((NonDataBlockMetadata) node.getMetadata(), getNextId(),
                previousBlockId, node.getOpcode()));
        previousBlockId = ((NonDataBlockMetadata) node.getMetadata()).getBlockId();
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();
        Color color = node.getColorExpr();
        IdJsonStringTuple tuple;

        tuple = exprCreator.createExpressionJSON(metadata.getBlockId(),
                color, symbolTable);
        if (tuple.getId() == null) {
            StringBuilder jsonString = new StringBuilder();
            createField(jsonString, COLOR_KEY).append(tuple.getJsonString());
            inputs.add(jsonString.toString());
        } else {
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceJSON(tuple.getId(), COLOR_KEY, true));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        NonDataBlockWithParamMetadata metadata = (NonDataBlockWithParamMetadata) node.getMetadata();
        createPenWithParamStmt(metadata, node.getParam(), node.getValue(), node, node.getOpcode(), node.getMenuColorParamOpcode());
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        NonDataBlockWithParamMetadata metadata = (NonDataBlockWithParamMetadata) node.getMetadata();
        createPenWithParamStmt(metadata, node.getParam(), node.getValue(), node, node.getOpcode(), node.getMenuColorParamOpcode());
    }

    @Override
    public void visitParentVisitor(PenStmt node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visitParentVisitor(TextToSpeechBlock node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(TextToSpeechBlock node) {
        node.accept((TextToSpeechExtensionVisitor) this);
    }

    @Override
    public void visit(SetLanguage node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();
        if (node.getLanguage() instanceof FixedLanguage) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), node.getLanguage(), node.getMenuLanguageOpcode());
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceInput(LANGUAGE_INPUT_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        } else {
            inputs.add(createExpr(metadata, LANGUAGE_INPUT_KEY, ((ExprLanguage) node.getLanguage()).getExpr()));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(SetVoice node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        List<String> inputs = new ArrayList<>();
        if (node.getVoice() instanceof FixedVoice) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), node.getVoice(), node.getMenuVoiceOpcode());
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceInput(VOICE_INPUT_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        } else {
            inputs.add(createExpr(metadata, VOICE_INPUT_KEY, ((ExprVoice) node.getVoice()).getExpr()));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, getNextId(),
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Speak node) {
        createSingleStringExprBlock((NonDataBlockMetadata) node.getMetadata(), WORDS_KEY, node.getText(), node.getOpcode());
    }
}

