/*
 * Copyright (C) 2019-2026 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.Attribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.ExprDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.ExprInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.ExprNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.TranslateTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.ViewerLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.translate.tlanguage.TFixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.video_sensing.VideoOn;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.*;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.SymbolTable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TextToSpeechExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.TranslateExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.VideoSensingExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.BlockJsonCreatorHelper.*;
import static de.uni_passau.fim.se2.litterbox.jsoncreation.JSONStringCreator.createField;

public class ExpressionJSONCreator
        implements MusicExtensionVisitor,
        TextToSpeechExtensionVisitor,
        TranslateExtensionVisitor,
        VideoSensingExtensionVisitor {
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
        // Filter out empty/null strings to prevent double-comma JSON corruption
        List<String> validJsonStrings = finishedJSONStrings.stream()
                .filter(s -> s != null && !s.isEmpty())
                .toList();

        for (int i = 0; i < validJsonStrings.size() - 1; i++) {
            jsonString.append(validJsonStrings.get(i)).append(",");
        }
        if (!validJsonStrings.isEmpty()) {
            jsonString.append(validJsonStrings.getLast());
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
    public void visit(UnspecifiedExpression node) {
        // Generic unspecified expression - use empty text as fallback
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
        final String number = BlockJsonCreatorHelper.formatNumberLiteral(node);
        finishedJSONStrings.add(createTypeInput(INPUT_SAME_BLOCK_SHADOW, MATH_NUM_PRIMITIVE, number));
    }

    @Override
    public void visit(ColorLiteral node) {
        final String colorString = String.format("#%02x%02x%02x", node.getRed(), node.getGreen(), node.getBlue());
        final String input = createTypeInput(INPUT_SAME_BLOCK_SHADOW, COLOR_PICKER_PRIMITIVE, colorString);

        finishedJSONStrings.add(input);
    }

    private void createSimpleExpression(BlockMetadata metadata, Opcode opcode) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null, previousBlockId, EMPTY_VALUE,
                EMPTY_VALUE, opcode));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Answer node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(MouseX node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(MouseY node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(PositionX node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(PositionY node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Direction node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Size node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Volume node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(IsMouseDown node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Loudness node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Timer node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(DaysSince2000 node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Username node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(Costume node) {
        createFieldsExpression(
                node.getMetadata(),
                NUMBER_NAME_KEY,
                node.getType().getTypeName(),
                node.getOpcode()
        );
    }

    @Override
    public void visit(Backdrop node) {
        createFieldsExpression(
                node.getMetadata(),
                NUMBER_NAME_KEY,
                node.getType().getTypeName(),
                node.getOpcode()
        );
    }

    @Override
    public void visit(Current node) {
        createFieldsExpression(
                node.getMetadata(),
                CURRENT_YEAR_FIELD,
                node.getTimeComp().getTypeName(),
                node.getOpcode()
        );
    }

    private void createFieldsExpression(
            BlockMetadata metadata, String fieldName, String fieldValue, Opcode opcode
    ) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }

        String fieldsString = createFields(fieldName, fieldValue, null);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString, opcode));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(LengthOfVar node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        String fieldsString = getListDataFields(node.getIdentifier());
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
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
    public void visit(Add node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY, node.getOpcode());
    }

    @Override
    public void visit(Minus node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY, node.getOpcode());
    }

    @Override
    public void visit(Mult node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY, node.getOpcode());
    }

    @Override
    public void visit(Div node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY, node.getOpcode());
    }

    @Override
    public void visit(LessThan node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                OPERAND1_KEY,
                OPERAND2_KEY, node.getOpcode());
    }

    @Override
    public void visit(BiggerThan node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                OPERAND1_KEY,
                OPERAND2_KEY, node.getOpcode());
    }

    @Override
    public void visit(Equals node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                OPERAND1_KEY,
                OPERAND2_KEY, node.getOpcode());
    }

    @Override
    public void visit(PickRandom node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                FROM_KEY,
                TO_KEY, node.getOpcode());
    }

    @Override
    public void visit(Join node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                STRING1_KEY,
                STRING2_KEY, node.getOpcode());
    }

    @Override
    public void visit(LetterOf node) {
        mathStringOperations(node.getMetadata(), node.getNum(), node.getStringExpr(), LETTER_KEY,
                STRING_KEY, node.getOpcode());
    }

    @Override
    public void visit(Mod node) {
        mathStringOperations(node.getMetadata(), node.getOperand1(), node.getOperand2(),
                NUM1_KEY,
                NUM2_KEY, node.getOpcode());
    }

    @Override
    public void visit(StringContains node) {
        mathStringOperations(node.getMetadata(), node.getContaining(), node.getContained(),
                STRING1_KEY, STRING2_KEY, node.getOpcode());
    }

    @Override
    public void visit(Or node) {
        boolOperations(
                node.getMetadata(), node.getOperand1(), node.getOperand2(), node.getOpcode()
        );
    }

    @Override
    public void visit(And node) {
        boolOperations(
                node.getMetadata(), node.getOperand1(), node.getOperand2(), node.getOpcode()
        );
    }

    @Override
    public void visit(Round node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getOperand1(), NUM_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(LengthOfString node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getStringExpr(), STRING_KEY, false));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Not node) {
        BlockMetadata metadata = node.getMetadata();
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
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Touching node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable touchable = node.getTouchable();
        if (touchable instanceof SpriteTouchable || touchable instanceof Edge || touchable instanceof MousePointer) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(
                    metadata.getBlockId(), touchable, node.getTouchingObjectMenuOpcode()
            );
            finishedJSONStrings.add(tuple.jsonString());
            inputs.add(createReferenceInput(TOUCHINGOBJECTMENU, INPUT_SAME_BLOCK_SHADOW, tuple.id(), false));
        } else {
            AsTouchable asTouchable = (AsTouchable) touchable;
            inputs.add(createExpr(metadata, asTouchable.getOperand1(), TOUCHINGOBJECTMENU, true));
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(DistanceTo node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Position pos = node.getPosition();
        if (pos instanceof MousePos) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(
                    metadata.getBlockId(), pos, node.getDistanceToMenuOpcode()
            );
            finishedJSONStrings.add(tuple.jsonString());
            inputs.add(createReferenceInput(DISTANCETOMENU_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.id(), false));
        } else {
            FromExpression fromPos = (FromExpression) pos;
            IdJsonStringTuple tuple;

            if (fromPos.getMetadata() instanceof NoBlockMetadata) {
                inputs.add(createExpr(metadata, fromPos.getStringExpr(), DISTANCETOMENU_KEY, true));
            } else {
                tuple = fixedExprCreator.createFixedExpressionJSON(
                        metadata.getBlockId(), pos, node.getDistanceToMenuOpcode()
                );
                finishedJSONStrings.add(tuple.jsonString());
                inputs.add(createReferenceInput(DISTANCETOMENU_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.id(), false));
            }
        }
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(AttributeOf node) {
        BlockMetadata metadata = node.getMetadata();
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
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(
                    metadata.getBlockId(), elem, node.getSensingOfObjectMenuOpcode()
            );
            finishedJSONStrings.add(tuple.jsonString());
            inputs.add(createReferenceInput(OBJECT_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.id(), false));
        }

        String fieldValue;
        Attribute attr = node.getAttribute();
        if (attr instanceof AttributeFromFixed attributeFromFixed) {
            fieldValue = attributeFromFixed.getAttribute().getTypeName();
        } else {
            fieldValue = ((AttributeFromVariable) attr).getVariable().getName().getName();
        }
        String fieldsString = createFields(PROPERTY_FIELDS_KEY, fieldValue, null);

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(NumFunctOf node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getOperand2(), NUM_KEY, true));

        String fieldsString = createFields(OPERATOR_KEY, node.getOperand1().getTypeName(), null);

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(IsKeyPressed node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Key key = node.getKey();
        if (key.getMetadata() instanceof NoBlockMetadata) {
            inputs.add(createExpr(metadata, key.getKey(), KEY_OPTION, true));
        } else {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(
                    metadata.getBlockId(), key, node.getKeyOptionsOpcode()
            );
            finishedJSONStrings.add(tuple.jsonString());
            inputs.add(createReferenceInput(KEY_OPTION, INPUT_SAME_BLOCK_SHADOW, tuple.id(), false));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ItemOfVariable node) {
        createListBlockWithExpr(node.getMetadata(), node.getNum(), INDEX_KEY,
                node.getIdentifier(), node.getOpcode());
    }

    @Override
    public void visit(IndexOf node) {
        createListBlockWithExpr(node.getMetadata(), node.getExpr(), ITEM_KEY,
                node.getIdentifier(), node.getOpcode());
    }

    @Override
    public void visit(ListContains node) {
        createListBlockWithExpr(node.getMetadata(), node.getElement(), ITEM_KEY,
                node.getIdentifier(), node.getOpcode());
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable color = node.getColor();
        inputs.add(createExpr(metadata, color, COLOR_KEY, true));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ColorTouchingColor node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        Touchable color = node.getOperand1();
        Touchable color2 = node.getOperand2();
        inputs.add(createExpr(metadata, color, COLOR_KEY, true));
        inputs.add(createExpr(metadata, color2, COLOR2_KEY, true));
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(Qualified node) {
        String actor = node.getFirst().getName();
        StringBuilder jsonString = new StringBuilder();
        if (node.getSecond() instanceof ScratchList list) {
            String listName = list.getName().getName();
            String listId = symbolTable.getListIdentifierFromActorAndName(actor, listName);
            BlockMetadata metadata = list.getMetadata();
            if (metadata instanceof DataBlockMetadata dataBlockMetadata) {
                createField(jsonString, dataBlockMetadata.getBlockId()).append("[").append(LIST_PRIMITIVE);
                jsonString.append(",\"").append(BlockJsonCreatorHelper.escape(listName)).append("\",\"");
                jsonString.append(BlockJsonCreatorHelper.escape(listId));
                if (dataBlockMetadata.getCommentId() != null) {
                    jsonString.append("\",\"")
                            .append(dataBlockMetadata.getCommentId())
                            .append("\",");
                } else {
                    jsonString.append("\",");
                }
                jsonString.append(dataBlockMetadata.getX())
                        .append(",")
                        .append(dataBlockMetadata.getY())
                        .append("]");
            } else {
                jsonString.append(
                        createReferenceType(
                                INPUT_DIFF_BLOCK_SHADOW,
                                LIST_PRIMITIVE,
                                BlockJsonCreatorHelper.escape(listName),
                                BlockJsonCreatorHelper.escape(listId),
                                true
                        )
                );
            }
        } else if (node.getSecond() instanceof Variable variable) {
            String variableName = variable.getName().getName();
            String variableId = symbolTable.getVariableIdentifierFromActorAndName(actor, variableName);
            BlockMetadata metadata = variable.getMetadata();
            if (metadata instanceof DataBlockMetadata dataBlockMetadata) {
                createField(jsonString, dataBlockMetadata.getBlockId()).append("[").append(VAR_PRIMITIVE);
                jsonString.append(",\"").append(BlockJsonCreatorHelper.escape(variableName)).append("\",\"");
                jsonString.append(BlockJsonCreatorHelper.escape(variableId));
                if (dataBlockMetadata.getCommentId() != null) {
                    jsonString.append("\",\"")
                            .append(dataBlockMetadata.getCommentId())
                            .append("\",");
                } else {
                    jsonString.append("\",");
                }
                jsonString.append(dataBlockMetadata.getX())
                        .append(",")
                        .append(dataBlockMetadata.getY())
                        .append("]");
            } else {
                jsonString.append(
                        createReferenceType(
                                INPUT_DIFF_BLOCK_SHADOW,
                                VAR_PRIMITIVE,
                                BlockJsonCreatorHelper.escape(variableName),
                                BlockJsonCreatorHelper.escape(variableId),
                                true
                        )
                );
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
        createFieldsExpression(
                node.getMetadata(), VALUE_KEY, node.getName().getName(), node.getOpcode()
        );
    }

    private void createListBlockWithExpr(BlockMetadata metadata, Expression expr, String inputName,
                                         Identifier identifier, Opcode opcode) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, expr, inputName, true));
        String fieldsString = getListDataFields(identifier);
        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), fieldsString, opcode));
        previousBlockId = metadata.getBlockId();
    }

    private void boolOperations(BlockMetadata metadata, BoolExpr expr1, BoolExpr expr2, Opcode opcode) {
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
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));
        previousBlockId = metadata.getBlockId();
    }

    private void mathStringOperations(BlockMetadata metadata, Expression expr1, Expression expr2,
                                      String inputName1,
                                      String inputName2, Opcode opcode) {
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();

        inputs.add(createExpr(metadata, expr1, inputName1, true));
        inputs.add(createExpr(metadata, expr2, inputName2, true));

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, opcode));
        previousBlockId = metadata.getBlockId();
    }

    private String createExpr(BlockMetadata metadata, ASTNode expr, String inputName, boolean withDefault) {
        IdJsonStringTuple tuple = expressionJSONCreator.createExpressionJSON(metadata.getBlockId(), expr, symbolTable);
        if (tuple.id() == null) {
            StringBuilder jsonString = new StringBuilder();
            createField(jsonString, inputName).append(tuple.jsonString());
            return jsonString.toString();
        } else {
            finishedJSONStrings.add(tuple.jsonString());
            return createReferenceJSON(tuple.id(), inputName, withDefault);
        }
    }

    @Override
    public void visit(ExprLanguage node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ExprVoice node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(VideoOn node) {
        BlockMetadata metadata = node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }

        // Build a single fields object with both fields to avoid invalid JSON like {...},{...}
        String fieldsString = "{"
                + "\"" + ATTRIBUTE_KEY + "\":[\"" + escape(node.getAttribute()) + "\",null],"
                + "\"" + SUBJECT_KEY + "\":[\"" + escape(node.getSubject()) + "\",null]"
                + "}";

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, EMPTY_VALUE, fieldsString, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    // Music Extension

    @Override
    public void visit(FixedNote node) {
        fixedExprCreator.visit(node);
        addExpressionJSONString(fixedExprCreator.getFinishedJSONStrings().getFirst(), node.getMetadata().getBlockId());
    }

    @Override
    public void visit(FixedDrum node) {
        fixedExprCreator.visit(node);
        addExpressionJSONString(fixedExprCreator.getFinishedJSONStrings().getFirst(), node.getMetadata().getBlockId());
    }

    @Override
    public void visit(FixedInstrument node) {
        fixedExprCreator.visit(node);
        addExpressionJSONString(fixedExprCreator.getFinishedJSONStrings().getFirst(), node.getMetadata().getBlockId());
    }

    @Override
    public void visit(ExprNote node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ExprDrum node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ExprInstrument node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(Tempo node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    // Translate Extension

    @Override
    public void visit(TranslateTo node) {
        NonDataBlockMetadata metadata = (NonDataBlockMetadata) node.getMetadata();
        if (topExpressionId == null) {
            topExpressionId = metadata.getBlockId();
        }
        List<String> inputs = new ArrayList<>();
        inputs.add(createExpr(metadata, node.getText(), WORDS_KEY, true));

        if (node.getLanguage() instanceof TFixedLanguage) {
            IdJsonStringTuple tuple = fixedExprCreator.createFixedExpressionJSON(
                    metadata.getBlockId(), node.getLanguage(), node.getMenuLanguageOpcode()
            );
            finishedJSONStrings.add(tuple.jsonString());
            inputs.add(createReferenceInput(LANGUAGE_INPUT_KEY, INPUT_SAME_BLOCK_SHADOW, tuple.id(), false));
        } else {
            inputs.add(createExpr(metadata, node.getLanguage(), LANGUAGE_INPUT_KEY, true));
        }

        finishedJSONStrings.add(createBlockWithoutMutationString(metadata, null,
                previousBlockId, createInputs(inputs), EMPTY_VALUE, node.getOpcode()));
        previousBlockId = metadata.getBlockId();
    }

    @Override
    public void visit(ViewerLanguage node) {
        createSimpleExpression(node.getMetadata(), node.getOpcode());
    }

    @Override
    public void visit(TFixedLanguage node) {
        fixedExprCreator.visit(node);
        addExpressionJSONString(fixedExprCreator.getFinishedJSONStrings().getFirst(), node.getMetadata().getBlockId());
    }

    @Override
    public void visit(TExprLanguage node) {
        node.getExpr().accept(this);
    }

    private void addExpressionJSONString(String jsonString, String id) {
        finishedJSONStrings.add(jsonString);
        if (topExpressionId == null) {
            topExpressionId = id;
        }
        previousBlockId = id;
    }
}
