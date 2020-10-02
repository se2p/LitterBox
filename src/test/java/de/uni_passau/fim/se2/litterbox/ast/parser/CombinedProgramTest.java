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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.DotVisitor;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.fail;

/**
 * This class contains test cases for a program that contains most constructions from the AST. The fixture for these
 * tests contains at least one Expression of each type and various statements.
 */
public class CombinedProgramTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/allBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    //@Test
    public void dummyParseAllBlocks() {
        try {
            Program program = ProgramParser.parseProgram("All", project);
        } catch (ParsingException e) {
            fail();
        }
    }

    //@Test
    public void testVisitor() {
        DotVisitor visitor = new DotVisitor();
        try {
            Program program = ProgramParser.parseProgram("All", project);
            program.accept(visitor);
            visitor.printGraph();
            //visitor.saveGraph("./target/graph.dot");
        } catch (ParsingException e) {
            fail();
        }
    }
}
