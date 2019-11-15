package scratch.newast.parser.stmt;

import static scratch.newast.Constants.DRAGMODE_KEY;
import static scratch.newast.Constants.DRAG_KEY;
import static scratch.newast.Constants.EFFECT_KEY;
import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.ROTATIONSTYLE_KEY;
import static scratch.newast.Constants.STYLE_KEY;
import static scratch.newast.Constants.VARIABLE_IDENTIFIER_POS;
import static scratch.newast.Constants.VARIABLE_KEY;
import static scratch.newast.Constants.VOLUME_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.string.Str;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.common.SetAttributeTo;
import scratch.newast.model.statement.common.SetStmt;
import scratch.newast.model.statement.common.SetVariableTo;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.opcodes.SetStmtOpcode;
import scratch.newast.parser.ExpressionParser;
import scratch.newast.parser.ProgramParser;
import scratch.newast.parser.attributes.DragMode;
import scratch.newast.parser.attributes.GraphicEffect;
import scratch.newast.parser.attributes.RotationStyle;
import scratch.newast.parser.attributes.SoundEffect;
import scratch.newast.parser.symboltable.VariableInfo;

public class SetStmtParser {
    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SetStmtOpcode.contains(opcodeString), "Given blockID does not point to a set block.");

        SetStmtOpcode opcode = SetStmtOpcode.valueOf(opcodeString);
        SetStmt stmt;

        switch (opcode) {
            case data_setvariableto:
                stmt = parseSetVariable(current, allBlocks);
                return stmt;
            case sensing_setdragmode:
                stmt = parseSetDragmode(current);
                return stmt;
            case motion_setrotationstyle:
                stmt = parseSetRotationStyle(current);
                return stmt;
            case looks_seteffectto:
                stmt = parseSetLookEffect(current, allBlocks);
                return stmt;
            case sound_seteffectto:
                stmt = parseSetSoundEffect(current, allBlocks);
                return stmt;
            case sound_setvolumeto:
                stmt = parseSetVolumeTo(current, allBlocks);
                return stmt;
            default:
                throw new RuntimeException("Not Implemented yet");
        }
    }

    private static SetStmt parseSetVolumeTo(JsonNode current, JsonNode allBlocks) throws ParsingException {
        return new SetAttributeTo(new Str(VOLUME_KEY), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetSoundEffect(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).textValue();
        Preconditions.checkArgument(SoundEffect.contains(effect));
        return new SetAttributeTo(new Str(effect), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetLookEffect(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).textValue();
        Preconditions.checkArgument(GraphicEffect.contains(effect));
        return new SetAttributeTo(new Str(effect), ExpressionParser.parseExpression(current, 0,
                allBlocks));
    }

    private static SetStmt parseSetRotationStyle(JsonNode current) {
        String rota = current.get(FIELDS_KEY).get(STYLE_KEY).get(0).textValue();
        Preconditions.checkArgument(RotationStyle.contains(rota));
        return new SetAttributeTo(new Str(ROTATIONSTYLE_KEY), new Str(rota));

    }

    private static SetStmt parseSetDragmode(JsonNode current) {
        String drag = current.get(FIELDS_KEY).get(DRAGMODE_KEY).get(0).textValue();
        Preconditions.checkArgument(DragMode.contains(drag));
        return new SetAttributeTo(new Str(DRAG_KEY), new Str(drag));
    }

    private static SetStmt parseSetVariable(JsonNode current, JsonNode allBlocks) throws ParsingException {
        String unique = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).textValue();
        Preconditions.checkArgument(ProgramParser.symbolTable.getVariables().containsKey(unique));
        VariableInfo info = ProgramParser.symbolTable.getVariables().get(unique);
        return new SetVariableTo(new Qualified(new Identifier(info.getActor()),
            new Identifier((info.getVariableName()))), ExpressionParser.parseExpression(current,
                0, allBlocks));
    }
}
