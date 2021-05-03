package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class DeleteControlBlock implements Refactoring {

    private final ControlStmt controlStmt;
    private static final String NAME = "delete_control_block";

    public DeleteControlBlock(ControlStmt controlStmt) {
        this.controlStmt = Preconditions.checkNotNull(controlStmt);
    }

    @Override
    public Program apply(Program program) {
        Program refactored = program.deepCopy();
        refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts().remove(controlStmt);
        return refactored;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "(" + controlStmt.getUniqueName() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeleteControlBlock)) {
            return false;
        }
        return controlStmt.equals(((DeleteControlBlock) other).controlStmt) ;
    }

    @Override
    public int hashCode() {
        return controlStmt.hashCode();
    }

}
