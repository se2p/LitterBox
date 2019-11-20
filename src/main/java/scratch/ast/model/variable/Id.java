package scratch.ast.model.variable;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import scratch.ast.model.ASTNode;
import scratch.ast.visitor.ScratchVisitor;

public class Id extends Identifier {

    private final String value; // TODO check if this is correct
    private final ImmutableList<ASTNode> children;

    public Id(String value) {
        super(value);
        this.value = Preconditions.checkNotNull(value);
        this.children = ImmutableList.<ASTNode>builder().build();
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        return children;
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] returnArray = {value};
        return returnArray;
    }

}
