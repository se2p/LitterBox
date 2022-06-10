package de.uni_passau.fim.se2.litterbox.ast.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.Arrays;
import java.util.List;

public class StatementInsertionVisitor extends OnlyCodeCloneVisitor {
    private final ASTNode parent;
    private final List<Stmt> replacementStatements;
    private final int position;

    public StatementInsertionVisitor(ASTNode parent, int position, List<Stmt> replacement) {
        this.parent = parent;
        this.replacementStatements = replacement;
        this.position = position;
    }

    public StatementInsertionVisitor(ASTNode parent, int position, Stmt... replacement) {
        this.parent = parent;
        this.replacementStatements = Arrays.asList(replacement);
        this.position = position;
    }

    protected boolean isTargetStatement(ASTNode node) {
        return node == parent;
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = applyList(node.getStmts());
        if (isTargetStatement(node.getParentNode())) {
            statements.addAll(position, replacementStatements);
        }
        return new StmtList(statements);
    }
}
