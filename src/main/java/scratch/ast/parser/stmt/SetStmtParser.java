/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.ast.parser.stmt;

import static scratch.ast.Constants.DRAGMODE_KEY;
import static scratch.ast.Constants.DRAG_KEY;
import static scratch.ast.Constants.EFFECT_KEY;
import static scratch.ast.Constants.FIELDS_KEY;
import static scratch.ast.Constants.OPCODE_KEY;
import static scratch.ast.Constants.ROTATIONSTYLE_KEY;
import static scratch.ast.Constants.STYLE_KEY;
import static scratch.ast.Constants.VARIABLE_IDENTIFIER_POS;
import static scratch.ast.Constants.VARIABLE_KEY;
import static scratch.ast.Constants.VOLUME_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.utils.Preconditions;
import scratch.ast.ParsingException;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.common.SetAttributeTo;
import scratch.ast.model.statement.common.SetStmt;
import scratch.ast.model.statement.common.SetVariableTo;
import scratch.ast.model.variable.Qualified;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.SetStmtOpcode;
import scratch.ast.parser.ExpressionParser;
import scratch.ast.parser.ProgramParser;
import scratch.ast.parser.attributes.DragMode;
import scratch.ast.parser.attributes.GraphicEffect;
import scratch.ast.parser.attributes.RotationStyle;
import scratch.ast.parser.attributes.SoundEffect;
import scratch.ast.parser.symboltable.VariableInfo;

public class SetStmtParser {

    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(SetStmtOpcode.contains(opcodeString), "Given blockID does not point to a set block.");

        final SetStmtOpcode opcode = SetStmtOpcode.valueOf(opcodeString);

        switch (opcode) {
            case data_setvariableto:
                return parseSetVariable(current, allBlocks);
            case sensing_setdragmode:
                return parseSetDragmode(current);
            case motion_setrotationstyle:
                return parseSetRotationStyle(current);
            case looks_seteffectto:
                return parseSetLookEffect(current, allBlocks);
            case sound_seteffectto:
                return parseSetSoundEffect(current, allBlocks);
            case sound_setvolumeto:
                return parseSetVolumeTo(current, allBlocks);
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static SetStmt parseSetVolumeTo(JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new SetAttributeTo(new StringLiteral(VOLUME_KEY), ExpressionParser.parseExpression(current, 0,
            allBlocks));
    }

    private static SetStmt parseSetSoundEffect(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).textValue();
        Preconditions.checkArgument(SoundEffect.contains(effect));
        return new SetAttributeTo(new StringLiteral(effect), ExpressionParser.parseExpression(current, 0,
            allBlocks));
    }

    private static SetStmt parseSetLookEffect(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).textValue();
        Preconditions.checkArgument(GraphicEffect.contains(effect));
        return new SetAttributeTo(new StringLiteral(effect), ExpressionParser.parseExpression(current, 0,
            allBlocks));
    }

    private static SetStmt parseSetRotationStyle(JsonNode current) {
        String rota = current.get(FIELDS_KEY).get(STYLE_KEY).get(0).textValue();
        Preconditions.checkArgument(RotationStyle.contains(rota));
        return new SetAttributeTo(new StringLiteral(ROTATIONSTYLE_KEY), new StringLiteral(rota));

    }

    private static SetStmt parseSetDragmode(JsonNode current) {
        String drag = current.get(FIELDS_KEY).get(DRAGMODE_KEY).get(0).textValue();
        Preconditions.checkArgument(DragMode.contains(drag));
        return new SetAttributeTo(new StringLiteral(DRAG_KEY), new StringLiteral(drag));
    }

    private static SetStmt parseSetVariable(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String unique = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).textValue();
        Preconditions.checkArgument(ProgramParser.symbolTable.getVariables().containsKey(unique));
        VariableInfo info = ProgramParser.symbolTable.getVariables().get(unique);
        return new SetVariableTo(new Qualified(new StrId(info.getActor()),
            new StrId((info.getVariableName()))), ExpressionParser.parseExpression(current,
            0, allBlocks));
    }
}
