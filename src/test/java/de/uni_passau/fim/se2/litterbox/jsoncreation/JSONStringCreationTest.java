/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.jsoncreation;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.Main;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

class JSONStringCreationTest implements JsonTest {

    @Test
    void createProgramJSONStringWithVariableInComparison() throws ParsingException {
        final var program = getAST("src/test/fixtures/MWE.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        Assertions.assertTrue(json.contains("\"inputs\": {\"OPERAND1\": [3,[12,\"my variable\",\"`jEk@4|i[#Fk?(8x)AV.-my variable\"],[10,\"\"]],\"OPERAND2\": [1,[4,\"50\"]]}"));
    }

    @Test
    void createProgramJSONStringWithLooseVariable() throws ParsingException {
        final var program = getAST("src/test/fixtures/looseVariable.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        Assertions.assertTrue(json.contains("[12,\"meine Variable\",\"`jEk@4|i[#Fk?(8x)AV.-my variable\",676.0,120.0]"));
    }

    @Test
    void createProgramJSONWithKeycode(@TempDir final Path outputDir) throws IOException {
        new CommandLine(new Main()).execute(
                "check", "-l", "de", "-p", "src/test/fixtures/jsonCreation/keyCodes.json", "-a", outputDir.toString()
        );

        final String json = Files.readString(outputDir.resolve("keyCodes_annotated.json"));

        // actual block should contain the English name
        assertThat(json).contains("\"right arrow\"");
        assertThat(json).contains("\"space\"");
        assertThat(json).doesNotContain("\"Pfeil nach rechts\"");
        assertThat(json).doesNotContain("\"Leertaste\"");

        // the hint description should be in German
        assertThat(json).contains("falls <Taste (Pfeil nach rechts ) gedrückt?>");
    }

    @Test
    void testCrashInNPEFixture() throws ParsingException {
        final var program = getAST("src/test/fixtures/refactoring/NPE.json");
        // This should not throw NPE
        String json = JSONStringCreator.createProgramJSONString(program);

        // Check if the json results in the same program
        Scratch3Parser parser = new Scratch3Parser();
        Program parsedProgram = parser.parseString("NPE", json);

        Assertions.assertTrue(StructuralEquality.areStructurallyEqual(program, parsedProgram));
    }

    @Test
    void testEscapeQuotesInString() throws ParsingException {
        final var program = getAST("src/test/fixtures/escape-special-chars.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        assertThat(json).contains("\\\"the quotes should have been escaped\\\"");
    }

    @Test
    void testEscapeQuotesInBroadcastMessage() throws ParsingException {
        final var program = getAST("src/test/fixtures/escape-special-chars-broadcast-message.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        assertThat(json).contains("the \\\"quotes\\\" should have been escaped");
        assertThat(json).doesNotContain("the \"quotes\" should have been escaped");
    }

    @Test
    void testQuantumBlocks() throws ParsingException {
        final var program = getAST("src/test/fixtures/quantum.json");
        final String json = JSONStringCreator.createProgramJSONString(program);

        final Scratch3Parser parser = new Scratch3Parser();
        final Program parsedProgram = parser.parseString("quantum", json);

        Assertions.assertTrue(StructuralEquality.areStructurallyEqual(program, parsedProgram));
    }
}
