package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Abstract base class for pen-related bug injectors that remove pen statements.
 * <p>
 * Precondition: The program must have BOTH PenUp and PenDown statements.
 * Subclasses specify which statement type to remove.
 */
public abstract class AbstractPenInjector extends AbstractBugInjector {

    private boolean hasPenUp = false;
    private boolean hasPenDown = false;

    /**
     * Returns the class of the pen statement type to remove.
     *
     * @return PenUpStmt.class or PenDownStmt.class
     */
    protected abstract Class<? extends Stmt> getStatementTypeToRemove();

    @Override
    public List<Program> inject(Program program) {
        // Check precondition: both PenUp and PenDown must exist
        hasPenUp = false;
        hasPenDown = false;
        checkPenStatements(program);

        if (!hasPenUp || !hasPenDown) {
            // Bug already exists or no pen statements - skip injection
            return List.of();
        }

        return super.inject(program);
    }

    private void checkPenStatements(Program program) {
        checkNode(program);
    }

    private void checkNode(ASTNode node) {
        if (node instanceof PenUpStmt) {
            hasPenUp = true;
        } else if (node instanceof PenDownStmt) {
            hasPenDown = true;
        }
        for (var child : node.getChildren()) {
            checkNode(child);
        }
    }

    @Override
    public void visit(StmtList node) {
        Class<? extends Stmt> targetType = getStatementTypeToRemove();

        // Only create mutation if this StmtList contains the target statement type
        boolean hasTarget = false;
        for (Stmt stmt : node.getStmts()) {
            if (targetType.isInstance(stmt)) {
                hasTarget = true;
                break;
            }
        }

        if (hasTarget) {
            addMutation(node, (StmtList clone) -> {
                // Remove ALL occurrences of the target statement type
                clone.getStmts().removeIf(targetType::isInstance);
            });
        }
        super.visit(node);
    }
}
