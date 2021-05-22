package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class StatementReplacementVisitor extends CloneVisitor {

    private Stmt target;
    private Set<Stmt> otherTargets = Sets.newIdentityHashSet();
    private List<Stmt> replacementStatements;

    public StatementReplacementVisitor(Stmt target, List<Stmt> replacement) {
        this.target = target;
        this.replacementStatements = replacement;
    }

    public StatementReplacementVisitor(Stmt target, Stmt... replacement) {
        this.target = target;
        this.replacementStatements = Arrays.asList(replacement);
    }

    public StatementReplacementVisitor(Stmt target, List<Stmt> otherTargets, List<Stmt> replacement) {
        this.target = target;
        this.otherTargets.addAll(otherTargets);
        this.replacementStatements = replacement;
    }

    protected boolean isTargetStatement(Stmt node) {
        return node == target;
    }


    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (isTargetStatement(stmt)) {
                for (Stmt replacement : replacementStatements)
                statements.add(replacement);
            } else if (!otherTargets.contains(stmt)) {
                statements.add(apply(stmt));
            }
        }
        return new StmtList(statements);
    }
}
