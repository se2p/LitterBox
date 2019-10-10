package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.BasicBlock;

public abstract class SingleInputTransformer extends Transformer {

    public BasicBlock transform(JsonNode node, String inputName, int inputType, String className) {
        return null;
    }
}
