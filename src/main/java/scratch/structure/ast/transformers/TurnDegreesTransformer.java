package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.stack.StackBlock;
import scratch.structure.ast.stack.TurnDegreesBlock;

import java.util.Iterator;
import java.util.Map;

public class TurnDegreesTransformer implements Transformer {
    @Override
    public String getIdentifier() {
        return "motion_turnright"; // TODO there is more than one identifier - maybe use a Set instead
    }

    @Override
    public TurnDegreesBlock transform(JsonNode node, Ast ast) {

        String opcode = node.get("opcode").toString();
        boolean topLevel = node.get("topLevel").asBoolean();
        boolean shadow = node.get("shadow").asBoolean();

        int inputValue;
        ArrayNode input = (ArrayNode) node.get("inputs").get("DEGREES");
        if(input.get(1).get(0).asInt() == 4) { // TODO if this is not 4 there was a variable entered, also cover this case
            inputValue = input.get(1).get(1).asInt();
        } else {
            inputValue = Integer.MIN_VALUE; // TODO implement the variable case
        }

        TurnDegreesBlock block;
        if (!topLevel) {
            block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, "DEGREES", inputValue);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            block = new TurnDegreesBlock(opcode, null, null, shadow, topLevel, x, y, "DEGREES", inputValue);
        }

        return block;
    }
}
