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

package de.uni_passau.fim.se2.litterbox.analytics.smells;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

class DuplicatedScriptTest {

    private static Program empty;
    private static Program duplicatedScript;
    private static Program duplicatedScriptMinimalDifference;
    private static Program duplicatedScriptDifferentEvent;
    private static Program duplicatedScriptMultipleBlocks;
    private static Program duplicatedScriptOtherSprite;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException, ParsingException {
        File f = new File("./src/test/fixtures/emptyProject.json");
        empty = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScript.json");
        duplicatedScript = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptMinimalDifference.json");
        duplicatedScriptMinimalDifference = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptDifferentEvent.json");
        duplicatedScriptDifferentEvent = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptMultipleBlocks.json");
        duplicatedScriptMultipleBlocks = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
        f = new File("./src/test/fixtures/smells/duplicatedScriptOtherSprite.json");
        duplicatedScriptOtherSprite = ProgramParser.parseProgram(f.getName(), mapper.readTree(f));
    }

    @Test
    public void testEmptyProgram() {
        DuplicatedScript parameterName = new DuplicatedScript();
        Set<Issue> reports = parameterName.check(empty);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testProgram() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScript);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedScriptMinimalDifference() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptMinimalDifference);
        // x-position and y-position sensing blocks are replaced
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicatedScriptDifferentEvent() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptDifferentEvent);
        Assertions.assertEquals(0, reports.size());
    }

    @Test
    public void testDuplicatedScriptMultipleBlocks() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptMultipleBlocks);
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void testDuplicatedScriptOtherSprite() {
        DuplicatedScript finder = new DuplicatedScript();
        Set<Issue> reports = finder.check(duplicatedScriptOtherSprite);
        Assertions.assertEquals(0, reports.size());
    }
}
