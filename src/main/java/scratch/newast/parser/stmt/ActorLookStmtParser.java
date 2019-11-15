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
import scratch.newast.opcodes.EventOpcode;
import scratch.newast.parser.ElementChoiceParser;
import scratch.newast.parser.ExpressionParser;
import scratch.newast.parser.ProgramParser;
import scratch.newast.parser.symboltable.VariableInfo;

public class ActorLookStmtParser {

    private static final String CHANGE_EFFECTBY_INPUT_KEY = "CHANGE";
    private static final String VARIABLE = "VARIABLE";

    public static ActorLookStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(EventOpcode.contains(opcodeString), "Given blockID does not point to an event block.");

        ActorLookStmtOpcode opcode = ActorLookStmtOpcode.valueOf(opcodeString);
        ActorLookStmt stmt;
        String variableName;
        String variableID;
        VariableInfo variableInfo;
        String actorName;
        Variable var;

        switch (opcode) {
            case sensing_askandwait:
                StringExpr question = ExpressionParser.parseStringExpr(current, 0, allBlocks);
                stmt = new AskAndWait(question);
                break;
            case looks_switchbackdropto:
                ElementChoice elementChoice = parseSwitchBackdropTo(current, allBlocks);
                stmt = new SwitchBackdrop(elementChoice);
                break;
            case looks_cleargraphiceffects:
                stmt = new ClearGraphicEffects();
                break;
            case data_showvariable:
            case data_showlist:
                variableName = current.get(FIELDS_KEY).get(VARIABLE).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE).get(1).asText();
                variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                actorName = variableInfo.getActor();
                var = new Qualified(new Identifier(actorName), new Identifier(variableName));
                stmt = new ShowVariable(var);
                break;
            case data_hidevariable:
            case data_hidelist:
                variableName = current.get(FIELDS_KEY).get(VARIABLE).get(FIELD_VALUE).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE).get(1).asText();
                variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                actorName = variableInfo.getActor();
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
