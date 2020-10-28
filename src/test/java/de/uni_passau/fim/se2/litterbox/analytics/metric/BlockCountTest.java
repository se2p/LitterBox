/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BlockCountTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(0, parameterName.calculateMetric(empty));
    }

    @Test
    public void testBlockCountNested() throws IOException, ParsingException {
        Program nestedLoops = JsonTest.parseProgram("./src/test/fixtures/smells/nestedLoops.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(14, parameterName.calculateMetric(nestedLoops));
    }

    @Test
    public void testBlockproc() throws IOException, ParsingException {
        Program withProc = JsonTest.parseProgram("./src/test/fixtures/blockCountWithProc.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(18, parameterName.calculateMetric(withProc));
    }

    @Test
    public void testFixedStatements() throws IOException, ParsingException {
        Program fixedStatements = JsonTest.parseProgram("./src/test/fixtures/stmtParser/allFixedStatements.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(26, parameterName.calculateMetric(fixedStatements));
    }

    @Test
    public void testFixedExpr() throws IOException, ParsingException {
        Program fixedExpressions = JsonTest.parseProgram("./src/test/fixtures/fixedExpressions.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(4, parameterName.calculateMetric(fixedExpressions));
    }

    @Test
    public void testOnlyVariable() throws IOException, ParsingException {
        Program onlyVariable = JsonTest.parseProgram("./src/test/fixtures/onlyVariable.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(1, parameterName.calculateMetric(onlyVariable));
    }

    @Test
    public void testHalfFixedExpr() throws IOException, ParsingException {
        Program halfFixedExpr = JsonTest.parseProgram("./src/test/fixtures/halfFixedExpressions.json");
        BlockCount parameterName = new BlockCount();
        Assertions.assertEquals(5, parameterName.calculateMetric(halfFixedExpr)); //TODO does an empty string have to be an UnspecifiedExpr?
    }
}
