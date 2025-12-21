package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingEraseAll;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the MissingEraseAll bug by removing ALL PenClearStmt (erase all) blocks.
 * <p>
 * Precondition: The program must have a PenDown statement for the bug to be meaningful.
 */
public class MissingEraseAllInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MissingEraseAll();
    }

    @Override
    public void visit(Program node) {
        // Precondition: Must have PenDown
        if (!hasPenDown(node)) {
            return;
        }

        // Collect all PenClearStmt blocks
        List<PenClearStmt> penClearStmts = new ArrayList<>();
        collectPenClearStmts(node, penClearStmts);
        if (penClearStmts.isEmpty()) {
            return;
        }

        // Create ONE mutation that removes ALL erase all blocks
        addMutation(penClearStmts.getFirst(), clonedFirst -> {
            Program clonedProgram = findProgramRoot(clonedFirst);
            if (clonedProgram != null) {
                removeAllPenClearStmts(clonedProgram);
            }
        });
    }

    private boolean hasPenDown(ASTNode node) {
        if (node instanceof PenDownStmt) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (hasPenDown(child)) {
                return true;
            }
        }
        return false;
    }

    private void collectPenClearStmts(ASTNode node, List<PenClearStmt> result) {
        if (node instanceof PenClearStmt penClear) {
            result.add(penClear);
        }
        for (ASTNode child : node.getChildren()) {
            collectPenClearStmts(child, result);
        }
    }

    private void removeAllPenClearStmts(ASTNode node) {
        if (node instanceof StmtList stmtList) {
            stmtList.getStmts().removeIf(stmt -> stmt instanceof PenClearStmt);
        }
        for (ASTNode child : node.getChildren()) {
            removeAllPenClearStmts(child);
        }
    }
}
