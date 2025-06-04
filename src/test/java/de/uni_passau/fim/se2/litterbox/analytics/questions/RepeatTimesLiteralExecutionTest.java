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

public class RepeatTimesLiteralExecutionTest implements JsonTest {
    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testNoLoops() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/noVariables.json");
    }

    @Test
    public void testScriptWithOnlyForeverLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithOnlyForeverLoop.json");
    }

    @Test
    public void testScriptWithOnlyTimesLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithOnlyTimesLoop.json");
    }

    @Test
    public void testRepeatTimesStmtsWithDifferentOvals() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesStmtsWithDifferentOvals.json");
    }

    @Test
    public void testScriptWithOnlyUntilLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithOnlyUntilLoop.json");
    }

    @Test
    public void testRepeatTimesWithStop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStop.json");
    }

    @Test
    public void testRepeatTimesWithStopInIfElse() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStopInIfThen.json");
    }

    @Test
    public void testRepeatTimesWithStopInNestedRepeatTimesLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStopInNestedRepeatTimesLoop.json");
    }

    @Test
    public void testRepeatTimesWithStopInNestedForeverLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesWithStopInNestedForeverLoop.json");
    }

    @Test
    public void testScriptWithTwoLoopsAndOtherElements() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/scriptWithTwoLoopsAndOtherElements.json");
    }

    @Test
    public void testNestedRepeatTimesStmts() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/nestedRepeatTimesStmts.json");
    }

    @Test
    public void testRepeatTimesStmtsNestedInForeverLoop() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesStmtsNestedInForeverLoop.json");
    }

    @Test
    public void testTripleNestedRepeatTimesStmts() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/tripleNestedRepeatTimesStmts.json");
    }

    @Test
    public void testRepeatTimesStmtInLoopStmt() throws IOException, ParsingException {
        assertThatFinderReports(0, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/repeatTimesInLoop.json");
    }

    @Test
    public void testTwoScriptsWithConditionalLoops() throws IOException, ParsingException {
        assertThatFinderReports(1, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/twoScriptsWithConditionalLoops.json");
    }

    @Test
    public void testTwoRepeatTimesStmts() throws IOException, ParsingException {
        assertThatFinderReports(2, new RepeatTimesLiteralExecution(), "src/test/fixtures/questions/twoRepeatTimesStmts.json");
    }
}
