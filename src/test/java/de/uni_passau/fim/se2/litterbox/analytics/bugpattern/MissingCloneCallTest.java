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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissingCloneCallTest implements JsonTest {

    @Test
    public void testEmptyProgram() throws IOException, ParsingException {
        Program empty = JsonTest.parseProgram("./src/test/fixtures/emptyProject.json");
        MissingCloneCall parameterName = new MissingCloneCall();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testClone() throws IOException, ParsingException {
        Program emptyCloneCall = JsonTest.parseProgram("./src/test/fixtures/bugpattern/emptyCloneCall.json");
        MissingCloneCall parameterName = new MissingCloneCall();
        Set<Issue> reports = parameterName.check(emptyCloneCall);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingCloneCall() throws IOException, ParsingException {
        Program missingCloneCall = JsonTest.parseProgram("./src/test/fixtures/bugpattern/missingCloneCall.json");
        MissingCloneCall parameterName = new MissingCloneCall();
        Set<Issue> reports = parameterName.check(missingCloneCall);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testCloneInOtherSprite() throws IOException, ParsingException {
        Program cloneInOtherSprite = JsonTest.parseProgram("./src/test/fixtures/bugpattern/cloneInOtherSprite.json");
        MissingCloneCall parameterName = new MissingCloneCall();
        Set<Issue> reports = parameterName.check(cloneInOtherSprite);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testRainbowSix() throws IOException, ParsingException {
        Program rainbowSix = JsonTest.parseProgram("./src/test/fixtures/bugpattern/rainbowSix.json");
        MissingCloneCall parameterName = new MissingCloneCall();
        Set<Issue> reports = parameterName.check(rainbowSix);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testJumper() throws IOException, ParsingException {
        Program jumper = JsonTest.parseProgram("./src/test/fixtures/bugpattern/jumper.json");
        MissingCloneCall parameterName = new MissingCloneCall();
        Set<Issue> reports = parameterName.check(jumper);
        Assertions.assertEquals(1, reports.size());
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
