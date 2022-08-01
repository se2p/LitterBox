package de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led;

import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.LEDPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;

public interface PositionStmt extends MBlockStmt {
    LEDPosition getPosition();
}
