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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InitialisationOfPositionTest implements JsonTest {

    @Test
    public void testInitLocXandY() throws IOException, ParsingException {
        assertThatFinderReports(1, new InitialisationOfPosition(), "./src/test/fixtures/goodPractice/initLocSetxSetY.json");
    }

    @Test
    public void testInitLocInCustomBlock() throws IOException, ParsingException {
        assertThatFinderReports(1, new InitialisationOfPosition(), "./src/test/fixtures/goodPractice/initLocInBlock.json");
    }

    @Test
    public void testInitInBoth() throws IOException, ParsingException {
        assertThatFinderReports(2, new InitialisationOfPosition(), "./src/test/fixtures/goodPractice/initLocInBoth.json");
    }

    @Test
    public void testInitPositionLocationInScript() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/localisePositionScript.json");
        InitialisationOfPosition pos = new InitialisationOfPosition();
        List<Issue> issues = new ArrayList<>(pos.check(prog));
        Assertions.assertEquals(2, issues.size());
        Issue gigaIssue = issues.get(0);
        Issue nanoIssue = issues.get(1);
        Assertions.assertEquals("Giga", gigaIssue.getActorName());
        Assertions.assertEquals("Nano", nanoIssue.getActorName());
        ProcedureDefinition gigaProcedure = gigaIssue.getProcedure();
        ProcedureDefinition nanoProcedure = nanoIssue.getProcedure();
        Assertions.assertNotNull(gigaProcedure);
        Assertions.assertNotNull(nanoProcedure);
        Assertions.assertInstanceOf(GoToPosXY.class, gigaProcedure.getStmtList().getStmts().get(0));
        Assertions.assertInstanceOf(GoToPosXY.class, nanoProcedure.getStmtList().getStmts().get(0));
    }
}
