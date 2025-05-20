/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class PenStmtParserTest implements JsonTest {

    @Test
    public void parseAllBlocks() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/stmtParser/penStmts.json");
        List<Stmt> stmts = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0).getStmtList().getStmts();
        Assertions.assertEquals(10, stmts.size());
        Assertions.assertInstanceOf(PenClearStmt.class, stmts.get(0));
        Assertions.assertInstanceOf(PenStampStmt.class, stmts.get(1));
        Assertions.assertInstanceOf(PenDownStmt.class, stmts.get(2));
        Assertions.assertInstanceOf(PenUpStmt.class, stmts.get(3));
        Assertions.assertInstanceOf(SetPenColorToColorStmt.class, stmts.get(4));
        Assertions.assertInstanceOf(ChangePenColorParamBy.class, stmts.get(5));
        Assertions.assertInstanceOf(ChangePenColorParamBy.class, stmts.get(6));
        Assertions.assertInstanceOf(SetPenColorParamTo.class, stmts.get(7));
        Assertions.assertInstanceOf(ChangePenSizeBy.class, stmts.get(8));
        Assertions.assertInstanceOf(SetPenSizeTo.class, stmts.get(9));
        Assertions.assertInstanceOf(FixedColorParam.class, ((SetPenColorParamTo) stmts.get(7)).getParam());
        Assertions.assertInstanceOf(ColorParamFromExpr.class, ((ChangePenColorParamBy) stmts.get(5)).getParam());
    }
}
