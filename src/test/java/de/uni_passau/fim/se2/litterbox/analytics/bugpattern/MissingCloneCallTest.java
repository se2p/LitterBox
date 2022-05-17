/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MissingCloneCallTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingCloneCall(), "./src/test/fixtures/emptyProject.json");
    }

    @Test
    public void testClone() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingCloneCall(), "./src/test/fixtures/bugpattern/emptyCloneCall.json");
    }

    @Test
    public void testMissingCloneCall() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingCloneCall(), "./src/test/fixtures/bugpattern/missingCloneCall.json");
    }

    @Test
    public void testCloneInOtherSprite() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingCloneCall(), "./src/test/fixtures/bugpattern/cloneInOtherSprite.json");
    }

    @Test
    public void testRainbowSix() throws IOException, ParsingException {
        assertThatFinderReports(0, new MissingCloneCall(), "./src/test/fixtures/bugpattern/rainbowSix.json");
    }

    @Test
    public void testJumper() throws IOException, ParsingException {
        assertThatFinderReports(1, new MissingCloneCall(), "./src/test/fixtures/bugpattern/jumper.json");
    }

    @Test
    public void testCloneCallDuplication() throws IOException, ParsingException {
        Program prog = getAST("src/test/fixtures/bugpattern/missingCloneCallDouble.json");
        MissingCloneCall finder = new MissingCloneCall();
        List<Issue> reports = new ArrayList<>(finder.check(prog));
        Assertions.assertEquals(3, reports.size());
        Assertions.assertTrue(reports.get(0).isDuplicateOf(reports.get(1)));
        Assertions.assertFalse(reports.get(0).isDuplicateOf(reports.get(2)));
    }
}
