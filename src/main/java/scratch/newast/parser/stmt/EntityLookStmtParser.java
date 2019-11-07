package scratch.newast.parser.stmt;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.opcodes.EntityLookStmtOpcode.looks_changeeffectby;
import static scratch.newast.opcodes.EntityLookStmtOpcode.looks_seteffectto;
import static scratch.newast.opcodes.EntityLookStmtOpcode.looks_switchbackdropto;
import static scratch.newast.opcodes.EntityLookStmtOpcode.sensing_askandwait;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.backdrop.Backdrop;
import scratch.newast.model.backdrop.BackdropWithId;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.expression.string.StringExpr;
import scratch.newast.model.graphiceffect.GraphicEffect;
import scratch.newast.model.statement.entitylook.AskAndWait;
import scratch.newast.model.statement.entitylook.ChangeEffectBy;
import scratch.newast.model.statement.entitylook.ClearGraphicEffects;
import scratch.newast.model.statement.entitylook.EntityLookStmt;
import scratch.newast.model.statement.entitylook.SwitchBackdrop;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.EntityLookStmtOpcode;
import scratch.newast.opcodes.EventOpcode;
import scratch.newast.parser.ExpressionParser;
import scratch.newast.parser.GraphicEffectParser;

public class EntityLookStmtParser {

    private static final String INPUTS = "inputs";
    private static final String ASKANDWAIT_INPUT_KEY = "QUESTION";
    private static final String CHANGE_EFFECTBY_INPUT_KEY = "CHANGE";
    private static final String SET_EFFECTTO_INPUT_KEY = "CHANGE";
    private static final String SWITCH_BACKDROPTO_INPUT_KEY = "BACKDROP";
    private static final String EFFECTS_FIELD_KEY = "EFFECT";


    public static EntityLookStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(EventOpcode.contains(opcodeString), "Given blockID does not point to an event block.");

        EntityLookStmtOpcode opcode = EntityLookStmtOpcode.valueOf(opcodeString);
        EntityLookStmt stmt;

        if (opcode.equals(sensing_askandwait)) {
            JsonNode questionNode = current.get(INPUTS).get(ASKANDWAIT_INPUT_KEY).get(Constants.POS_DATA_ARRAY);
            StringExpr question = ExpressionParser.parseStringExpr(questionNode);
            stmt = new AskAndWait(question);
        } else if (opcode.equals(looks_switchbackdropto)) {
            JsonNode backdropNodeId = current.get(INPUTS).get(CHANGE_EFFECTBY_INPUT_KEY).get(Constants.POS_DATA_ARRAY)
                .get(Constants.POS_INPUT_VALUE);
            JsonNode backdropMenu = allBlocks.get(backdropNodeId.asText());
            String backdropName = backdropMenu.get(FIELDS_KEY).get(SWITCH_BACKDROPTO_INPUT_KEY).get(FIELD_VALUE)
                .asText();

            Backdrop backdrop = new BackdropWithId(new Identifier(backdropName));
            stmt = new SwitchBackdrop(backdrop);
        } else if (opcode.equals(looks_changeeffectby)) {
            JsonNode effectValueNode = current.get(INPUTS).get(CHANGE_EFFECTBY_INPUT_KEY).get(Constants.POS_DATA_ARRAY);
            NumExpr effectValue = ExpressionParser.parseNumExpr(effectValueNode);
            String fieldValue = current.get(FIELDS_KEY).get(EFFECTS_FIELD_KEY).get(Constants.FIELD_VALUE).asText();
            GraphicEffect effect = GraphicEffectParser.parse(fieldValue);
            stmt = new ChangeEffectBy(effect, effectValue);

        } else if (opcode.equals(looks_seteffectto)) {
            JsonNode effectValueNode = current.get(INPUTS).get(SET_EFFECTTO_INPUT_KEY).get(Constants.POS_DATA_ARRAY);
            NumExpr effectValue = ExpressionParser.parseNumExpr(effectValueNode);
            String fieldValue = current.get(FIELDS_KEY).get(EFFECTS_FIELD_KEY).get(Constants.FIELD_VALUE).asText();
            GraphicEffect effect = GraphicEffectParser.parse(fieldValue);
            stmt = new ChangeEffectBy(effect, effectValue);
        } else if (opcode.equals(EntityLookStmtOpcode.looks_cleargraphiceffects)) {
            stmt = new ClearGraphicEffects();
        } else {
            throw new ParsingException("No parser for opcode " + opcodeString);
        }

        return stmt;

    }
}
