package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.hat.WhenStartAsCloneBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WhenStartAsCloneTransformer extends Transformer {
    @Override
    Set<String> getIdentifiers() {
        return new HashSet<>(Arrays.asList("control_start_as_clone"));
    }

    @Override
    BasicBlock transform(JsonNode node, Ast ast) {
        extractStandardValues(node);

        WhenStartAsCloneBlock block;
        if (!topLevel) {
            block = new WhenStartAsCloneBlock(opcode, null, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            block = new WhenStartAsCloneBlock(opcode, null, shadow, topLevel, x, y);
        }

        return block;
    }
}
