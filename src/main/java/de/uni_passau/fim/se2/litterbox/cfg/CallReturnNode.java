package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;

public class CallReturnNode extends CFGNode {

    private CallStmt originalCall;

    public CallReturnNode(CallStmt callNode) {
        this.originalCall = callNode;
    }
    @Override
    public ASTNode getASTNode() {
        return originalCall;
    }

    @Override
    public String toString() {
        return "Return "+originalCall.getIdent().getName()+"@" + System.identityHashCode(originalCall);
    }
}
