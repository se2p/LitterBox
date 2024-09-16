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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RGB;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class NumExprConverter extends ExprConverter {

    private NumExprConverter() {
        throw new IllegalCallerException("utility class constructor");
    }

    static NumExpr convertNumExpr(
            final ProgramParserState state,
            final RawBlock.RawRegularBlock containingBlock,
            final RawInput exprBlock
    ) {
        if (!parseableAsNumExpr(state.getCurrentTarget(), exprBlock)) {
            final Expression expr = ExpressionConverter.convertExpr(state, containingBlock, exprBlock);
            return new AsNumber(expr);
        }

        if (hasCorrectShadow(exprBlock)) {
            return parseLiteralNumberInput(exprBlock);
        }

        if (
                exprBlock.input() instanceof BlockRef.IdRef exprInput
                && state.getBlock(exprInput.id()) instanceof RawBlock.RawRegularBlock exprInputRegularBlock
        ) {
            return parseBlockNumExpr(state, exprInput.id(), exprInputRegularBlock);
        }

        throw new InternalParsingException("Could not parse NumExpr.");
    }

    /**
     * Checks if the block can be parsed as {@link NumExpr} without wrapping in {@link AsNumber}.
     *
     * @param target The actor the block appears in.
     * @param exprBlock The expression to check.
     * @return True, iff the {@code exprBlock} can be parsed as {@link NumExpr}.
     */
    static boolean parseableAsNumExpr(final RawTarget target, final RawInput exprBlock) {
        final boolean parseableAsNumberLiteral = isParseableAsNumberLiteral(exprBlock);
        final boolean hasNumExprOpcode = hasNumExprOpcode(target, exprBlock);

        return parseableAsNumberLiteral || hasNumExprOpcode;
    }

    private static boolean isParseableAsNumberLiteral(final RawInput exprBlock) {
        final boolean hasCorrectType = exprBlock.input() instanceof BlockRef.Block exprInput && (
                exprInput.block() instanceof RawBlock.RawFloatBlockLiteral
                        || exprInput.block() instanceof RawBlock.RawIntBlockLiteral
                        || exprInput.block() instanceof RawBlock.RawAngleBlockLiteral
        );

        return hasCorrectShadow(exprBlock) || hasCorrectType;
    }

    private static boolean hasNumExprOpcode(final RawTarget target, final RawInput exprBlock) {
        if (exprBlock.input() instanceof BlockRef.IdRef inputIdRef) {
            final RawBlock inputBlock = target.blocks().get(inputIdRef.id());
            if (inputBlock == null) {
                return false;
            }

            if (inputBlock instanceof RawBlock.RawRegularBlock inputRegularBlock) {
                return NumExprOpcode.contains(inputRegularBlock.opcode());
            }
        }

        return false;
    }

    private static NumExpr parseLiteralNumberInput(final RawInput exprBlock) {
        if (exprBlock.input() instanceof BlockRef.Block inputBlock) {
            final RawBlock.ArrayBlock literalInput = inputBlock.block();
            // note: should be converted to pattern-matching switch with Java 21
            if (literalInput instanceof RawBlock.RawFloatBlockLiteral f) {
                return new NumberLiteral(f.value());
            } else if (literalInput instanceof RawBlock.RawIntBlockLiteral i) {
                return new NumberLiteral(i.value());
            } else if (literalInput instanceof RawBlock.RawAngleBlockLiteral a) {
                return new NumberLiteral(a.angle());
            }
        }

        return new UnspecifiedNumExpr();
    }

    private static NumExpr parseBlockNumExpr(
            final ProgramParserState state,
            final RawBlockId id,
            final RawBlock.RawRegularBlock block
    ) {
        final NumExprOpcode opcode = NumExprOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(id, block);

        return switch (opcode) {
            case sound_volume -> new Volume(metadata);
            case motion_xposition -> new PositionX(metadata);
            case motion_yposition -> new PositionY(metadata);
            case motion_direction -> new Direction(metadata);
            case looks_size -> new Size(metadata);
            case sensing_timer -> new Timer(metadata);
            case sensing_dayssince2000 -> new DaysSince2000(metadata);
            case sensing_mousex -> new MouseX(metadata);
            case sensing_mousey -> new MouseY(metadata);
            case sensing_loudness -> new Loudness(metadata);
            case detect_sound_volume -> new SpeakerVolume(metadata);
            case detect_potentiometer -> new Potentiometer(metadata);
            case detect_volume -> new SoundVolume(metadata);
            case detect_lightness -> new AmbientLight(metadata);
            case dump_energy -> new BatteryEnergy(metadata);
            case detect_shaked_strength -> new ShakingStrength(metadata);
            case detect_gyro_roll_angle -> new GyroRollAngle(metadata);
            case detect_gyro_pitch_angle -> new GyroPitchAngle(metadata);
            case detect_rotatex_angle -> new RotateXAngle(metadata);
            case detect_rotatey_angle -> new RotateYAngle(metadata);
            case detect_rotatez_angle -> new RotateZAngle(metadata);
            case detect_timer, detect_time -> new RobotTimer(metadata);
            case rocky_detect_lightness -> new DetectAmbientLight(metadata);
            case rocky_detect_reflection -> new DetectReflection(metadata);
            case rocky_detect_ir_reflection -> new DetectIRReflection(metadata);
            case rocky_detect_grey -> new DetectGrey(metadata);
            case music_getTempo -> new Tempo(metadata);
            case operator_round -> {
                final NumExpr num = convertNumExpr(state, block, block.inputs().get(Constants.NUM_KEY));
                yield new Round(num, metadata);
            }
            case operator_length -> {
                final RawInput stringInput = block.inputs().get(Constants.STRING_KEY);
                final StringExpr stringExpr = StringExprConverter.convertStringExpr(state, block, stringInput);
                yield new LengthOfString(stringExpr, metadata);
            }
            // binary operators
            case operator_add -> {
                final NumExpr left = convertNumExpr(state, block, block.inputs().get(Constants.NUM1_KEY));
                final NumExpr right = convertNumExpr(state, block, block.inputs().get(Constants.NUM2_KEY));
                yield new Add(left, right, metadata);
            }
            case operator_subtract -> {
                final NumExpr left = convertNumExpr(state, block, block.inputs().get(Constants.NUM1_KEY));
                final NumExpr right = convertNumExpr(state, block, block.inputs().get(Constants.NUM2_KEY));
                yield new Minus(left, right, metadata);
            }
            case operator_multiply -> {
                final NumExpr left = convertNumExpr(state, block, block.inputs().get(Constants.NUM1_KEY));
                final NumExpr right = convertNumExpr(state, block, block.inputs().get(Constants.NUM2_KEY));
                yield new Mult(left, right, metadata);
            }
            case operator_divide -> {
                final NumExpr left = convertNumExpr(state, block, block.inputs().get(Constants.NUM1_KEY));
                final NumExpr right = convertNumExpr(state, block, block.inputs().get(Constants.NUM2_KEY));
                yield new Div(left, right, metadata);
            }
            case operator_mod -> {
                final NumExpr left = convertNumExpr(state, block, block.inputs().get(Constants.NUM1_KEY));
                final NumExpr right = convertNumExpr(state, block, block.inputs().get(Constants.NUM2_KEY));
                yield new Mod(left, right, metadata);
            }
            case operator_random -> {
                final NumExpr left = convertNumExpr(state, block, block.inputs().get(Constants.NUM1_KEY));
                final NumExpr right = convertNumExpr(state, block, block.inputs().get(Constants.NUM2_KEY));
                yield new PickRandom(left, right, metadata);
            }
            // others
            case operator_mathop -> {
                final NumFunct function = convertNumberFunction(block.fields().get(Constants.OPERATOR_KEY));
                final NumExpr expr = convertNumExpr(state, block, block.inputs().get(Constants.NUM_KEY));
                yield new NumFunctOf(function, expr, metadata);
            }
            case data_lengthoflist -> throw new UnsupportedOperationException("todo: length of list");
            case data_itemnumoflist -> throw new UnsupportedOperationException("todo: item of list");
            case sensing_current -> throw new UnsupportedOperationException("todo: num expr now");
            case sensing_distanceto -> throw new UnsupportedOperationException("todo: distance to");
            case rocky_detect_rgb -> {
                final String rgbName = block.fields().get(Constants.RGB_KEY).value().toString();
                final RGB rgb = new RGB(rgbName);
                yield new DetectRGBValue(rgb, metadata);
            }
            case detect_external_light -> {
                final String portId = block.fields().get(Constants.PORT_KEY).value().toString();
                final MCorePort port = new MCorePort(portId);
                yield new DetectAmbientLightPort(port, metadata);
            }
            case detect_external_ultrasonic -> {
                final String portId = block.fields().get(Constants.PORT_KEY).value().toString();
                final MCorePort port = new MCorePort(portId);
                yield new DetectDistancePort(port, metadata);
            }
            case detect_external_linefollower -> {
                final String portId = block.fields().get(Constants.PORT_KEY).value().toString();
                final MCorePort port = new MCorePort(portId);
                yield new DetectLinePort(port, metadata);
            }
        };
    }

    private static NumFunct convertNumberFunction(final RawField numberFunction) {
        final String opcode = numberFunction.value().toString();
        return new NumFunct(opcode);
    }
}
