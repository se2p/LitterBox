package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.hat.WhenFlagClickedBlock;

public class WhenFlagClickedTransformer implements Transformer {

    @Override
    public String getIdentifier() {
        return "event_whenflagclicked";
    }

    @Override
    public BasicBlock transform(JsonNode node, Ast ast) {
        boolean topLevel = node.get("topLevel").asBoolean();
        boolean shadow = node.get("shadow").asBoolean();

        WhenFlagClickedBlock block;
        if (!topLevel) {
            block = new WhenFlagClickedBlock(this.getIdentifier(), null, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("x").intValue();
            block = new WhenFlagClickedBlock(this.getIdentifier(), null, shadow, topLevel, x, y);
        }

        return block;

    }
}
