package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;

import java.util.Set;

public interface Transformer {

    //Returns the opcode(s)/id(s) this transformer works for.
    Set<String> getIdentifiers();

    BasicBlock transform(JsonNode node, Ast ast);
}
