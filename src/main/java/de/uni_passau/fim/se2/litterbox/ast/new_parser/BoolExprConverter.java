/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.UnspecifiedNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;

import java.util.Collections;

final class BoolExprConverter extends ExprConverter {

    private static final String ORIENTATE_CAP = "ORIENTATE";
    private static final String LINE_FOLLOW_STATE_KEY = "LINEFOLLOW_STATE";
    private static final String BLACK_WHITE_KEY = "BLACK_WHITE";
    private static final String OPTION_KEY = "OPTION";
    private static final String REMOTE_KEY_KEY = "REMOTE_KEY";

    private BoolExprConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static boolean parseableAsBoolExpr(final RawTarget target, final RawInput exprBlock) {
        if (exprBlock.input() == null) {
            return true;
        }

        if (exprBlock.input() instanceof BlockRef.IdRef inputRef) {
            final RawBlock inputBlock = target.blocks().get(inputRef.id());
            // if not regular block: must be `null`, therefore this is a DataExpr, not a BoolExpr
            return inputBlock instanceof RawBlock.RawRegularBlock block
                    && BoolExprOpcode.contains(block.opcode());
        }

        throw new UnsupportedOperationException("todo: is bool expr?");
    }

    static BoolExpr convertBoolExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        if (!parseableAsBoolExpr(state.getCurrentTarget(), exprBlock)) {
            return new AsBool(ExprConverter.convertExpr(state, containingBlock, exprBlock));
        }

        if (exprBlock.input() == null) {
            return new UnspecifiedBoolExpr();
        }

        if (hasCorrectShadow(exprBlock)) {
            // parse literal
            return convertBoolLiteral(exprBlock);
        }

        if (
                exprBlock.input() instanceof BlockRef.IdRef exprInput
                        && state.getBlock(exprInput.id()) instanceof RawBlock.RawRegularBlock exprInputRegularBlock
        ) {
            return convertBlockBoolExpr(state, exprInput.id(), exprInputRegularBlock);
        }

