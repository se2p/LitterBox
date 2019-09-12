package scratch.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static scratch.data.OpCodeMapping.BlockShape.BOOLEAN;
import static scratch.data.OpCodeMapping.BlockShape.C;
import static scratch.data.OpCodeMapping.BlockShape.CAP;
import static scratch.data.OpCodeMapping.BlockShape.HAT;
import static scratch.data.OpCodeMapping.BlockShape.REPORTER;
import static scratch.data.OpCodeMapping.BlockShape.STACK;

public class OpCodeMapping {

    public static final List<String> hatBlockOpcodes = new ArrayList<>(Arrays.asList(
            "event_whenflagclicked",
            "event_whenkeypressed",
            "event_whenthisspriteclicked",
            "event_whenbroadcastreceived",
            "event_whenbackdropswitchesto",
            "control_start_as_clone",
            "event_whengreaterthan",
            "procedures_definition"
    ));
    public static final List<String> stackBlockOpcodes = new ArrayList<>(Arrays.asList(
            "motion_movesteps",
            "motion_turnright",
            "motion_turnleft",
            "motion_goto",
            "motion_gotoxy",
            "motion_glideto",
            "motion_glidesecstoxy",
            "motion_pointindirection",
            "motion_pointtowards",
            "motion_changexby",
            "motion_setx",
            "motion_changeyby",
            "motion_sety",
            "motion_ifonedgebounce",
            "motion_setrotationstyle",
            "looks_sayforsecs",
            "looks_say",
            "looks_thinkforsecs",
            "looks_think",
            "looks_switchcostumeto",
            "looks_switchbackdropto",
            "looks_nextcostume",
            "looks_nextbackdrop",
            "looks_changesizeby",
            "looks_setsizeto",
            "looks_changeeffectby",
            "looks_seteffectto",
            "looks_cleargraphiceffects",
            "looks_show",
            "looks_hide",
            "looks_gotofrontback",
            "looks_goforwardbackwardlayers",
            "sound_playuntildone",
            "sound_play",
            "sound_stopallsounds",
            "sound_changeeffectby",
            "sound_seteffectto",
            "sound_cleareffects",
            "sound_changevolumeby",
            "sound_setvolumeto",
            "event_broadcast",
            "event_broadcastandwait",
            "control_wait",
            "control_wait_until",
            "control_create_clone_of",
            "sensing_askandwait",
            "sensing_setdragmode",
            "sensing_resettimer",
            "data_setvariableto",
            "data_changevariableby",
            "data_showvariable",
            "data_hidevariable",
            "data_addtolist",
            "data_deleteoflist",
            "data_deletealloflist",
            "data_insertatlist",
            "data_replaceitemoflist",
            "data_showlist",
            "data_hidelist",
            "procedures_call"
    ));
    public static final List<String> booleanBlockOpcodes = new ArrayList<>(Arrays.asList(
            "sensing_touchingobject",
            "sensing_touchingcolor",
            "sensing_coloristouchingcolor",
            "sensing_keypressed",
            "sensing_mousedown",
            "operator_gt",
            "operator_lt",
            "operator_equals",
            "operator_and",
            "operator_or",
            "operator_not",
            "operator_contains",
            "data_listcontainsitem"
    ));
    public static final List<String> reporterBlockOpcodes = new ArrayList<>(Arrays.asList(
            "motion_xposition",
            "motion_yposition",
            "motion_direction",
            "looks_size",
            "looks_costumenumbername",
            "looks_backdropnumbername",
            "sound_volume",
            "sensing_distanceto",
            "sensing_answer",
            "sensing_mousex",
            "sensing_mousey",
            "sensing_loudness",
            "sensing_timer",
            "sensing_of",
            "sensing_current",
            "sensing_dayssince2000",
            "sensing_username",
            "operator_add",
            "operator_subtract",
            "operator_multiply",
            "operator_divide",
            "operator_random",
            "operator_join",
            "operator_letter_of",
            "operator_length",
            "operator_mod",
            "operator_round",
            "operator_mathop",
            "data_itemoflist",
            "data_itemnumoflist",
            "data_lengthoflist"
    ));
    public static final List<String> cBlockOpcodes = new ArrayList<>(Arrays.asList(
            "control_repeat",
            "control_forever",
            "control_if",
            "control_if_else",
            "control_repeat_until"
    ));
    public static final List<String> capBlockOpcodes = new ArrayList<>(Arrays.asList(
            "control_stop",
            "control_delete_this_clone"
    ));

    public static Map<String, BlockShape> opcodeShape;
    static {
        opcodeShape = new HashMap<>();
        for (String opcode : hatBlockOpcodes) {
            opcodeShape.put(opcode, HAT);
        }
        for (String opcode: stackBlockOpcodes) {
            opcodeShape.put(opcode, STACK);
        }
        for (String opcode: booleanBlockOpcodes) {
            opcodeShape.put(opcode, BOOLEAN);
        }
        for (String opcode: reporterBlockOpcodes) {
            opcodeShape.put(opcode, REPORTER);
        }
        for (String opcode: cBlockOpcodes) {
            opcodeShape.put(opcode, C);
        }
        for (String opcode: capBlockOpcodes) {
            opcodeShape.put(opcode, CAP);
        }
    }

    public enum BlockShape {
        HAT, STACK, BOOLEAN, REPORTER, C, CAP
    }
}
