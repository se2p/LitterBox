package scratch.newast.parser.symboltable;

import com.google.common.base.Preconditions;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.ProcedureOpcode;

import java.util.HashMap;

public class ProcedureDefinitionNameMapping {
    private HashMap<Identifier, ProcedureInfo> procedures;

    public ProcedureDefinitionNameMapping() {
        procedures = new HashMap<>();
    }

    public void addProcedure(Identifier identifier, String procedureName, String[] argumentNames,
                             ProcedureOpcode[] opcodes) {

        procedures.put(identifier, new ProcedureInfo(procedureName, makeArguments(argumentNames, opcodes)));

    }

    private ArgumentInfo[] makeArguments(String[] argumentNames, ProcedureOpcode[] opcodes) {
        Preconditions.checkArgument(argumentNames.length == opcodes.length);
        ArgumentInfo[] arguments = new ArgumentInfo[argumentNames.length];
        for (int i = 0; i < argumentNames.length; i++) {
            arguments[i] = new ArgumentInfo(argumentNames[i], opcodes[i]);
        }
        return arguments;
    }

    public HashMap<Identifier, ProcedureInfo> getProcedures() {
        return procedures;
    }
}
