package scratch.structure.ast.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.structure.ast.ScratchBlock;
import scratch.structure.ast.bool.KeyPressedBlock;
import scratch.structure.ast.bool.OperatorGT;
import scratch.structure.ast.bool.OperatorNot;
import scratch.structure.ast.cap.DeleteCloneBlock;
import scratch.structure.ast.cblock.IfBlock;
import scratch.structure.ast.cblock.RepeatBlock;
import scratch.structure.ast.dynamicMenu.KeyOptionsBlock;
import scratch.structure.ast.hat.WhenFlagClickedBlock;
import scratch.structure.ast.hat.WhenSpriteClickedBlock;
import scratch.structure.ast.hat.WhenStartAsCloneBlock;
import scratch.structure.ast.reporter.OperatorAdd;
import scratch.structure.ast.stack.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Transformer {

    public static HashMap<String, Class<? extends ScratchBlock>> opCodeClassMapping = new HashMap();

    static {
        opCodeClassMapping.put("motion_movesteps", MoveStepBlock.class);
        opCodeClassMapping.put("motion_pointindirection", PointInDirectionBlock.class);
        opCodeClassMapping.put("motion_turnright", TurnDegreesBlock.class);
        opCodeClassMapping.put("motion_turnleft", TurnDegreesBlock.class);
        opCodeClassMapping.put("looks_changesizeby", ChangeSizeByBlock.class);
        opCodeClassMapping.put("looks_setsizeto", SetSizeToBlock.class);
        opCodeClassMapping.put("sound_changevolumeby", ChangeVolumeByBlock.class);
        opCodeClassMapping.put("sound_setvolumeto", SetVolumeToBlock.class);
        opCodeClassMapping.put("control_wait", WaitSecondsBlock.class);
        opCodeClassMapping.put("motion_changexby", ChangeXCoordinateByBlock.class);
        opCodeClassMapping.put("motion_changeyby", ChangeYCoordinateByBlock.class);
        opCodeClassMapping.put("motion_setx", SetXCoordinateToBlock.class);
        opCodeClassMapping.put("motion_sety", SetYCoordinateToBlock.class);
        opCodeClassMapping.put("motion_ifonedgebounce", IfOnEdgeBounceBlock.class);
        opCodeClassMapping.put("looks_nextcostume", NextCostumeBlock.class);
        opCodeClassMapping.put("looks_nextbackdrop", NextBackdropBlock.class);
        opCodeClassMapping.put("looks_cleargraphiceffects", ClearGraphEffectsBlock.class);
        opCodeClassMapping.put("sound_cleareffects", ClearSoundEffectsBlock.class);
        opCodeClassMapping.put("sound_stopallsounds", StopAllSoundsBlock.class);
        opCodeClassMapping.put("sensing_resettimer", ResetTimerBlock.class);
        opCodeClassMapping.put("looks_show", ShowBlock.class);
        opCodeClassMapping.put("looks_hide", HideBlock.class);
        opCodeClassMapping.put("control_repeat", RepeatBlock.class);
        opCodeClassMapping.put("control_if", IfBlock.class);
        opCodeClassMapping.put("control_start_as_clone", WhenStartAsCloneBlock.class);
        opCodeClassMapping.put("control_delete_this_clone", DeleteCloneBlock.class);
        opCodeClassMapping.put("event_whenthisspriteclicked", WhenSpriteClickedBlock.class);
        opCodeClassMapping.put("event_whenflagclicked", WhenFlagClickedBlock.class);
        opCodeClassMapping.put("operator_add", OperatorAdd.class);
        opCodeClassMapping.put("operator_not", OperatorNot.class);
        opCodeClassMapping.put("operator_gt", OperatorGT.class);
        opCodeClassMapping.put("sensing_keypressed", KeyPressedBlock.class);
        opCodeClassMapping.put("sensing_keyoptions", KeyOptionsBlock.class);
    }


    public static <T extends ScratchBlock> T transformGeneric(Class<T> clazz, JsonNode node, String id) {
        String opcode;
        boolean topLevel;
        boolean shadow;
        opcode = node.get("opcode").toString().replaceAll("^\"|\"$", "");
        topLevel = node.get("topLevel").asBoolean();
        shadow = node.get("shadow").asBoolean();

        T block;

        if (clazz == null) {
            //FIXME
            return null;
        }

        try {
            if (!topLevel) {
                Constructor<?> constructor = clazz.getConstructor(String.class, String.class, Boolean.class, Boolean.class);
                block = (T) constructor.newInstance(opcode, id, shadow, topLevel);
            } else {
                int x = node.get("x").intValue();
                int y = node.get("y").intValue();
                Constructor<?> constructor = clazz.getConstructor(String.class, String.class, Boolean.class, Boolean.class, Integer.class, Integer.class);
                block = (T) constructor.newInstance(opcode, id, shadow, topLevel, x, y);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Excuse me?"); //Todo use an exception that is also acceptable when code is published on github
        }

        return block;
    }


}
