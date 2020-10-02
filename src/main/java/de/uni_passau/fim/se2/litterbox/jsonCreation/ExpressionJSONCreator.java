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
package de.uni_passau.fim.se2.litterbox.jsonCreation;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.*;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper.*;
import static de.uni_passau.fim.se2.litterbox.jsonCreation.JSONStringCreator.createField;

public class ExpressionJSONCreator implements ScratchVisitor {
    private List<String> finishedJSONStrings;
    private String previousBlockId = null;
    private String topExpressionId = null;
    private SymbolTable symbolTable;
    private ExpressionJSONCreator expressionJSONCreator;
    private FixedExpressionJSONCreator fixedExprCreator;

    public IdJsonStringTuple createExpressionJSON(String parentId, ASTNode expression, SymbolTable symbolTable) {
        finishedJSONStrings = new ArrayList<>();
        topExpressionId = null;
        previousBlockId = parentId;
        this.symbolTable = symbolTable;
        expressionJSONCreator = new ExpressionJSONCreator();
        fixedExprCreator = new FixedExpressionJSONCreator();
        expression.accept(this);
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; i < finishedJSONStrings.size() - 1; i++) {
            jsonString.append(finishedJSONStrings.get(i)).append(",");
        }
        if (finishedJSONStrings.size() > 0) {
            jsonString.append(finishedJSONStrings.get(finishedJSONStrings.size() - 1));
        }
        return new IdJsonStringTuple(topExpressionId, jsonString.toString());
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE, ""));
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, ""));
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE, ""));
    }

    @Override
    public void visit(AsString node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(AsNumber node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(AsBool node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(FromNumber node) {
        node.getValue().accept(this);
    }

    @Override
    public void visit(BoolLiteral node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE,
                String.valueOf(node.getValue())));
    }

    @Override
    public void visit(StringLiteral node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, TEXT_PRIMITIVE,
                node.getText()));
    }

    @Override
    public void visit(NumberLiteral node) {
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE,
                String.valueOf((float) node.getValue())));
    }

    @Override
    public void visit(ColorLiteral node) {
        StringBuilder colorString = new StringBuilder();
        colorString.append("#").append(String.format("0x%02x", node.getRed()).substring(2)).append(String.format("0x"
                + "%02X", node.getGreen()).substring(2)).append(String.format("0x%02X",
                node.getBlue()).substring(2));
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, COLOR_PICKER_PRIMITIVE,
                colorString.toString()));
    }

    private void createSimpleExpression(NonDataBlockMetadata metadata) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null, previousBlockId, EMPTY_VALUE,
                EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Answer node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(MouseX node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(MouseY node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(PositionX node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(PositionY node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Direction node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Size node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Volume node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(IsMouseDown node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Loudness node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Timer node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(DaysSince2000 node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Username node) {
        createSimpleExpression((NonDataBlockMetadata) node.getMetadata());
    }

    @Override
    public void visit(Costume node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getType().getTypeName());
    }

    @Override
    public void visit(Backdrop node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getType().getTypeName());
    }

    @Override
    public void visit(Current node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getTimeComp().getTypeName());
    }

    private void createFieldsExpression(NonDataBlockMetadata metadata, String fieldValue) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), fieldValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(LengthOfVar node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        String fieldsString = getListDataFields((NonDataBlockMetadata) node.getMetadata(), node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
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
    public void visit(Add node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(Minus node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(Mult node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(Div node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(LessThan node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                OPERAND1_KEY,
                OPERAND2_KEY);
    }

    @Override
    public void visit(BiggerThan node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                OPERAND1_KEY,
                OPERAND2_KEY);
    }

    @Override
    public void visit(Equals node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                OPERAND1_KEY,
                OPERAND2_KEY);
    }

    @Override
    public void visit(PickRandom node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                FROM_KEY,
                TO_KEY);
    }

    @Override
    public void visit(Join node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                STRING1_KEY,
                STRING2_KEY);
    }

    @Override
    public void visit(LetterOf node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getNum(), node.getStringExpr(), LETTER_KEY,
                STRING_KEY);
    }

    @Override
    public void visit(Mod node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY);
    }

    @Override
    public void visit(StringContains node) {
        mathStringOperations((NonDataBlockMetadata) node.getMetadata(), node.getContaining(), node.getContained(),
                STRING1_KEY, STRING2_KEY);
    }

    @Override
    public void visit(Or node) {
        boolOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2());
    }

    @Override
    public void visit(And node) {
        boolOperations((NonDataBlockMetadata) node.getMetadata(), node.getOperand1(), node.getOperand2());
    }

    @Override
    public void visit(Round node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getOperand1(), NUM_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(LengthOfString node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getStringExpr(), STRING_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Not node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        if (node.getOperand1() instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, OPERAND_KEY, false));
        } else {
            inputs.add(createExpr(metadata, node.getOperand1(), OPERAND_KEY, false));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Touching node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable touchable = node.getTouchable();
        if (touchable instanceof SpriteTouchable || touchable instanceof Edge || touchable instanceof MousePointer) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), touchable);
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceInput(TOUCHINGOBJECTMENU, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        } else {
            AsTouchable asTouchable = (AsTouchable) touchable;
            inputs.add(createExpr(metadata, asTouchable.getOperand1(), TOUCHINGOBJECTMENU, true));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(DistanceTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Position pos = node.getPosition();
        if (pos instanceof MousePos) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), pos);
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceInput(DISTANCETOMENU_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        } else {
            FromExpression fromPos = (FromExpression) pos;
            IdJsonStringTuple tuple;

            if (fromPos.getMetadata() instanceof NoBlockMetadata) {
                inputs.add(createExpr(metadata, fromPos.getStringExpr(), DISTANCETOMENU_KEY, true));
            } else {
                tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), pos);
                finishedJSONStrings.add(tuple.getJsonString());
                inputs.add(createReferenceInput(DISTANCETOMENU_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
            }
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(AttributeOf node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        ElementChoice elem = node.getElementChoice();
        WithExpr withExpr = (WithExpr) elem;
        //if metadata are NoBlockMetadata the WithExpr is simply a wrapper of another block
        if (withExpr.getMetadata() instanceof NoBlockMetadata) {
            inputs.add(createExpr(metadata, withExpr.getExpression(), OBJECT_KEY, true));
        } else {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), elem);
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceInput(OBJECT_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        }

        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        String fieldValue;
        Attribute attr = node.getAttribute();
        if (attr instanceof AttributeFromFixed) {
            fieldValue = ((AttributeFromFixed) attr).getAttribute().getTypeName();
        } else {
            fieldValue = ((AttributeFromVariable) attr).getVariable().getName().getName();
        }
        String fieldsString = createFields(fieldsMeta.getFieldsName(), fieldValue, null);

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(NumFunctOf node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getOperand2(), NUM_KEY, true));

        FieldsMetadata fieldsMeta = metadata.getFields().getList().get(0);
        String fieldsString = createFields(fieldsMeta.getFieldsName(), node.getOperand1().getTypeName(), null);

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(IsKeyPressed node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Key key = node.getKey();
        if (key.getMetadata() instanceof NoBlockMetadata) {
            inputs.add(createExpr(metadata, key.getKey(), KEY_OPTION, true));
        } else {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(metadata.getBlockId(), key);
            finishedJSONStrings.add(tuple.getJsonString());
            inputs.add(createReferenceInput(KEY_OPTION, INPUT_SAME_BLOCK_SHADOW, tuple.getId(), false));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ItemOfVariable node) {
        createListBlockWithExpr((NonDataBlockMetadata) node.getMetadata(), node.getNum(), INDEX_KEY,
                node.getIdentifier());
    }

    @Override
    public void visit(IndexOf node) {
        createListBlockWithExpr((NonDataBlockMetadata) node.getMetadata(), node.getExpr(), ITEM_KEY,
                node.getIdentifier());
    }

    @Override
    public void visit(ListContains node) {
        createListBlockWithExpr((NonDataBlockMetadata) node.getMetadata(), node.getElement(), ITEM_KEY,
                node.getIdentifier());
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable color = node.getColor();
        inputs.add(createExpr(metadata, color, COLOR_KEY, true));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ColorTouchingColor node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable color = node.getOperand1();
        Touchable color2 = node.getOperand2();
        inputs.add(createExpr(metadata, color, COLOR_KEY, true));
        inputs.add(createExpr(metadata, color2, COLOR2_KEY, true));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Qualified node) {
        String actor = node.getFirst().getName();
        StringBuilder jsonString = new StringBuilder();
        if (node.getSecond() instanceof ScratchList) {
            ScratchList list = (ScratchList) node.getSecond();
            String listName = list.getName().getName();
            String listId = symbolTable.getListIdentifierFromActorAndName(actor, listName);
            BlockMetadata metadata = list.getMetadata();
            if (metadata instanceof DataBlockMetadata) {
                DataBlockMetadata dataBlockMetadata = (DataBlockMetadata) metadata;
                createField(jsonString, dataBlockMetadata.getBlockId()).append("[").append(LIST_PRIMITIVE);
                jsonString.append(",\"").append(listName).append("\",\"");
                jsonString.append(listId)
                        .append("\",")
                        .append(dataBlockMetadata.getX())
                        .append(",")
                        .append(dataBlockMetadata.getY())
                        .append("]");
            } else {
                jsonString.append(createReferenceType(INPUT_DIFF_BLOCK_SHADOW, LIST_PRIMITIVE, listName, listId,
                        true));
            }
        } else if (node.getSecond() instanceof Variable) {
            Variable variable = (Variable) node.getSecond();
            String variableName = variable.getName().getName();
            String variableId = symbolTable.getVariableIdentifierFromActorAndName(actor, variableName);
            BlockMetadata metadata = variable.getMetadata();
            if (metadata instanceof DataBlockMetadata) {
                DataBlockMetadata dataBlockMetadata = (DataBlockMetadata) metadata;
                createField(jsonString, dataBlockMetadata.getBlockId()).append("[").append(VAR_PRIMITIVE);
                jsonString.append(",\"").append(variableName).append("\",\"");
                jsonString.append(variableId)
                        .append("\",")
                        .append(dataBlockMetadata.getX())
                        .append(",")
                        .append(dataBlockMetadata.getY())
                        .append("]");
            } else {
                jsonString.append(createReferenceType(INPUT_DIFF_BLOCK_SHADOW, VAR_PRIMITIVE, variableName, variableId,
                        true));
            }
        }
        finishedJSONStrings.add(jsonString.toString());
    }

    @Override
    public void visit(AsTouchable node) {
        node.getOperand1().accept(this);
    }

    @Override
    public void visit(Parameter node) {
        createFieldsExpression((NonDataBlockMetadata) node.getMetadata(), node.getName().getName());
    }

    private void createListBlockWithExpr(NonDataBlockMetadata metadata, Expression expr, String inputName,
                                         Identifier identifier) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, expr, inputName, true));
        String fieldsString = getListDataFields(metadata, identifier);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), fieldsString));
        previousBlockId = metadata.getBlockId();
    }

    private void boolOperations(NonDataBlockMetadata metadata, BoolExpr expr1, BoolExpr expr2) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        if (expr1 instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, OPERAND1_KEY, false));
        } else {
            inputs.add(createExpr(metadata, expr1, OPERAND1_KEY, false));
        }

        if (expr2 instanceof UnspecifiedBoolExpr) {
            inputs.add(createReferenceJSON(null, OPERAND2_KEY, false));
        } else {
            inputs.add(createExpr(metadata, expr2, OPERAND2_KEY, false));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    private void mathStringOperations(NonDataBlockMetadata metadata, Expression expr1, Expression expr2,
                                      String inputName1,
                                      String inputName2) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();

        inputs.add(createExpr(metadata, expr1, inputName1, true));
        inputs.add(createExpr(metadata, expr2, inputName2, true));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE));
        previousBlockId = metadata.getBlockId();
    }

    private String createExpr(NonDataBlockMetadata metadata, ASTNode expr, String inputName, boolean withDefault) {
        IdJsonStringTuple tuple = expressionJSONCreator.createExpressionJSON(metadata.getBlockId(), expr, symbolTable);
        if (tuple.getId() == null) {
            StringBuilder jsonString = new StringBuilder();
            createField(jsonString, inputName).append(tuple.getJsonString());
            return jsonString.toString();
        } else {
            finishedJSONStrings.add(tuple.getJsonString());
            return createReferenceJSON(tuple.getId(), inputName, withDefault);
        }
    }
}
