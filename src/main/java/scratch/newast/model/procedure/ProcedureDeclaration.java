package scratch.newast.model.procedure;

import scratch.newast.model.StmtList;
import scratch.newast.model.variable.Identifier;

public class ProcedureDeclaration {

    Identifier ident;
    ParameterList parameterList;
    StmtList stmtList;

    public ProcedureDeclaration(Identifier ident, ParameterList parameterList, StmtList stmtList) {
        this.ident = ident;
        this.parameterList = parameterList;
        this.stmtList = stmtList;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
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
