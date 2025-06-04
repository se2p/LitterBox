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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConditionalInsideLoopTest implements JsonTest {

    @Test
    public void testNestedConInLoop() throws IOException, ParsingException {
        assertThatFinderReports(1, new ConditionalInsideLoop(), "./src/test/fixtures/goodPractice/nestedConInLoop.json");
    }

    @Test
    public void testNestedConInLoopTwoDifferent() throws IOException, ParsingException {
        assertThatFinderReports(2, new ConditionalInsideLoop(), "./src/test/fixtures/goodPractice/nestedConInLoopTwo.json");
    }

    @Test
    public void testNestedConInLoopDuplicates() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/multipleNestedConditionInLoop.json");
        ConditionalInsideLoop conditionalInsideLoop = new ConditionalInsideLoop();
        List<Issue> reports = new ArrayList<>(conditionalInsideLoop.check(prog));
        Assertions.assertEquals(11, reports.size());
        for (int i = 0; i < reports.size(); i++) {
            for (int j = i + 1; j < reports.size(); j++) {
                Assertions.assertTrue(reports.get(i).isDuplicateOf(reports.get(j)));
            }
        }
    }
}
