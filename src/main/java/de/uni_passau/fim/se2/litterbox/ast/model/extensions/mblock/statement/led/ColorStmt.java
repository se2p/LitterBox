package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;

public interface ColorStmt extends MBlockStmt {
    StringExpr getColorString();
}
