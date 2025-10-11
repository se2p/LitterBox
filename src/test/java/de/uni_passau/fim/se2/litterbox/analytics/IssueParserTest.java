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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class IssueParserTest implements JsonTest {

    @Test
    public void testReadingBlockId() throws ParsingException, IOException {
        File file = new File("src/test/fixtures/jsonReport/testfruit.json");
        IssueParser issueParser = new IssueParser();
        Map<String, List<IssueDTO>> foundIssues = issueParser.getIssuesPerFinder(file);
        Assertions.assertEquals(3, foundIssues.size());
        Assertions.assertTrue(foundIssues.containsKey(MissingInitialization.NAME));
        Assertions.assertEquals(1, foundIssues.get(MissingInitialization.NAME).size());
        Assertions.assertEquals("Jif;Ug!4[{,1su7{fvp}", foundIssues.get(MissingInitialization.NAME).getFirst().issueLocationBlockId());
    }
}
