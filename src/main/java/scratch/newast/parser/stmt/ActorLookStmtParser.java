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
package scratch.newast.parser.stmt;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.elementchoice.ElementChoice;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.statement.actorlook.ActorLookStmt;
import scratch.newast.model.statement.actorlook.AskAndWait;
import scratch.newast.model.statement.actorlook.ClearGraphicEffects;
import scratch.newast.model.statement.actorlook.SwitchBackdrop;
import scratch.newast.model.statement.spritelook.HideVariable;
import scratch.newast.model.statement.spritelook.ShowVariable;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.model.variable.Variable;
import scratch.newast.opcodes.ActorLookStmtOpcode;
import scratch.newast.parser.ElementChoiceParser;
import scratch.newast.parser.ProgramParser;
import scratch.newast.parser.StringExprParser;
import scratch.newast.parser.symboltable.ExpressionListInfo;
import scratch.newast.parser.symboltable.VariableInfo;

public class ActorLookStmtParser {

    private static final String CHANGE_EFFECTBY_INPUT_KEY = "CHANGE";
    private static final String VARIABLE = "VARIABLE";
    private static final String LIST = "LIST";

    public static ActorLookStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(ActorLookStmtOpcode.contains(opcodeString), "Given blockID does not point to an event block.");

        ActorLookStmtOpcode opcode = ActorLookStmtOpcode.valueOf(opcodeString);
        ActorLookStmt stmt;
        String variableName;
        String variableID;
        VariableInfo variableInfo;
        String actorName;
        Variable var;
        ExpressionListInfo expressionListInfo;

        switch (opcode) {
            case sensing_askandwait:
                StringExpr question = StringExprParser.parseStringExpr(current, 0, allBlocks);
                stmt = new AskAndWait(question);
                break;
            case looks_switchbackdropto:
                ElementChoice elementChoice = parseSwitchBackdropTo(current, allBlocks);
                stmt = new SwitchBackdrop(elementChoice);
                break;
            case looks_cleargraphiceffects:
                stmt = new ClearGraphicEffects();
                break;
            case data_hidevariable:
                variableName = current.get(FIELDS_KEY).get(VARIABLE).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE).get(1).asText();
                variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                actorName = variableInfo.getActor();
                var = new Qualified(new Identifier(actorName), new Identifier(variableName));
                stmt = new HideVariable(var);
                break;
            case data_showvariable:
                variableName = current.get(FIELDS_KEY).get(VARIABLE).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE).get(1).asText();
                variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                actorName = variableInfo.getActor();
                var = new Qualified(new Identifier(actorName), new Identifier(variableName));
                stmt = new ShowVariable(var);
                break;
            case data_showlist:
                variableName = current.get(FIELDS_KEY).get(LIST).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(LIST).get(1).asText();
                expressionListInfo = ProgramParser.symbolTable.getLists().get(variableID);
                actorName = expressionListInfo.getActor();
                var = new Qualified(new Identifier(actorName), new Identifier(variableName));
                stmt = new ShowVariable(var);
                break;
            case data_hidelist:
                variableName = current.get(FIELDS_KEY).get(LIST).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(LIST).get(1).asText();
                expressionListInfo = ProgramParser.symbolTable.getLists().get(variableID);
                actorName = expressionListInfo.getActor();
                var = new Qualified(new Identifier(actorName), new Identifier(variableName));
                stmt = new HideVariable(var);
                break;
            default:
                throw new ParsingException("No parser for opcode " + opcodeString);
        }

        return stmt;
    }

    private static ElementChoice parseSwitchBackdropTo(JsonNode current, JsonNode allBlocks) {
        JsonNode backdropNodeId = current.get(Constants.INPUTS_KEY).get(CHANGE_EFFECTBY_INPUT_KEY)
            .get(Constants.POS_DATA_ARRAY)
            .get(Constants.POS_INPUT_VALUE);
        JsonNode backdropMenu = allBlocks.get(backdropNodeId.asText());

        return ElementChoiceParser.parse(backdropMenu, allBlocks);
    }
}
