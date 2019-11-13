package scratch.newast.model;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class DeclarationStmtList implements ASTNode {

    List<DeclarationStmt> declarationStmtList;
    private final ImmutableList<ASTNode> children;

    public DeclarationStmtList(List<DeclarationStmt> declarationStmtList) {
        this.declarationStmtList = declarationStmtList;
        children = ImmutableList.<ASTNode>builder().build();
    }

    public List<DeclarationStmt> getDeclarationStmtList() {
        return declarationStmtList;
    }

    public void setDeclarationStmtList(List<DeclarationStmt> declarationStmtList) {
        this.declarationStmtList = declarationStmtList;
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
