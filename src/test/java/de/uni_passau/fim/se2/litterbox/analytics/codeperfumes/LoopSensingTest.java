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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoopSensingTest implements JsonTest {

    @Test
    public void testSensingInLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new LoopSensing(), "./src/test/fixtures/solutionpattern/eventInLoop.json");
    }

    @Test
    public void testTwoEventsInLoop() throws IOException, ParsingException {
        assertThatFinderReports(2, new LoopSensing(), "./src/test/fixtures/solutionpattern/eventInLoopTwo.json");
    }

    @Test
    public void testMissingSensingForEvent() throws IOException, ParsingException {
        assertThatFinderReports(0, new LoopSensing(), "./src/test/fixtures/bugpattern/missingLoopSensingMultiple.json");
    }

    @Test
    public void testMultiBlock() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/solutionpattern/loopSensingMultiBlock.json");
        LoopSensing LoopSensing = new LoopSensing();
        List<Issue> reports = new ArrayList<>(LoopSensing.check(prog));
        Assertions.assertEquals(1, reports.size());
        MultiBlockIssue issue = (MultiBlockIssue) reports.get(0);
        List<ASTNode> nodes = issue.getNodes();
        Assertions.assertInstanceOf(UntilStmt.class, nodes.get(0));
        Assertions.assertInstanceOf(IfThenStmt.class, nodes.get(1));
        Assertions.assertInstanceOf(SpriteTouchingColor.class, nodes.get(2));
        Assertions.assertInstanceOf(BiggerThan.class, ((UntilStmt) nodes.get(0)).getBoolExpr());
    }
}
