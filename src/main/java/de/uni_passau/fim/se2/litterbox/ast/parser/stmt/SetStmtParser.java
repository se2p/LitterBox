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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SetStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ActorDefinitionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class SetStmtParser {

    public static Stmt parse(String blockId, JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SetStmtOpcode.contains(opcodeString), "Given blockID does not point to a set block.");

        final SetStmtOpcode opcode = SetStmtOpcode.valueOf(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        if (opcode == SetStmtOpcode.data_setvariableto) {
            return parseSetVariable(current, allBlocks, metadata);
        }
        throw new RuntimeException("Not Implemented yet");
    }

    private static SetStmt parseSetVariable(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        String unique = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).asText();
        String variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_NAME_POS).asText();
        String currentActorName = ActorDefinitionParser.getCurrentActor().getName();
        if (ProgramParser.symbolTable.getVariable(unique, variableName, currentActorName).isEmpty()) {
            return new SetVariableTo(new UnspecifiedId(), ExpressionParser.parseExpr(current,
                    VALUE_KEY, allBlocks), metadata);
        }
        VariableInfo info = ProgramParser.symbolTable.getVariable(unique, variableName, currentActorName).get();
        return new SetVariableTo(new Qualified(new StrId(info.getActor()),
                new Variable(new StrId(info.getVariableName()))), ExpressionParser.parseExpr(current,
                VALUE_KEY, allBlocks), metadata);
    }
}
