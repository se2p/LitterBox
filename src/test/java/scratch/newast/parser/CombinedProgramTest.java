/*
 * Copyright (C) 2019 LitterBox contributors
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
package scratch.newast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.visitor.DotVisitor;

/**
 * This class contains test cases for a program that contains most constructions from the AST. The fixture for these
 * tests contains at least one Expression of each type and various statements.
 */
public class CombinedProgramTest {

    private JsonNode project;

    @Before
    public void setup() {
        String path = "src/test/java/scratch/fixtures/combinedProgram.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void dummyParseAllBlocks() {
        String path = "src/test/java/scratch/fixtures/allBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
            Program program = ProgramParser.parseProgram("All", project);
        } catch (IOException | ParsingException e) {
            fail();
        }
    }

    @Test
    public void testVisitor() {
        DotVisitor visitor = new DotVisitor();
        String path = "src/test/java/scratch/fixtures/allBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
            Program program = ProgramParser.parseProgram("All", project);
            program.accept(visitor);
            visitor.printGraph();
            //visitor.saveGraph("./target/graph.dot");
        } catch (IOException | ParsingException e) {
            fail();
        }
    }
}