package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.variable.Identifier;

public class Program implements ASTNode {
    private Identifier ident;
    private ScriptGroupList scriptGroupList;

    public Program(Identifier ident, ScriptGroupList scriptGroupList) {
        this.ident = ident;
        this.scriptGroupList = scriptGroupList;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public ScriptGroupList getScriptGroupList() {
        return scriptGroupList;
    }

    public void setScriptGroupList(ScriptGroupList scriptGroupList) {
        this.scriptGroupList = scriptGroupList;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<Object> getChildren() {
        return null;
    }
}