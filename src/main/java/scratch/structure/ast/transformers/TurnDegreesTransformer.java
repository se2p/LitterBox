package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.stack.TurnDegreesBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TurnDegreesTransformer extends Transformer {

    @Override
    public Set<String> getIdentifiers() {
        return new HashSet<>(Arrays.asList("motion_turnright", "motion_turnleft"));
    }

    @Override
    public TurnDegreesBlock transform(JsonNode node, Ast ast) {

        extractStandardValues(node);

        TurnDegreesBlock block;
        ArrayNode input = (ArrayNode) node.get("inputs").get("DEGREES");
        if(input.get(1).get(0).asInt() == MATH_NUM_PRIMITIVE) {
            int inputShadow = input.get(1).get(0).asInt(); //FIXME Use This value in the block
            int inputValue = input.get(1).get(1).asInt();
            if (!topLevel) {
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, "DEGREES", inputValue, inputShadow);
            } else {
                int x = node.get("x").intValue();
                int y = node.get("y").intValue();
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, x, y, "DEGREES", inputValue, inputShadow);
            }
        } else {
            int inputShadow = input.get(1).get(0).asInt(); //FIXME Use This value in the block
            String inputVariableID = input.get(1).get(2).toString().replaceAll("^\"|\"$", "");
            if (!topLevel) {
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, "DEGREES", inputVariableID, inputShadow);
            } else {
                int x = node.get("x").intValue();
                int y = node.get("y").intValue();
                block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, x, y, "DEGREES", inputVariableID, inputShadow);
            }
        }
        return block;
    }
}
