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
import de.uni_passau.fim.se2.litterbox.analytics.LeilaAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;

public class LeilaVisitorTest {

    private static JsonNode project;

    @Test
    public void testSetRotationStyle() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/setRotationStyle.json";
        String output = getLeilaForProject(path);
        assertThat(output).contains("define rotationStyle as \"don't rotate\"");
        assertThat(output).contains("define rotationStyle as \"left-right\"");
        assertThat(output).contains("define rotationStyle as \"all around\"");
    }

    @Test
    public void testTouching() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/touching.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("touchingMousePointer()");
        assertThat(output).contains("touchingEdge()");
        assertThat(output).contains("touchingColor(rgb(88, 192, 228))");
        assertThat(output).contains("touchingObject(locate actor \"Apple\")");
    }

    @Test
    public void testMouseDown() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/mouseDown.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("mouseDown()");
    }

    @Test
    public void testJoin() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/join.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("join \"apple \" \"banana\"");
    }

    @Test
    public void testTurnRight() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/turnRight.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("turnRight(15)");
    }

    @Test
    public void testChangeVariableBy() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/changeVariableBy.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("define myvar as myvar + 1");
    }

    @Test
    public void testFromNumber() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/fromNumber.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("touchingColor((0 + 0))");
    }

    @Test
    public void testProcedureCombinedTextAndParams() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/ambiguousProcedureAndCombinedTextSignature.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("define myMethodWithParamsText (aParam: string, bParam: boolean) begin");
    }

    @Test
    public void testAmbiguousProcedureName() throws Exception {
        String path = "src/test/fixtures/leilaVisitor/ambiguousProcedureAndCombinedTextSignature.json";
        String output = getLeilaForProject(path);

        assertThat(output).contains("define myMethod_,y4+jC!5_KL#z]ByYJ^H () begin");
        assertThat(output).contains("define myMethod_LxW~gi,I]9)6;-1DnDd) () begin");
        assertThat(output).contains("define myMethod_LxW~gi,I]9)6;-1DnDd) () begin");
        assertThat(output).contains("myMethod_,y4+jC!5_KL#z]ByYJ^H()");
        assertThat(output).doesNotContain("myMethod_LxW~gi,I]9)6;-1DnDd)()");
    }

    private String getLeilaForProject(String path) throws IOException, ParsingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode project = objectMapper.readTree(file);
        PrintStream stream = new PrintStream(out);
        LeilaVisitor visitor = new LeilaVisitor(stream, false, true);
        Program program = ProgramParser.parseProgram("Small", project);
        visitor.visit(program);
        return out.toString();
    }

    @Test
    public void testCheckFailsForFolder(@TempDir File tempFile) {
        File file = new File("./src/test/fixtures/emptyProject.json");
        String path = file.getAbsolutePath();
        String outPath = tempFile.getAbsolutePath();
        LeilaAnalyzer analyzer = new LeilaAnalyzer(path, outPath + "foobar", false, true);
        analyzer.analyzeFile();
        File output = new File(Paths.get(outPath + "foobar", "emptyProject.sc").toString());
        assertThat(output.exists()).isFalse();
    }

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/leilaVisitor/grammarvisitorsmall.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }
}
