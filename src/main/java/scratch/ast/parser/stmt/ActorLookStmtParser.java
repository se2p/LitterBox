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

import static scratch.ast.Constants.FIELDS_KEY;
import static scratch.ast.Constants.FIELD_VALUE;
import static scratch.ast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.ast.ParsingException;
import scratch.ast.model.elementchoice.ElementChoice;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.statement.actorlook.ActorLookStmt;
import scratch.ast.model.statement.actorlook.AskAndWait;
import scratch.ast.model.statement.actorlook.ClearGraphicEffects;
import scratch.ast.model.statement.actorlook.SwitchBackdrop;
import scratch.ast.model.statement.spritelook.HideVariable;
import scratch.ast.model.statement.spritelook.ShowVariable;
import scratch.ast.model.variable.Qualified;
import scratch.ast.model.variable.StrId;
import scratch.ast.model.variable.Variable;
import scratch.ast.opcodes.ActorLookStmtOpcode;
import scratch.ast.parser.ElementChoiceParser;
import scratch.ast.parser.ProgramParser;
import scratch.ast.parser.StringExprParser;
import scratch.ast.parser.symboltable.ExpressionListInfo;
import scratch.ast.parser.symboltable.VariableInfo;

public class ActorLookStmtParser {

    private static final String CHANGE_BACKDROP_TO = "BACKDROP";
    private static final String VARIABLE = "VARIABLE";
    private static final String LIST = "LIST";

    public static ActorLookStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(ActorLookStmtOpcode.contains(opcodeString), "Given blockID does not point to an event block.");

        final ActorLookStmtOpcode opcode = ActorLookStmtOpcode.valueOf(opcodeString);

        String variableName;
        String variableID;
        VariableInfo variableInfo;
        String actorName;
        Variable var;
        ExpressionListInfo expressionListInfo;

        switch (opcode) {
            case sensing_askandwait:
                StringExpr question = StringExprParser.parseStringExpr(current, 0, allBlocks);
                return new AskAndWait(question);

            case looks_switchbackdropto:
                ElementChoice elementChoice = ElementChoiceParser.parse(current, allBlocks);
                return new SwitchBackdrop(elementChoice);

            case looks_cleargraphiceffects:
                return new ClearGraphicEffects();

            case data_hidevariable:
                variableName = current.get(FIELDS_KEY).get(VARIABLE).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE).get(1).asText();
                variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                actorName = variableInfo.getActor();
                var = new Qualified(new StrId(actorName), new StrId(variableName));
                return new HideVariable(var);

            case data_showvariable:
                variableName = current.get(FIELDS_KEY).get(VARIABLE).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE).get(1).asText();
                variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                actorName = variableInfo.getActor();
                var = new Qualified(new StrId(actorName), new StrId(variableName));
                return new ShowVariable(var);

            case data_showlist:
                variableName = current.get(FIELDS_KEY).get(LIST).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(LIST).get(1).asText();
                expressionListInfo = ProgramParser.symbolTable.getLists().get(variableID);
                actorName = expressionListInfo.getActor();
                var = new Qualified(new StrId(actorName), new StrId(variableName));
                return new ShowVariable(var);

            case data_hidelist:
                variableName = current.get(FIELDS_KEY).get(LIST).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(LIST).get(1).asText();
                expressionListInfo = ProgramParser.symbolTable.getLists().get(variableID);
                actorName = expressionListInfo.getActor();
                var = new Qualified(new StrId(actorName), new StrId(variableName));
                return new HideVariable(var);

            default:
                throw new ParsingException("No parser for opcode " + opcodeString);
        }
    }
}
