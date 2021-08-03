/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import java.util.Set;

public class BackdropSwitchTest implements JsonTest {


    @Test
    public void testSimpleBackdropSwitchAndEvent() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropEvent.json");
        BackdropSwitch backdropSwitch = new BackdropSwitch();
        Set<Issue> reports = backdropSwitch.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testWithRandomBackdrop() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropRandom.json");
        BackdropSwitch backdropSwitch = new BackdropSwitch();
        Set<Issue> reports = backdropSwitch.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testBackdropEventWithoutEventAndOneCorrect() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropEventWithoutSwitch.json");
        BackdropSwitch backdropSwitch = new BackdropSwitch();
        Set<Issue> reports = backdropSwitch.check(prog);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testBackdropEventNoSwitch() throws IOException, ParsingException {
        Program prog = JsonTest.parseProgram("./src/test/fixtures/goodPractice/backdropEventNoSwitch.json");
        BackdropSwitch backdropSwitch = new BackdropSwitch();
        Set<Issue> reports = backdropSwitch.check(prog);
        Assertions.assertEquals(0, reports.size());
    }
}


