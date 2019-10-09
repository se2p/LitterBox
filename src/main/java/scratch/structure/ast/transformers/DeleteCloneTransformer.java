package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.cap.DeleteCloneBlock;
import scratch.structure.ast.hat.WhenFlagClickedBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DeleteCloneTransformer extends Transformer {

    @Override
    public Set<String> getIdentifiers() {
       return new HashSet<>(Arrays.asList("control_delete_this_clone"));
    }

    @Override
    public BasicBlock transform(JsonNode node, Ast ast) {
        extractStandardValues(node);

        DeleteCloneBlock block;

        if (!topLevel) {
            block = new DeleteCloneBlock(opcode, null, shadow, topLevel, 0, 0);
        } else {
            int x = node.get("x").intValue();
            int y = node.get("y").intValue();
            block = new DeleteCloneBlock(opcode, null, shadow, topLevel, x, y);
        }

        return block;

    }
}
