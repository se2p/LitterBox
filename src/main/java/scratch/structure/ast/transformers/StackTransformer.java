package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.Extendable;
import scratch.structure.ast.Stackable;
import scratch.structure.ast.stack.StackBlock;

public class StackTransformer implements Transformer {
    @Override
    public String getIdentifier() {
        return "motion_movestep";
    }

    @Override
    public BasicBlock transform(JsonNode node, Ast ast) {

        String parent = node.get("parent").toString();
        boolean topLevel = node.get("topLevel").asBoolean();
        boolean shadow = node.get("shadow").asBoolean();

        parent = parent.replaceAll("^\"|\"$", ""); //remove quotes around string

        Extendable parentNode = null;
        if (!parent.equals("null") && ast != null) {
            parentNode = (Extendable) ast.getNodesIdMap().get(parent);
        }

        Stackable block;
        if (!topLevel) {
            block = new StackBlock(this.getIdentifier(), null, null, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("x").intValue();
            block = new StackBlock(this.getIdentifier(), null, null, shadow, topLevel, x, y);
        }


        if (parentNode != null) {
            parentNode.setNext(block);
        }

        return block;
    }
}
