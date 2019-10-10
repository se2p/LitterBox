package scratch.structure.ast.transformers;


import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.stack.MoveStepBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Dispatcher {

    public static Dispatcher dispatcher;


    static{
        dispatcher = new Dispatcher(); //Maybe get a container system or initialize somehow else
        //dispatcher.registerTransformer(new MoveStepTransformer());
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(MoveStepBlock.class.getName(), new HashSet<>(Arrays.asList("motion_movesteps")), Transformer.MATH_NUM_PRIMITIVE, "STEPS"));

        dispatcher.registerTransformer(new WhenFlagClickedTransformer());
        dispatcher.registerTransformer(new TurnDegreesTransformer());
        dispatcher.registerTransformer(new DeleteCloneTransformer());
        dispatcher.registerTransformer(new WhenSpriteClickedTransformer());
        dispatcher.registerTransformer(new WhenStartAsCloneTransformer());
        dispatcher.registerTransformer(new PointInDirectionTransformer());
    }

    private Map<String, Transformer> transformerMap = new HashMap<>();

    public BasicBlock transform(String opcode, JsonNode node, Ast ast) {
        Transformer transformer = transformerMap.get(opcode);

        if (transformer == null) {
            //Todo check if this is the correct exception
            throw new IllegalArgumentException("No transformer for opcode '" + opcode + "' registered");
        }

        return transformer.transform(node, ast);
    }

    public void registerTransformer(Transformer transformer) {
        for(String identifier: transformer.getIdentifiers()) {
            transformerMap.put(identifier, transformer);
        }
    }

    public Object[] transformArray(String fields, JsonNode fields1) {
        //Fixme, return something more useful
        return new Object[0];
    }
}
