package scratch.newast.model.procedure;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ProcedureDeclarationList implements ASTNode {

    private List<ProcedureDeclaration> list;
    private final ImmutableList<ASTNode> children;

    public ProcedureDeclarationList(List<ProcedureDeclaration> list) {
        this.list = list;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public List<ProcedureDeclaration> getList() {
        return list;
    }

    public void setList(List<ProcedureDeclaration> list) {
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
