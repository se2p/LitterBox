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
import com.fasterxml.jackson.databind.node.TextNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RGB;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser.*;

public class NumExprParser {

    /**
     * Returns true iff the input of the containing block is parsable as NumExpr,
     * excluding casts with AsNumber.
     *
     * @param containingBlock The block inputs of which contain the expression
     *                        to be checked.
     * @param inputKey        The key of the input containing the expression to be checked.
     * @param allBlocks       All blocks of the actor definition currently analysed.
     * @return True iff the input of the containing block is parsable as NumExpr.
     * @throws ParsingException If the JSON is malformed.
     */
    @SuppressWarnings("unused")
    public static boolean parsableAsNumExpr(JsonNode containingBlock,
                                            String inputKey, JsonNode allBlocks) throws ParsingException {
        JsonNode inputs = containingBlock.get(INPUTS_KEY);
        ArrayNode exprArray = getExprArray(inputs, inputKey);
        int shadowIndicator = getShadowIndicator(exprArray);

        // parsable as NumberLiteral
        boolean parsableAsNumberLiteral = false;
        if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                || (shadowIndicator == INPUT_BLOCK_NO_SHADOW
                && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
            try {
                String valueString =
                        ExpressionParser.getDataArrayByName(inputs, inputKey).get(POS_INPUT_VALUE).asText();
                Float.parseFloat(valueString);
                parsableAsNumberLiteral = true;
            } catch (NumberFormatException | ParsingException | ClassCastException e) {
                // not parsable as NumberLiteral
            }
        }

        // or NumExpr opcode
        boolean hasNumExprOpcode = false;
        if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            JsonNode exprBlock = allBlocks.get(identifier);
            if (exprBlock == null) {
                return false; // it is a DataExpr
            }
            JsonNode opcodeNode = exprBlock.get(OPCODE_KEY);
            String opcodeString = opcodeNode.asText();
            hasNumExprOpcode = NumExprOpcode.contains(opcodeString);
        }
        return hasNumExprOpcode || parsableAsNumberLiteral;
    }

    /**
     * Parses the input of the containingBlock specified by the inputKey.
     * If the input does not contain a NumExpr calls the ExpressionParser
     * and wraps the result as NumExpr.
     *
     * @param state           The current state of the parser.
     * @param containingBlock The block inputs of which contain the expression to be parsed.
     * @param inputKey        The key of the input which contains the expression.
     * @param allBlocks       All blocks of the actor definition currently parsed.
     * @return The expression identified by the inputKey.
     * @throws ParsingException If parsing fails.
     */
    public static NumExpr parseNumExpr(final ProgramParserState state, JsonNode containingBlock, String inputKey,
                                       JsonNode allBlocks)
            throws ParsingException {
        if (parsableAsNumExpr(containingBlock, inputKey, allBlocks)) {
            ArrayNode exprArray = getExprArray(containingBlock.get(INPUTS_KEY), inputKey);
            int shadowIndicator = getShadowIndicator(exprArray);
            if (shadowIndicator == INPUT_SAME_BLOCK_SHADOW
                    || (shadowIndicator == INPUT_BLOCK_NO_SHADOW
                    && !(exprArray.get(POS_BLOCK_ID) instanceof TextNode))) {
                try {
                    return parseNumber(containingBlock.get(INPUTS_KEY), inputKey);
                } catch (NumberFormatException | ParsingException e) {
                    return new UnspecifiedNumExpr();
                }
            } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
                String identifier = exprArray.get(POS_BLOCK_ID).asText();
                return parseBlockNumExpr(state, identifier, allBlocks.get(identifier), allBlocks);
            }
        } else {
            return new AsNumber(ExpressionParser.parseExpr(state, containingBlock, inputKey, allBlocks));
        }
        throw new ParsingException("Could not parse NumExpr.");
    }

    /**
     * Parses a single NumExpression corresponding to a reporter block.
     * The opcode of the block has to be a NumExprOpcode.
     *
     * @param state     The current state of the parser.
     * @param exprBlock The JsonNode of the reporter block.
     * @param allBlocks All blocks of the actor definition currently analysed.
     * @return The parsed expression.
     * @throws ParsingException If the opcode of the block is no NumExprOpcode
     *                          or if parsing inputs of the block fails.
     */
    static NumExpr parseBlockNumExpr(final ProgramParserState state, String blockId, JsonNode exprBlock,
                                     JsonNode allBlocks)
            throws ParsingException {
        String opcodeString = exprBlock.get(OPCODE_KEY).asText();
        Preconditions.checkArgument(NumExprOpcode.contains(opcodeString),
                opcodeString + " is not a NumExprOpcode.");
        NumExprOpcode opcode = NumExprOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, exprBlock);
        String currentActorName = state.getCurrentActor().getName();
        switch (opcode) {
            case sound_volume:
                return new Volume(metadata);
            case motion_xposition:
                return new PositionX(metadata);
            case motion_yposition:
                return new PositionY(metadata);
            case motion_direction:
                return new Direction(metadata);
            case looks_size:
                return new Size(metadata);
            case sensing_timer:
                return new Timer(metadata);
            case sensing_dayssince2000:
                return new DaysSince2000(metadata);
            case sensing_mousex:
                return new MouseX(metadata);
            case sensing_mousey:
                return new MouseY(metadata);
            case sensing_loudness:
                return new Loudness(metadata);
            case operator_round:
                NumExpr num = parseNumExpr(state, exprBlock, NUM_KEY, allBlocks);
                return new Round(num, metadata);
            case operator_length:
                return new LengthOfString(StringExprParser.parseStringExpr(state, exprBlock, STRING_KEY, allBlocks),
                        metadata);
            case data_lengthoflist:
                String identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                String listName = exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_NAME_POS).asText();
                Identifier variable;
                if (state.getSymbolTable().getList(identifier, listName, currentActorName).isEmpty()) {
                    List<Expression> listEx = new ArrayList<>();
                    ExpressionList expressionList = new ExpressionList(listEx);
                    state.getSymbolTable().addExpressionListInfo(identifier, listName, expressionList, true, "Stage");
                }
                Optional<ExpressionListInfo> list = state.getSymbolTable().getList(identifier, listName,
                        currentActorName);

                Preconditions.checkArgument(list.isPresent());
                ExpressionListInfo variableInfo = list.get();
                variable = new Qualified(new StrId(variableInfo.getActor()),
                        new ScratchList(new StrId(variableInfo.getVariableName())));
                return new LengthOfVar(variable, metadata);
            case sensing_current:
                TimeComp timeComp = TimecompParser.parse(exprBlock);
                return new Current(timeComp, metadata);
            case sensing_distanceto:
                Position pos = PositionParser.parse(state, exprBlock, allBlocks);
                return new DistanceTo(pos, metadata);
            case operator_add:
                return buildNumExprWithTwoNumExprInputs(state, Add.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks,
                        metadata);
            case operator_subtract:
                return buildNumExprWithTwoNumExprInputs(state, Minus.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks,
                        metadata);
            case operator_multiply:
                return buildNumExprWithTwoNumExprInputs(state, Mult.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks,
                        metadata);
            case operator_divide:
                return buildNumExprWithTwoNumExprInputs(state, Div.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks,
                        metadata);
            case operator_mod:
                return buildNumExprWithTwoNumExprInputs(state, Mod.class, exprBlock, NUM1_KEY, NUM2_KEY, allBlocks,
                        metadata);
            case operator_random:
                return buildNumExprWithTwoNumExprInputs(state, PickRandom.class, exprBlock, FROM_KEY, TO_KEY, allBlocks,
                        metadata);
            case operator_mathop:
                NumFunct funct = parseNumFunct(exprBlock.get(FIELDS_KEY));
                NumExpr numExpr = parseNumExpr(state, exprBlock, NUM_KEY, allBlocks);
                return new NumFunctOf(funct, numExpr, metadata);
            case data_itemnumoflist:
                Expression item = parseExpr(state, exprBlock, ITEM_KEY, allBlocks);
                identifier =
                        exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                listName = exprBlock.get(FIELDS_KEY).get(LIST_KEY).get(LIST_NAME_POS).asText();
                if (state.getSymbolTable().getList(identifier, listName, currentActorName).isEmpty()) {
                    List<Expression> listEx = new ArrayList<>();
                    ExpressionList expressionList = new ExpressionList(listEx);
                    state.getSymbolTable().addExpressionListInfo(identifier, listName, expressionList, true, "Stage");
                }
                list = state.getSymbolTable().getList(identifier, listName, currentActorName);

                Preconditions.checkArgument(list.isPresent());
                variableInfo = list.get();
                variable = new Qualified(new StrId(variableInfo.getActor()),
                        new ScratchList(new StrId(variableInfo.getVariableName())));
                return new IndexOf(item, variable, metadata);
            case detect_sound_volume:
                return new SpeakerVolume(metadata);

            case detect_potentiometer:
                return new Potentiometer(metadata);

            case detect_volume:
                return new SoundVolume(metadata);

            case detect_lightness:
                return new AmbientLight(metadata);

            case dump_energy:
                return new BatteryEnergy(metadata);

            case detect_shaked_strength:
                return new ShakingStrength(metadata);

            case detect_gyro_roll_angle:
                return new GyroRollAngle(metadata);

            case detect_gyro_pitch_angle:
                return new GyroPitchAngle(metadata);

            case detect_rotatex_angle:
                return new RotateXAngle(metadata);

            case detect_rotatey_angle:
                return new RotateYAngle(metadata);

            case detect_rotatez_angle:
                return new RotateZAngle(metadata);

            case detect_timer:
            case detect_time:
                return new RobotTimer(metadata);

            case rocky_detect_rgb:
                String rgbName = exprBlock.get(FIELDS_KEY).get(RGB_KEY).get(0).asText();
                RGB rgb = new RGB(rgbName);
                return new DetectRGBValue(rgb, metadata);

            case rocky_detect_lightness:
                return new DetectAmbientLight(metadata);

            case rocky_detect_reflection:
                return new DetectReflection(metadata);

            case rocky_detect_ir_reflection:
                return new DetectIRReflection(metadata);

            case rocky_detect_grey:
                return new DetectGrey(metadata);

            case detect_external_light:
                String portNumber = exprBlock.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                MCorePort port = new MCorePort(portNumber);
                return new DetectAmbientLightPort(port, metadata);

            case detect_external_ultrasonic:
                portNumber = exprBlock.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                return new DetectDistancePort(port, metadata);

            case detect_external_linefollower:
                portNumber = exprBlock.get(FIELDS_KEY).get(PORT_KEY).get(0).asText();
                port = new MCorePort(portNumber);
                return new DetectLinePort(port, metadata);
            case music_getTempo:
                return new Tempo(metadata);

            default:
                throw new ParsingException(opcodeString + " is not covered by parseBlockNumExpr");
        }
    }

    /**
     * Parses the inputs of the NumExpr the identifier of which is handed over and returns the NumExpr holding its two
     * inputs.
     *
     * @param state     The current state of the parser.
     * @param clazz     The class implementing NumExpr of which an instance is to be created.
     * @param exprBlock The JsonNode of the NumExpr.
     * @param allBlocks All blocks of the actor definition currently analysed.
     * @param <T>       A class which has to implement the {@link NumExpr} interface.
     * @return A new T instance holding the NumExpr inputs specified by their names.
     * @throws ParsingException If creating the new T instance goes wrong.
     */
    private static <T extends NumExpr> NumExpr buildNumExprWithTwoNumExprInputs(final ProgramParserState state,
                                                                                Class<T> clazz,
                                                                                JsonNode exprBlock,
                                                                                String firstInputName,
                                                                                String secondInputName,
                                                                                JsonNode allBlocks,
                                                                                BlockMetadata metadata)
            throws ParsingException {

        NumExpr first = parseNumExpr(state, exprBlock, firstInputName, allBlocks);
        NumExpr second = parseNumExpr(state, exprBlock, secondInputName, allBlocks);
        try {
            return clazz.getConstructor(NumExpr.class, NumExpr.class, BlockMetadata.class).newInstance(first, second,
                    metadata);
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new ParsingException(e);
        }
    }

    /**
     * Returns the number at the position in the inputs node. For example, if script is the JsonNode holding all blocks
     * and "EU(l=G6)z8NGlJFcx|fS" is a blockID of a block with an input called "STEPS",
     * you can parse the STEPS to a Number like this:
     *
     * <p>JsonNode inputs = script.get("EU(l=G6)z8NGlJFcx|fS").get("inputs");
     * Number result = ExpressionParser.parseNumber(inputs, "STEPS");
     *
     * <p>Note that this method only works if the inputs node has the key specified
     * in the inputKey parameter.
     *
     * @param inputs   The JsonNode holding all inputs of a block.
     * @param inputKey The name of the input to parse in the inputs node.
     * @return A Number holding the value of the literal entered.
     */
    static NumberLiteral parseNumber(JsonNode inputs, String inputKey) throws ParsingException {
        Preconditions.checkArgument(inputs.has(inputKey));
        String valueString = ExpressionParser.getDataArrayByName(inputs, inputKey).get(POS_INPUT_VALUE).asText();
        double value = Double.parseDouble(valueString);
        return new NumberLiteral(value);
    }

    /**
     * Parses a NumFunct from the "fields" JsonNode.
     * The node has to have an "OPERATOR" key.
     *
     * @param fields The JsonNode containing the operator of the NumFunct.
     * @return The NumFunct stored in the fields node.
     */
    static NumFunct parseNumFunct(JsonNode fields) {
        Preconditions.checkArgument(fields.has(OPERATOR_KEY));
        ArrayNode operator = (ArrayNode) fields.get(OPERATOR_KEY);
        String operatorOpcode = operator.get(FIELD_VALUE).asText();
        return new NumFunct(operatorOpcode);
    }
}
