package scratch.newast.parser.symboltable;

import scratch.newast.opcodes.ProcedureOpcode;

public class ArgumentInfo {
    String name;
    ProcedureOpcode type;

    public ArgumentInfo(String name, ProcedureOpcode type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ProcedureOpcode getType() {
        return type;
    }
}
