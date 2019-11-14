package scratch.newast.model.statement.spritelook;

import com.google.common.collect.ImmutableList;
import java.util.List;
import scratch.newast.model.ASTNode;
import scratch.newast.model.ScratchVisitor;
import scratch.newast.model.statement.Stmt;

public class ListOfStmt implements ASTNode {

    List<Stmt> listOfStmt;

    public ListOfStmt(List<Stmt> listOfStmt) {
        this.listOfStmt = listOfStmt;
    }

    public List<Stmt> getListOfStmt() {
        return listOfStmt;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ImmutableList<ASTNode> getChildren() {
        throw new RuntimeException("Not Implemented yet");
    }
}
