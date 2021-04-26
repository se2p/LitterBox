package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class DeleteControlBlock implements Refactoring {

    private final Script script;
    private final StmtList stmtList;
    private static final String NAME = "delete_control_block";

    public DeleteControlBlock(Script script) {
        this.script = Preconditions.checkNotNull(script);
        this.stmtList = script.getStmtList();
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy();
        for (Stmt stmt : stmtList.getStmts()) {
            if (stmt instanceof ControlStmt) {
                stmtList.getStmts().remove(stmt);
                break;
            }
        }
        return refactored;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + script.getUniqueName() + ")";
    }

}
