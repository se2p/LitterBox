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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static junit.framework.TestCase.fail;

public class GrammarPrintVisitorTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/printvisitor/grammarvisitorsmall.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Ignore // This is not really a test, it's a convenience method for showing what the visitor does or does not
   // @Test
    public void testVisitor() {
        PrintStream stream = new PrintStream(System.out);
        GrammarPrintVisitor visitor = new GrammarPrintVisitor(stream);
        try {
            Program program = ProgramParser.parseProgram("Small", project);
            visitor.visit(program);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Ignore // This is not really a test, it's a convenience method for showing what the visitor does or does not
   // @Test
    public void testVisitorBig() {
        String path = "src/test/fixtures/printvisitor/grammarvisitorbig.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = null;
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
        PrintStream stream = new PrintStream(System.out);
        GrammarPrintVisitor visitor = new GrammarPrintVisitor(stream);
        try {
            Program program = ProgramParser.parseProgram("Small", project);
            visitor.visit(program);
        } catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }
}
