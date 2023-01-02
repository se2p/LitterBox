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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class BoolExprParser {
    private static final String ORIENTATE_CAP = "ORIENTATE";
    private static final String LINEFOLLOW_STATE_KEY = "LINEFOLLOW_STATE";
    private static final String BLACK_WHITE_KEY = "BLACK_WHITE";
    private static final String OPTION_KEY = "OPTION";
    private static final String REMOTE_KEY_KEY = "REMOTE_KEY";

    /**
     * Returns true iff the input of the containing block is parsable as BoolExpr,
     * excluding as BoolLiteral, as these are a theoretical construct and would
     * be parsed as StringExpr when the ExpressionParser is called.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputKey        The key of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the input of the containing block is parsable as BoolExpr.
     */
    public static boolean parsableAsBoolExpr(JsonNode containingBlock, String inputKey, JsonNode allBlocks) {
        ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
        boolean hasBoolExprOpcode = false;
        JsonNode input = exprArray.get(POS_BLOCK_ID);
        if (input instanceof TextNode) {
            String identifier = input.asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return false; // it is a DataExpr
            }
            String opcodeString = exprBlock.get(OPCODE_KEY).asText();
            hasBoolExprOpcode = BoolExprOpcode.contains(opcodeString);
        } else if (input instanceof NullNode) {
            return true;
        }
        return hasBoolExprOpcode;
    }

    /**
     * Parses the input of the containingBlock specified by the inputKey.
     * If the input does not contain a BoolExpr calls the ExpressionParser
     * and wraps the result as BoolExpr.
     *
     * @param state           The current parser state.
     * @param containingBlock The block inputs of which contain the expression to be parsed.
     * @param inputKey        The key of the input which contains the expression.
     * @param allBlocks       All blocks of the actor definition currently parsed.
     * @return The expression identified by the inputKey.
     * @throws ParsingException If parsing fails.
     */
    public static BoolExpr parseBoolExpr(final ProgramParserState state, JsonNode containingBlock, String inputKey,
                                         JsonNode allBlocks) throws ParsingException {

        if (parsableAsBoolExpr(containingBlock, inputKey, allBlocks)) {
            ArrayNode exprArray = ExpressionParser.getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
            if (exprArray == null || exprArray.get(POS_INPUT_VALUE) instanceof NullNode) {
                return new UnspecifiedBoolExpr();
            }
            int shadowIndicator = ExpressionParser.getShadowIndicator(exprArray);
            if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                    || (shadowIndicator == INPUT_BLOCK_NO_SHADOW
                    && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
                try {
                    return parseBool(containingBlock.get(INPUTS_KEY), inputKey);
                } catch (ParsingException e) {
                    return new UnspecifiedBoolExpr();
                }
            } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
                String identifier = exprArray.get(POS_BLOCK_ID).asText();
                return parseBlockBoolExpr(state, identifier, allBlocks.get(identifier), allBlocks);
            } else if (shadowIndicator == INPUT_DIFF_BLOCK_SHADOW
                    && exprArray.get(POS_DATA_ARRAY) instanceof NullNode) {
                return new UnspecifiedBoolExpr();
            }
        } else {
            return new AsBool(ExpressionParser.parseExpr(state, containingBlock, inputKey, allBlocks));
        }

        throw new ParsingException("Could not parse BoolExpr");
    }

    /**
     * Parses a single BoolExpr corresponding to a reporter block.
     * The opcode of the block has to be a BoolExprOpcode.
     *
     * @param state     The current parser state.
     * @param exprBlock The JsonNode of the reporter block.
     * @param allBlocks All blocks of the actor definition currently analysed.
     * @return The parsed expression.
     * @throws ParsingException If the opcode of the block is no NumExprOpcode
     *                          or if parsing inputs of the block fails.
     */
    static BoolExpr parseBlockBoolExpr(final ProgramParserState state, String blockId, JsonNode exprBlock,
                                       JsonNode allBlocks) throws ParsingException {
        final String opcodeString = exprBlock.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(BoolExprOpcode.contains(opcodeString),
                        opcodeString + " is not a BoolExprOpcode.");
        final BoolExprOpcode opcode = BoolExprOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, exprBlock);
        switch (opcode) {

            case sensing_touchingcolor:
                Touchable color = TouchableParser.parseTouchable(state, exprBlock, allBlocks);
                return new SpriteTouchingColor(color, metadata);
            case sensing_touchingobject:
                Touchable touchable = TouchableParser.parseTouchable(state, exprBlock, allBlocks);
                return new Touching(touchable, metadata);
            case sensing_coloristouchingcolor:
                Color one = ColorParser.parseColor(state, exprBlock, COLOR_KEY, allBlocks);
                Color two = ColorParser.parseColor(state, exprBlock, COLOR2_KEY, allBlocks);
                return new ColorTouchingColor(one, two, metadata);
            case sensing_keypressed:
                Key key = KeyParser.parse(state, exprBlock, allBlocks);
                return new IsKeyPressed(key, metadata);
            case sensing_mousedown:
                return new IsMouseDown(metadata);
            case operator_gt:
                ComparableExpr first = NumExprParser.parseNumExpr(state, exprBlock, OPERAND1_KEY, allBlocks);
                ComparableExpr second = NumExprParser.parseNumExpr(state, exprBlock, OPERAND2_KEY, allBlocks);

                first = convertInputToFittingComparableExpr(state, first, OPERAND1_KEY, exprBlock, allBlocks);
                second = convertInputToFittingComparableExpr(state, second, OPERAND2_KEY, exprBlock, allBlocks);

                return new BiggerThan(first, second, metadata);
            case operator_lt:
                first = NumExprParser.parseNumExpr(state, exprBlock, OPERAND1_KEY, allBlocks);
                second = NumExprParser.parseNumExpr(state, exprBlock, OPERAND2_KEY, allBlocks);

                first = convertInputToFittingComparableExpr(state, first, OPERAND1_KEY, exprBlock, allBlocks);
                second = convertInputToFittingComparableExpr(state, second, OPERAND2_KEY, exprBlock, allBlocks);

                return new LessThan(first, second, metadata);
            case operator_equals:
                first = NumExprParser.parseNumExpr(state, exprBlock, OPERAND1_KEY, allBlocks);
                second = NumExprParser.parseNumExpr(state, exprBlock, OPERAND2_KEY, allBlocks);

                first = convertInputToFittingComparableExpr(state, first, OPERAND1_KEY, exprBlock, allBlocks);
                second = convertInputToFittingComparableExpr(state, second, OPERAND2_KEY, exprBlock, allBlocks);

                return new Equals(first, second, metadata);
            case operator_and:
                BoolExpr andFirst = parseCondition(state, exprBlock, OPERAND1_KEY, allBlocks);
                BoolExpr andSecond = parseCondition(state, exprBlock, OPERAND2_KEY, allBlocks);
                return new And(andFirst, andSecond, metadata);
            case operator_or:
                BoolExpr orFirst = parseCondition(state, exprBlock, OPERAND1_KEY, allBlocks);
                BoolExpr orSecond = parseCondition(state, exprBlock, OPERAND2_KEY, allBlocks);
                return new Or(orFirst, orSecond, metadata);
            case operator_not:
                BoolExpr notInput = parseCondition(state, exprBlock, OPERAND_KEY, allBlocks);
                return new Not(notInput, metadata);
            case operator_contains:
                StringExpr containing = StringExprParser.parseStringExpr(state, exprBlock, STRING1_KEY, allBlocks);
                StringExpr contained = StringExprParser.parseStringExpr(state, exprBlock, STRING2_KEY, allBlocks);
                return new StringContains(containing, contained, metadata);
            case data_listcontainsitem:
                String identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                String listName =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_NAME_POS).asText();
                Identifier containingVar;
                String currentActorName = state.getCurrentActor().getName();

                if (state.getSymbolTable().getList(identifier, listName, currentActorName).isEmpty()) {
                    List<Expression> listEx = new ArrayList<>();
                    ExpressionList expressionList = new ExpressionList(listEx);
                    state.getSymbolTable().addExpressionListInfo(identifier, listName, expressionList, true, "Stage");
                }
                Optional<ExpressionListInfo> list = state.getSymbolTable().getList(identifier, listName,
                        currentActorName);

                Preconditions.checkArgument(list.isPresent());
                ExpressionListInfo variableInfo = list.get();
                containingVar = new Qualified(new StrId(variableInfo.getActor()),
                        new ScratchList(new StrId((variableInfo.getVariableName()))));
                contained = StringExprParser.parseStringExpr(state, exprBlock, ITEM_KEY, allBlocks);
                return new ListContains(containingVar, contained, metadata);

            case event_led_matrix_position_is_light:
                NumExpr xCoordinate = NumExprParser.parseNumExpr(state, exprBlock, X, allBlocks);
                NumExpr yCoordinate = NumExprParser.parseNumExpr(state, exprBlock, Y, allBlocks);
                return new LEDMatrixPosition(xCoordinate, yCoordinate, metadata);

            case event_button_pressed:
                String buttonName = exprBlock.get(FIELDS_KEY).get(BUTTONS_KEY).get(0).asText();
                RobotButton button = new RobotButton(buttonName);
                return new RobotButtonPressed(button, metadata);

            case event_connect_rocky:
                return new ConnectRobot(metadata);

            case event_is_shaked:
                return new RobotShaken(metadata);

            case event_is_tilt:
                String tiltDirection = exprBlock.get(FIELDS_KEY).get(DIRECTION_KEY_CAP).get(0).asText();
                RobotDirection direction = new RobotDirection(tiltDirection);
                return new RobotTilted(direction, metadata);

            case event_is_orientate_to:
                String facing = exprBlock.get(FIELDS_KEY).get(ORIENTATE_CAP).get(0).asText();
                PadOrientation orientation = new PadOrientation(facing);
                return new OrientateTo(orientation, metadata);

            case rocky_event_obstacles_ahead:
                return new ObstaclesAhead(metadata);

            case event_is_color:
                String colorName = exprBlock.get(FIELDS_KEY).get(COLOR_KEY).get(0).asText();
                LEDColor ledColor = new LEDColor(colorName);
                return new SeeColor(ledColor, metadata);

            case event_external_linefollower:
                String portName = exprBlock.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                MCorePort port = new MCorePort(portName);
                String stateName = exprBlock.get(FIELDS_KEY).get(LINEFOLLOW_STATE_KEY).get(0).asText();
                LineFollowState lfState = new LineFollowState(stateName);
                String bwName = exprBlock.get(FIELDS_KEY).get(BLACK_WHITE_KEY).get(0).asText();
                BlackWhite blackWhite = new BlackWhite(bwName);
                return new PortOnLine(port, lfState, blackWhite, metadata);

            case event_board_button_pressed:
                String pressedState = exprBlock.get(FIELDS_KEY).get(OPTION_KEY).get(0).asText();
                PressedState pressed = new PressedState(pressedState);
                return new BoardButtonPressed(pressed, metadata);

            case event_ir_remote:
                String irButtonName = exprBlock.get(FIELDS_KEY).get(REMOTE_KEY_KEY).get(0).asText();
                IRRemoteButton irButton = new IRRemoteButton(irButtonName);
                return new IRButtonPressed(irButton, metadata);

            default:
                throw new RuntimeException(opcodeString + " is not covered by parseBlockExpr");
        }
    }

    private static BoolLiteral parseBool(JsonNode inputs, String inputKey) throws ParsingException {
        boolean value = ExpressionParser.getDataArrayByName(inputs, inputKey).get(POS_INPUT_VALUE).asBoolean();
        return new BoolLiteral(value);
    }

    /**
     * As in Scratch there are no default boolean values
     * the input can be empty and is then returned as UnspecifiedBoolExpr - directly calling parseBoolExpr()
     * would result in a ParsingException.
     */
    private static BoolExpr parseCondition(final ProgramParserState state, JsonNode exprBlock, String fieldName,
                                           JsonNode allBlocks) throws ParsingException {

        if (exprBlock.get(INPUTS_KEY).has(fieldName)) {
            return parseBoolExpr(state, exprBlock, fieldName, allBlocks);
        } else {
            return new UnspecifiedBoolExpr();
        }
    }

    private static ComparableExpr convertInputToFittingComparableExpr(final ProgramParserState state,
                                                                      ComparableExpr node, String input,
                                                                      JsonNode exprBlock, JsonNode allBlocks)
            throws ParsingException {
        if (node instanceof AsNumber) {
            if (((AsNumber) node).getOperand1() instanceof StringExpr) {
                node = (StringExpr) ((AsNumber) node).getOperand1();
            } else if (((AsNumber) node).getOperand1() instanceof ComparableExpr) {
                node = (ComparableExpr) ((AsNumber) node).getOperand1();
            } else {
                node = new AsString(((AsNumber) node).getOperand1());
            }
        } else if (node instanceof UnspecifiedNumExpr) {
            node = StringExprParser.parseStringExpr(state, exprBlock, input, allBlocks);
        } else if (node instanceof NumExpr) {
            return node;
        } else {
            throw new ParsingException("Could not convert to ComparableExpression");
        }
        return node;
    }
}
