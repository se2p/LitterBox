package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;

public interface Transformer {

    //Returns the opcode/id this transformer works for.
    String getIdentifier();

    BasicBlock transform(JsonNode node, Ast ast);
}
