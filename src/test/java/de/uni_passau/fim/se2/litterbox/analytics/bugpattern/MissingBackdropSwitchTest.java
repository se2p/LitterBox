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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MissingBackdropSwitchTest {
    private static Program empty;
    private static Program missingBackdropSwitchNext;
    private static Program missingBack;
    private static Program random;
    private static Program fischmampfer;
    private static Program fischmampferWithWait;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {

        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingBackDropSwitchNext.json");
        missingBackdropSwitchNext = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missBackdrop.json");
        missingBack = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/randomBackdrop.json");
        random = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingBackdropSwitchAsString.json");
        fischmampfer = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/bugpattern/missingBackdropSwitchAndWaitAsString.json");
        fischmampferWithWait = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissingBackdropSwitchNext() {
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(missingBackdropSwitchNext);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testMissBackdrop() {
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(missingBack);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testRandomBack() {
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(random);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testFischmampfer() {
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(fischmampfer);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testSwitchAndWait() {
        MissingBackdropSwitch parameterName = new MissingBackdropSwitch();
        Set<Issue> reports = parameterName.check(fischmampferWithWait);
        Assertions.assertEquals(0, reports.size());
    }
}
