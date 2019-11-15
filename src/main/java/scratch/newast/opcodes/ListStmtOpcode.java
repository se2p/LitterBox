package scratch.newast.opcodes;

public enum ListStmtOpcode {
    data_replaceitemoflist, data_insertatlist, data_deletealloflist, data_deleteoflist, data_addtolist;

    public static boolean contains(String opcode) {
        for (ListStmtOpcode value : ListStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
