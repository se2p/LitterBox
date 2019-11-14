package scratch.newast.parser.symboltable;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Identifier;

public class ProcedureDefinitionNameMapping {
    private HashMap<Identifier, ProcedureInfo> procedures;

    public ProcedureDefinitionNameMapping() {
        procedures = new HashMap<>();
    }

    public void addProcedure(Identifier identifier, String procedureName, String[] argumentNames,
                             Type[] types) {

        procedures.put(identifier, new ProcedureInfo(procedureName, makeArguments(argumentNames, types)));

    }

    private ArgumentInfo[] makeArguments(String[] argumentNames, Type[] types) {
        Preconditions.checkArgument(argumentNames.length == types.length);
        ArgumentInfo[] arguments = new ArgumentInfo[argumentNames.length];
        for (int i = 0; i < argumentNames.length; i++) {
            arguments[i] = new ArgumentInfo(argumentNames[i], types[i]);
        }
        return arguments;
    }

    public HashMap<Identifier, ProcedureInfo> getProcedures() {
        return procedures;
    }
}
