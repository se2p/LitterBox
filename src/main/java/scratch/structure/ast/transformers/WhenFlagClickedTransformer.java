package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.hat.WhenFlagClickedBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WhenFlagClickedTransformer implements Transformer {

    @Override
    public Set<String> getIdentifiers() {
       return new HashSet<>(Arrays.asList("event_whenflagclicked"));
    }

    @Override
    public BasicBlock transform(JsonNode node, Ast ast) {
        String opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
        boolean topLevel = node.get("topLevel").asBoolean();
        boolean shadow = node.get("shadow").asBoolean();

        WhenFlagClickedBlock block;
        if (!topLevel) {
            block = new WhenFlagClickedBlock(opcode, null, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("x").intValue();
            block = new WhenFlagClickedBlock(opcode, null, shadow, topLevel, x, y);
        }

        return block;

    }
}
