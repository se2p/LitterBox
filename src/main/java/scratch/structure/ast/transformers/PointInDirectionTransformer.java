package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.stack.TurnDegreesBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PointInDirectionTransformer extends Transformer {
    @Override
    Set<String> getIdentifiers() {
        return new HashSet<>(Arrays.asList("motion_pointindirection"));
    }

    @Override
    BasicBlock transform(JsonNode node, Ast ast) {
        extractStandardValues(node);

        TurnDegreesBlock block;
        ArrayNode inputArray = (ArrayNode) node.get("inputs").get("DIRECTION");
        int inputType = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_TYPE).asInt();
        int inputShadow = inputArray.get(POS_INPUT_SHADOW).asInt();

        switch (inputType) {
        case ANGLE_NUM_PRIMITIVE: {
            int inputValue = inputArray.get(POS_DATA_ARRAY).get(POS_INPUT_VALUE).asInt();
            if (!topLevel) {
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, inputType, "DIRECTION", inputValue, inputShadow);
            } else {
                int x = node.get("x").intValue();
                int y = node.get("y").intValue();
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, x, y, inputType, "DIRECTION", inputValue, inputShadow);
            }
            break;
        }
        case VAR_PRIMITIVE: { // FIXME also store the value of the obscured input
            String inputVariableID = inputArray.get(POS_DATA_ARRAY).get(POS_VAR_ID).toString().replaceAll("^\"|\"$", "");
            if (!topLevel) {
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, inputType, "DIRECTION", inputVariableID, inputShadow);
            } else {
                int x = node.get("x").intValue();
                int y = node.get("y").intValue();
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, x, y, inputType, "DIRECTION", inputVariableID, inputShadow);
            }
            break;
        }
        default: {
            throw new RuntimeException("Unexpected input type: " + inputType); // TODO what is an appropriate error handling strategy here?
        }
        }

        return block;
    }
}
