package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.variable.Identifier;

public class Program implements ASTNode {
    private final Identifier ident;
    private final ScriptGroupList scriptGroupList;
    private final ImmutableList<ASTNode> children;

    public Program(Identifier ident, ScriptGroupList scriptGroupList) {
        this.ident = ident;
        this.scriptGroupList = scriptGroupList;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public Identifier getIdent() {
        return ident;
    }

    public ScriptGroupList getScriptGroupList() {
        return scriptGroupList;
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