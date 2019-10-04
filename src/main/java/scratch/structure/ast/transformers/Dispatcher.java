package scratch.structure.ast.transformers;


import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {

    public static Dispatcher dispatcher;

    static{
        dispatcher = new Dispatcher(); //Maybe get a container system or initialize somehow else
        dispatcher.registerTransformer(new MoveStepTransformer());
        dispatcher.registerTransformer(new WhenFlagClickedTransformer());
        dispatcher.registerTransformer(new TurnDegreesTransformer());
    }

    private Map<String, Transformer> transformerMap = new HashMap<>();

    public BasicBlock transform(String opcode, JsonNode node, Ast ast) {
        Transformer transformer = transformerMap.get(opcode);

        if (transformer == null) {
            //Todo check if this is the correct exception
            throw new IllegalArgumentException("No transformer for opcode '" + opcode + "' registered");
        }

        BasicBlock block = transformer.transform(node, ast);
        return block;
    }

    public void registerTransformer(Transformer transformer) {
        transformerMap.put(transformer.getIdentifier(), transformer);
    }

    public Object[] transformArray(String fields, JsonNode fields1) {
        //Fixme, return something more useful
        return new Object[0];
    }
}
