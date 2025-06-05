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
package de.uni_passau.fim.se2.litterbox.analytics.questions;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class IfBlockConditionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfBlockCondition(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfBlockCondition(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyIfThenStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/oneVariableInIfStatement.json");
    }

    @Test
    public void testScriptWithOnlyIfElseStmt() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/twoVariablesInIfElseStatement.json");
    }

    @Test
    public void testScriptWithTwoIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/twoIfThenStmtsInScript.json");
    }

    @Test
    public void testScriptWithNestedIfStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/nestedIfThenStmts.json");
    }

    @Test
    public void testAllControlBlocks() throws IOException, ParsingException {
        assertThatFinderReports(2, new IfBlockCondition(), "src/test/fixtures/questions/allControlBlocks.json");
    }

    @Test
    public void testNestedControlBlock() throws IOException, ParsingException {
        assertThatFinderReports(1, new IfBlockCondition(), "src/test/fixtures/questions/nestedControlBlock.json");
    }

    @Test
    public void testEmptyControlBody() throws IOException, ParsingException {
        assertThatFinderReports(0, new IfBlockCondition(), "src/test/fixtures/questions/emptyIfThenBody.json");
    }
}
