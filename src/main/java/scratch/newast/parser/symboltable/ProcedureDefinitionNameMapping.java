package scratch.newast.parser.symboltable;

import scratch.newast.model.variable.Identifier;

import java.util.HashMap;

public class ProcedureDefinitionNameMapping {
    private HashMap<Identifier, String>  procedureNames;
    private HashMap<Identifier, String[]> procedureArgumentNames;

    public ProcedureDefinitionNameMapping(){
        procedureNames = new HashMap<>();
        procedureArgumentNames = new HashMap<>();
    }

    public void addProcedure(Identifier identifier, String procedureName, String[] argumentNames){
        procedureNames.put(identifier, procedureName);
        procedureArgumentNames.put(identifier,argumentNames);
    }

    public HashMap<Identifier, String> getProcedureNames() {
        return procedureNames;
    }

    public HashMap<Identifier, String[]> getProcedureArgumentNames() {
        return procedureArgumentNames;
    }
}
