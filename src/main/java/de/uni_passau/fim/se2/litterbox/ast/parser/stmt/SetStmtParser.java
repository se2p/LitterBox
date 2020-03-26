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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetAttributeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SetStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.attributes.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.parser.attributes.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.parser.attributes.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.parser.attributes.SoundEffect;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

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
            case pen_setPenSizeTo:
                return parseSetPenSizeTo(current, allBlocks);
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static SetStmt parseSetPenSizeTo(JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new SetAttributeTo(new StringLiteral(PEN_SIZE_KEY), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetVolumeTo(JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new SetAttributeTo(new StringLiteral(VOLUME_KEY), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetSoundEffect(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).asText();
        Preconditions.checkArgument(SoundEffect.contains(effect));
        return new SetAttributeTo(new StringLiteral(effect), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetLookEffect(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).asText();
        Preconditions.checkArgument(GraphicEffect.contains(effect));
        return new SetAttributeTo(new StringLiteral(effect), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetRotationStyle(JsonNode current) {
        String rota = current.get(FIELDS_KEY).get(STYLE_KEY).get(0).asText();
        Preconditions.checkArgument(RotationStyle.contains(rota));
        return new SetAttributeTo(new StringLiteral(ROTATIONSTYLE_KEY), new StringLiteral(rota));
    }

    private static SetStmt parseSetDragmode(JsonNode current) {
        String drag = current.get(FIELDS_KEY).get(DRAGMODE_KEY).get(0).asText();
        Preconditions.checkArgument(DragMode.contains(drag));
        return new SetAttributeTo(new StringLiteral(DRAG_KEY), new StringLiteral(drag));
    }

    private static SetStmt parseSetVariable(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String unique = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).asText();
        if (!ProgramParser.symbolTable.getVariables().containsKey(unique)) {
            return new SetVariableTo(new UnspecifiedId(), ExpressionParser.parseExpression(current,
                    0, allBlocks));
        }
        VariableInfo info = ProgramParser.symbolTable.getVariables().get(unique);
        return new SetVariableTo(new Qualified(new StrId(info.getActor()),
                new StrId((VARIABLE_ABBREVIATION + info.getVariableName()))), ExpressionParser.parseExpression(current,
                0, allBlocks));
    }
}
