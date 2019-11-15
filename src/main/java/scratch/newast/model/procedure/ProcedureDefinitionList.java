package scratch.newast.model.procedure;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ProcedureDefinitionList implements ASTNode {

    private List<ProcedureDefinition> list;
    private final ImmutableList<ASTNode> children;

    public ProcedureDefinitionList(List<ProcedureDefinition> list) {
        this.list = list;
        children = ImmutableList.<ASTNode>builder().addAll(list).build();
    }

    public List<ProcedureDefinition> getList() {
        return list;
    }

    public void setList(List<ProcedureDefinition> list) {
        this.list = list;
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
