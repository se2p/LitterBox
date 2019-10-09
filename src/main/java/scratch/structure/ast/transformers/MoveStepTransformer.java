package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.stack.MoveStepBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MoveStepTransformer extends Transformer {
    @Override
    public Set<String> getIdentifiers() {
        return new HashSet<>(Arrays.asList("motion_movesteps"));
    }

    @Override
    public BasicBlock transform(JsonNode node, Ast ast) {

        extractStandardValues(node);

        String parent = node.get("parent").toString();
        JsonNode inputs = node.get("inputs");
        int inputShadow = inputs.get("STEPS").get(POS_INPUT_SHADOW).asInt(); //Is there a nice way to access those values?
        ArrayNode input = (ArrayNode) node.get("inputs").get("STEPS");
        int inputType = input.get(POS_DATA_ARRAY).get(POS_INPUT_TYPE).asInt(); // FIXME also cover the variable case
        int inputValue = inputs.get("STEPS").get(POS_DATA_ARRAY).get(POS_INPUT_VALUE).asInt();

        //remove quotes around string
        parent = parent.replaceAll("^\"|\"$", "");

        Extendable parentNode = null;
        if (!parent.equals("null") && ast != null) {
            parentNode = (Extendable) ast.getNodesIdMap().get(parent);
        }

        MoveStepBlock block;
        if (!topLevel) {
            block = new MoveStepBlock(opcode, null, null, shadow, topLevel, inputType, "STEPS", inputValue, inputShadow);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            block = new MoveStepBlock(opcode, null, null, shadow, topLevel, x, y, inputType, "STEPS", inputValue, inputShadow);
        }


        if (parentNode != null) {
            parentNode.setNext(block);
        }

        return block;
    }
}
