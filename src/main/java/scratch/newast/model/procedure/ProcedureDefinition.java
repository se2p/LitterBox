package scratch.newast.model.procedure;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.StmtList;
import scratch.newast.model.variable.Identifier;

public class ProcedureDefinition implements ASTNode {

    private Identifier ident;
    private ParameterList parameterList;
    private StmtList stmtList;
    private final ImmutableList<ASTNode> children;

    public ProcedureDefinition(Identifier ident, ParameterList parameterList, StmtList stmtList) {
        this.ident = ident;
        this.parameterList = parameterList;
        this.stmtList = stmtList;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(ident).add(parameterList).add(stmtList).build();
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

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }
}