        throw new InternalParsingException("Could not parse BoolExpr.");
    }

    private static BoolExpr convertBoolLiteral(final RawInput exprBlock) {
        if (exprBlock.input() instanceof BlockRef.Block inputBlock) {
            final RawBlock.ArrayBlock literalInput = inputBlock.block();

            // can be pattern-matching switch when upgrading to Java 21
            final boolean value;
            if (literalInput instanceof RawBlock.RawStringLiteral s) {
                value = Boolean.parseBoolean(s.value());
            } else if (literalInput instanceof RawBlock.RawFloatBlockLiteral f) {
                value = f.value() == 0.0;
            } else if (literalInput instanceof RawBlock.RawIntBlockLiteral i) {
                value = i.value() == 0;
            } else if (literalInput instanceof RawBlock.RawAngleBlockLiteral a) {
                value = a.angle() == 0.0;
            } else {
                value = false;
            }

            return new BoolLiteral(value);
        }

        return new UnspecifiedBoolExpr();
    }

    static BoolExpr convertBlockBoolExpr(
            final ProgramParserState state,
            final RawBlockId id,
            final RawBlock.RawRegularBlock block
    ) {
        final BoolExprOpcode opcode = BoolExprOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(id, block);

        return switch (opcode) {
            case sensing_keypressed -> {
                final Key key = KeyConverter.convertKey(state, block);
                yield new IsKeyPressed(key, metadata);
            }
            case sensing_mousedown -> new IsMouseDown(metadata);
            case sensing_touchingobject -> {
                final Touchable touchable = ConverterUtilities.convertTouchable(state, block);
                yield new Touching(touchable, metadata);
            }
            case sensing_coloristouchingcolor -> {
                final Color a = ConverterUtilities.convertColor(state, block.inputs().get(Constants.COLOR_KEY));
                final Color b = ConverterUtilities.convertColor(state, block.inputs().get(Constants.COLOR2_KEY));
                yield new ColorTouchingColor(a, b, metadata);
            }
            case sensing_touchingcolor -> {
                final Touchable touchable = ConverterUtilities.convertTouchable(state, block);
                yield new SpriteTouchingColor(touchable, metadata);
            }
            case operator_gt -> new BiggerThan(
                    castComparableExpr(state, block, Constants.OPERAND1_KEY),
                    castComparableExpr(state, block, Constants.OPERAND2_KEY),
                    metadata
            );
            case operator_lt -> new LessThan(
                    castComparableExpr(state, block, Constants.OPERAND1_KEY),
                    castComparableExpr(state, block, Constants.OPERAND2_KEY),
                    metadata
            );
            case operator_equals -> new Equals(
                    castComparableExpr(state, block, Constants.OPERAND1_KEY),
                    castComparableExpr(state, block, Constants.OPERAND2_KEY),
                    metadata
            );
            case operator_and -> {
                final BoolExpr left = convertCondition(state, block, Constants.OPERAND1_KEY);
                final BoolExpr right = convertCondition(state, block, Constants.OPERAND2_KEY);
                yield new And(left, right, metadata);
            }
            case operator_or -> {
                final BoolExpr left = convertCondition(state, block, Constants.OPERAND1_KEY);
                final BoolExpr right = convertCondition(state, block, Constants.OPERAND2_KEY);
                yield new Or(left, right, metadata);
            }
            case operator_not -> {
                final BoolExpr input = convertCondition(state, block, Constants.OPERAND_KEY);
                yield new Not(input, metadata);
            }
            case operator_contains -> {
                final StringExpr containing = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.STRING1_KEY)
                );
                final StringExpr contained = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.STRING2_KEY)
                );
                yield new StringContains(containing, contained, metadata);
            }
            case data_listcontainsitem -> {
                final RawField listField = block.fields().get(Constants.LIST_KEY);
                final RawBlockId listId = listField.id()
                        .orElseThrow(() -> new InternalParsingException("Referenced list is missing an identifier."));
                final String listName = listField.value().toString();

                final ExpressionListInfo listInfo = state.getSymbolTable().getOrAddList(
                        listId.id(), listName, state.getCurrentActor().getName(),
                        () -> new ExpressionList(Collections.emptyList()), true, "Stage"
                );
                final Qualified list = ConverterUtilities.listInfoToIdentifier(listInfo, listName);

                final StringExpr contained = StringExprConverter.convertStringExpr(
                        state, block, block.inputs().get(Constants.ITEM_KEY)
                );

                yield new ListContains(list, contained, metadata);
            }
            case event_led_matrix_position_is_light -> {
                final NumExpr x = NumExprConverter.convertNumExpr(state, block, block.inputs().get(Constants.X));
                final NumExpr y = NumExprConverter.convertNumExpr(state, block, block.inputs().get(Constants.Y));
                yield new LEDMatrixPosition(x, y, metadata);
            }
            case event_button_pressed -> {
                final String buttonName = block.fields().get(Constants.BUTTONS_KEY).value().toString();
                final RobotButton button = new RobotButton(buttonName);
                yield new RobotButtonPressed(button, metadata);
            }
            case event_connect_rocky -> new ConnectRobot(metadata);
            case event_is_shaked -> new RobotShaken(metadata);
            case event_is_tilt -> {
                final String direction = block.fields().get(ORIENTATE_CAP).value().toString();
                final RobotDirection robotDirection = new RobotDirection(direction);
                yield new RobotTilted(robotDirection, metadata);
            }
            case event_is_orientate_to -> {
                final String direction = block.fields().get(ORIENTATE_CAP).value().toString();
                final PadOrientation orientation = new PadOrientation(direction);
                yield new OrientateTo(orientation, metadata);
            }
            case rocky_event_obstacles_ahead -> new ObstaclesAhead(metadata);
            case event_is_color -> {
                final String colorName = block.fields().get(Constants.COLOR_KEY).value().toString();
                final LEDColor color = new LEDColor(colorName);
                yield new SeeColor(color, metadata);
            }
            case event_external_linefollower -> {
                final MCorePort port = new MCorePort(
                        block.fields().get(Constants.PORT_KEY).value().toString()
                );
                final LineFollowState followState = new LineFollowState(
                        block.fields().get(LINE_FOLLOW_STATE_KEY).value().toString()
                );
                final BlackWhite bw = new BlackWhite(
                        block.fields().get(BLACK_WHITE_KEY).value().toString()
                );
                yield new PortOnLine(port, followState, bw, metadata);
            }
            case event_board_button_pressed -> {
                final String buttonName = block.fields().get(OPTION_KEY).value().toString();
                final PressedState button = new PressedState(buttonName);
                yield new BoardButtonPressed(button, metadata);
            }
            case event_ir_remote -> {
                final String irButtonName = block.fields().get(REMOTE_KEY_KEY).value().toString();
                final IRRemoteButton button = new IRRemoteButton(irButtonName);
                yield new IRButtonPressed(button, metadata);
            }
        };
    }

    private static BoolExpr convertCondition(
            final ProgramParserState state, final RawBlock.RawRegularBlock containingBlock, final String inputKey
    ) {
        final RawInput input = containingBlock.inputs().get(inputKey);

        if (input == null) {
            return new UnspecifiedBoolExpr();
        } else {
            return convertBoolExpr(state, containingBlock, input);
        }
    }

    private static ComparableExpr castComparableExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final String inputKey
    ) {
        final RawInput rawInput = containingBlock.inputs().get(inputKey);
        final NumExpr input = NumExprConverter.convertNumExpr(state, containingBlock, rawInput);

        // note: order of if/else-if chain important, since AsNumber is a subclass of the other ones
        if (input instanceof AsNumber number) {
            if (number.getOperand1() instanceof StringExpr op1) {
                return op1;
            } else if (number.getOperand1() instanceof ComparableExpr op1) {
                return op1;
            } else {
                return new AsString(number.getOperand1());
            }
        } else if (input instanceof UnspecifiedNumExpr) {
            return StringExprConverter.convertStringExpr(state, containingBlock, rawInput);
        } else if (input != null) {
            return input;
        }

        throw new InternalParsingException("Could not convert to ComparableExpression.");
    }
}
