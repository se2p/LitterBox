package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.BlockingIfElse;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the BlockingIfElse bug by appending StopThisScript to both branches.
 */
public class BlockingIfElseInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new BlockingIfElse();
    }

    @Override
    public void visit(IfElseStmt node) {
        // Determine if this if-else is in a context where blocking matters
        boolean isNotLastStatement = isNotLastStatement(node);
        boolean isInsideLoop = isInsideLoop(node);

        if (isNotLastStatement || isInsideLoop) {
            addMutation(node, clonedIfElse -> {
                StopThisScript stopThen = new StopThisScript(NonDataBlockMetadata.emptyNonBlockMetadata());
                clonedIfElse.getThenStmts().getStmts().add(stopThen);
                stopThen.setParentNode(clonedIfElse.getThenStmts());

                StopThisScript stopElse = new StopThisScript(NonDataBlockMetadata.emptyNonBlockMetadata());
                clonedIfElse.getElseStmts().getStmts().add(stopElse);
                stopElse.setParentNode(clonedIfElse.getElseStmts());
            });
        }
        super.visit(node);
    }

    private boolean isInsideLoop(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof LoopStmt) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private boolean isNotLastStatement(ASTNode node) {
        ASTNode parent = node.getParentNode();
        if (parent instanceof StmtList stmtList) {
            int index = stmtList.getStmts().indexOf(node);
            return index < stmtList.getStmts().size() - 1;
        }
        return false;
    }
}
