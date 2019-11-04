package scratch.newast.model.procedure;

import scratch.newast.model.StmtList;
import scratch.newast.model.variable.Identifier;

public class ProcedureDeclarationList {

    Identifier identifier;
    ParameterList parameterList;
    StmtList stmtList;

    public ProcedureDeclarationList(Identifier identifier, ParameterList parameterList, StmtList stmtList) {
        this.identifier = identifier;
        this.parameterList = parameterList;
        this.stmtList = stmtList;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public ParameterList getParameterList() {
        return parameterList;
    }

    public void setParameterList(ParameterList parameterList) {
        this.parameterList = parameterList;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    public void setStmtList(StmtList stmtList) {
        this.stmtList = stmtList;
    }
}
