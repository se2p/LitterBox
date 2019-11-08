package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class ScriptList implements ASTNode {

    List<Script> scriptList;
    private final ImmutableList<ASTNode> children;

    public ScriptList(List<Script> scriptList) {
        this.scriptList = scriptList;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public List<Script> getScriptList() {
        return scriptList;
    }

    public void setScriptList(List<Script> scriptList) {
        this.scriptList = scriptList;
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
