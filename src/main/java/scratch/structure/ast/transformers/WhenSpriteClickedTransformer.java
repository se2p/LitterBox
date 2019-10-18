package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.hat.WhenSpriteClickedBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WhenSpriteClickedTransformer extends Transformer {

    @Override
    public Set<String> getIdentifiers() {
        return new HashSet<>(Arrays.asList("event_whenthisspriteclicked"));
    }

    @Override
    public ScratchBlock transform(JsonNode node, Ast ast) {
        extractStandardValues(node);

        WhenSpriteClickedBlock block;
        if (!topLevel) {
            block = new WhenSpriteClickedBlock(opcode, null, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            block = new WhenSpriteClickedBlock(opcode, null, shadow, topLevel, x, y);
        }

        return block;
    }
}
