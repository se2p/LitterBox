package scratch.ast.opcodes;

/**
 * These enums are for blocks which are only used inside other blocks and thus do not have to be parsed as statements
 * themselves.
 */
public enum DependentBlockOpcodes {

    motion_goto_menu, motion_glideto_menu, motion_pointtowards_menu,
    looks_costume, looks_backdrops, sound_sounds_menu, control_create_clone_of_menu,
    sensing_distancetomenu, sensing_touchingobjectmenu, sensing_keyoptions,
    sensing_of_object_menu, pen_menu_colorParam;

    public static boolean contains(String opcode) {
        for (DependentBlockOpcodes value : DependentBlockOpcodes.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
