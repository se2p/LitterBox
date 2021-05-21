package de.uni_passau.fim.se2.litterbox.ast.model.statement.control;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;

public interface LoopStmt extends ControlStmt {

    StmtList getStmtList();
}
