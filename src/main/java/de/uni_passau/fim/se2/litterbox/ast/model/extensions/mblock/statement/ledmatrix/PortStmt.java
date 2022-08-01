package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.ledmatrix;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.MCorePort;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;

public interface PortStmt extends MBlockStmt {
    MCorePort getPort();
}
