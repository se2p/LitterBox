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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClonedCodeType1Test {

    @Test
    public void testDuplicatedScript() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/duplicatedScript.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        assertEquals(2, issues.size());
    }

    @Test
    public void testDuplicatedScriptDifferentLiteralsAndVariables() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codecloneliteralsvariables.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        // 0, as clone is of type 2
        assertEquals(0, issues.size());
    }

    @Test
    public void testSubsequenceClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonesubsequence.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        // 0, as clone is of type 2
        assertEquals(0, issues.size());
    }

    @Test
    public void testVariableClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonevariableblocks.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        // 0, as clone is of type 2
        assertEquals(0, issues.size());
    }

    @Test
    public void testCompareScriptWithItself() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/cloneScriptWithItself.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        assertEquals(2, issues.size());
    }

    @Test
    public void testListClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonelistblocks.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        // 0, as clone is of type 2
        assertEquals(0, issues.size());
    }

    @Test
    public void testCustomBlockClone() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/codeclonecustomblock.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        Set<Issue> issues = finder.check(program);
        // 0, as clone is shorter than the minSize of 6
        assertEquals(0, issues.size());
    }

    private Program getAST(String fileName) throws IOException, ParsingException {
        File file = new File(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        Program program = ProgramParser.parseProgram("TestProgram", project);
        return program;
    }

    @Test
    public void testDuplicatedScriptSixInclEvent() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/exactCopySixInclEvent.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        //0, event is not counted
        assertEquals(0, issues.size());
    }

    @Test
    public void testDuplicatedScriptSevenInclEvent() throws IOException, ParsingException {
        Program program = getAST("./src/test/fixtures/smells/exactCopySevenInclEvent.json");
        ClonedCodeType1 finder = new ClonedCodeType1();
        List<Issue> issues = new ArrayList<>(finder.check(program));
        assertEquals(2, issues.size());
        assertTrue(issues.get(0).isDuplicateOf(issues.get(1)));
    }
}
