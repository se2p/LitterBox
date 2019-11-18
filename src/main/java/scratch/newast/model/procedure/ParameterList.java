package scratch.newast.model.procedure;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

public class ParameterList implements ASTNode {

    private ParameterListPlain parameterListPlain;
    private final ImmutableList<ASTNode> children;

    public ParameterList(ParameterListPlain parameterListPlain) {
        this.parameterListPlain = parameterListPlain;
        ImmutableList.Builder<ASTNode> builder = ImmutableList.builder();
        children = builder.add(parameterListPlain).build();
    }

    public ParameterListPlain getParameterListPlain() {
        return parameterListPlain;
    }

    public void setParameterListPlain(ParameterListPlain parameterListPlain) {
        this.parameterListPlain = parameterListPlain;
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
