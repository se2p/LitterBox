package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.stack.MoveStepBlock;
import scratch.structure.ast.stack.StackBlock;

public class MoveStepTransformer implements Transformer {
    @Override
    public String getIdentifier() {
        return "motion_movesteps";
    }

    @Override
    public BasicBlock transform(JsonNode node, Ast ast) {

        String parent = node.get("parent").toString();
        JsonNode inputs = node.get("inputs");
        int inputShadow = inputs.get("STEPS").get(0).asInt(); //Is there a nice way to access those values?
        int steps = inputs.get("STEPS").get(1).get(1).asInt();

        boolean topLevel = node.get("topLevel").asBoolean();
        boolean shadow = node.get("shadow").asBoolean();

        //remove quotes around string
        parent = parent.replaceAll("^\"|\"$", "");

        Extendable parentNode = null;
        if (!parent.equals("null") && ast != null) {
            parentNode = (Extendable) ast.getNodesIdMap().get(parent);
        }

        MoveStepBlock block;
        if (!topLevel) {
            block = new MoveStepBlock(this.getIdentifier(), null, null, steps, inputShadow, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("x").intValue();
            block = new MoveStepBlock(this.getIdentifier(), null, null, steps, inputShadow, shadow, topLevel, x, y);
        }


        if (parentNode != null) {
            parentNode.setNext(block);
        }

        return block;
    }
}
