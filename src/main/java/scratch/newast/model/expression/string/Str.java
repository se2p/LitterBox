package scratch.newast.model.expression.string;

import com.google.common.collect.ImmutableList;
import scratch.newast.model.ASTLeaf;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;

//Concrete string: StringExpr ::= String
public class Str implements StringExpr, ASTLeaf {

    private final ImmutableList<ASTNode> children;
    private String str;

    public Str(String str) {
        this.str = str;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public String getStr() {
        return str;
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
        String[] returnArray = {str};
        return returnArray;
    }
}
