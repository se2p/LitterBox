package scratch.structure.ast.transformers;


import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.BasicBlock;
import scratch.structure.ast.stack.ChangeSizeByBlock;
import scratch.structure.ast.stack.ChangeVolumeByBlock;
import scratch.structure.ast.stack.MoveStepBlock;
import scratch.structure.ast.stack.PointInDirectionBlock;
import scratch.structure.ast.stack.SetSizeToBlock;
import scratch.structure.ast.stack.TurnDegreesBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Dispatcher {

    public static Dispatcher dispatcher;


    static{
        dispatcher = new Dispatcher(); //Maybe get a container system or initialize somehow else
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(MoveStepBlock.class.getName(), new HashSet<>(Arrays.asList("motion_movesteps")), Transformer.MATH_NUM_PRIMITIVE, "STEPS"));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(PointInDirectionBlock.class.getName(), new HashSet<>(Arrays.asList("motion_pointindirection")), Transformer.ANGLE_NUM_PRIMITIVE, "DIRECTION"));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(TurnDegreesBlock.class.getName(), new HashSet<>(Arrays.asList("motion_turnright", "motion_turnleft")), Transformer.MATH_NUM_PRIMITIVE, "DEGREES"));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeSizeByBlock.class.getName(), new HashSet<>(Arrays.asList("looks_changesizeby")), Transformer.MATH_NUM_PRIMITIVE, "CHANGE"));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetSizeToBlock.class.getName(), new HashSet<>(Arrays.asList("looks_setsizeto")), Transformer.MATH_NUM_PRIMITIVE, "SIZE"));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeVolumeByBlock.class.getName(), new HashSet<>(Arrays.asList("sound_changevolumeby")), Transformer.MATH_NUM_PRIMITIVE, "VOLUME"));

        dispatcher.registerTransformer(new WhenFlagClickedTransformer());
        dispatcher.registerTransformer(new DeleteCloneTransformer());
        dispatcher.registerTransformer(new WhenSpriteClickedTransformer());
        dispatcher.registerTransformer(new WhenStartAsCloneTransformer());
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
