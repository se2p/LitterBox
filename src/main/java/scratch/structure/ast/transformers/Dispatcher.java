package scratch.structure.ast.transformers;


import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.stack.ChangeSizeByBlock;
import scratch.structure.ast.stack.ChangeVolumeByBlock;
import scratch.structure.ast.stack.ChangeXCoordinateByBlock;
import scratch.structure.ast.stack.ChangeYCoordinateByBlock;
import scratch.structure.ast.stack.MoveStepBlock;
import scratch.structure.ast.stack.PointInDirectionBlock;
import scratch.structure.ast.stack.SetSizeToBlock;
import scratch.structure.ast.stack.SetVolumeToBlock;
import scratch.structure.ast.stack.SetXCoordinateToBlock;
import scratch.structure.ast.stack.SetYCoordinateToBlock;
import scratch.structure.ast.stack.TurnDegreesBlock;
import scratch.structure.ast.stack.WaitSecondsBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Dispatcher {

    public static Dispatcher dispatcher;


    static{
        dispatcher = new Dispatcher(); //Maybe get a container system or initialize somehow else
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(MoveStepBlock.class.getName(), new HashSet<>(Arrays.asList("motion_movesteps"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(PointInDirectionBlock.class.getName(), new HashSet<>(Arrays.asList("motion_pointindirection"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(TurnDegreesBlock.class.getName(), new HashSet<>(Arrays.asList("motion_turnright", "motion_turnleft"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeSizeByBlock.class.getName(), new HashSet<>(Arrays.asList("looks_changesizeby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetSizeToBlock.class.getName(), new HashSet<>(Arrays.asList("looks_setsizeto"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeVolumeByBlock.class.getName(), new HashSet<>(Arrays.asList("sound_changevolumeby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetVolumeToBlock.class.getName(), new HashSet<>(Arrays.asList("sound_setvolumeto"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(WaitSecondsBlock.class.getName(), new HashSet<>(Arrays.asList("control_wait"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeXCoordinateByBlock.class.getName(), new HashSet<>(Arrays.asList("motion_changexby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeYCoordinateByBlock.class.getName(), new HashSet<>(Arrays.asList("motion_changeyby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetXCoordinateToBlock.class.getName(), new HashSet<>(Arrays.asList("motion_setx"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetYCoordinateToBlock.class.getName(), new HashSet<>(Arrays.asList("motion_sety"))));

        dispatcher.registerTransformer(new WhenFlagClickedTransformer());
        dispatcher.registerTransformer(new DeleteCloneTransformer());
        dispatcher.registerTransformer(new WhenSpriteClickedTransformer());
        dispatcher.registerTransformer(new WhenStartAsCloneTransformer());
    }

    private Map<String, Transformer> transformerMap = new HashMap<>();

    public ScratchBlock transform(String opcode, JsonNode node, Ast ast) {
        Transformer transformer = transformerMap.get(opcode);

        if (transformer == null) {
            //TODO check if this is the correct exception
            //FIXME comment this in again as soon as the slot refactoring is done and the tests do not
            // include blocks which we haven't implemented yet
            //throw new IllegalArgumentException("No transformer for opcode '" + opcode + "' registered");
            System.err.println("No transformer for opcode '" + opcode + "' registered");
            return null;
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
