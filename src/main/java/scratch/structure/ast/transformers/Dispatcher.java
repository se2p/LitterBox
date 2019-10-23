package scratch.structure.ast.transformers;


import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.Ast;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.cblock.RepeatBlock;
import scratch.structure.ast.stack.*;

import java.util.*;

public class Dispatcher {

    public static Dispatcher dispatcher;


    static{
        dispatcher = new Dispatcher(); //Maybe get a container system or initialize somehow else
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(MoveStepBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_movesteps"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(PointInDirectionBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_pointindirection"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(TurnDegreesBlock.class.getName(), new HashSet<>(Arrays.asList("motion_turnright", "motion_turnleft"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeSizeByBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_changesizeby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetSizeToBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_setsizeto"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeVolumeByBlock.class.getName(), new HashSet<>(Collections.singletonList("sound_changevolumeby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetVolumeToBlock.class.getName(), new HashSet<>(Collections.singletonList("sound_setvolumeto"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(WaitSecondsBlock.class.getName(), new HashSet<>(Collections.singletonList("control_wait"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeXCoordinateByBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_changexby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(ChangeYCoordinateByBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_changeyby"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetXCoordinateToBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_setx"))));
        dispatcher.registerTransformer(SingleInputTransformerFactory.buildTransformer(SetYCoordinateToBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_sety"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(IfOnEdgeBounceBlock.class.getName(), new HashSet<>(Collections.singletonList("motion_ifonedgebounce"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(NextCostumeBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_nextcostume"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(NextBackdropBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_nextbackdrop"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(ClearGraphEffectsBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_cleargraphiceffects"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(ClearSoundEffectsBlock.class.getName(), new HashSet<>(Collections.singletonList("sound_cleareffects"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(StopAllSoundsBlock.class.getName(), new HashSet<>(Collections.singletonList("sound_stopallsounds"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(ResetTimerBlock.class.getName(), new HashSet<>(Collections.singletonList("sensing_resettimer"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(ShowBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_show"))));
        dispatcher.registerTransformer(NoInputTransformerFactory.buildTransformer(HideBlock.class.getName(), new HashSet<>(Collections.singletonList("looks_hide"))));
        dispatcher.registerTransformer(CBlockTransformerFactory.buildTransformer(RepeatBlock.class.getName(), new HashSet<>(Collections.singleton("control_repeat"))));

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
