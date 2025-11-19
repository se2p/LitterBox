/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

public class JSONStringCreationTest implements JsonTest {

    @Test
    void createProgramJSONStringWithVariableInComparison() throws ParsingException, IOException {
        final var program = getAST("src/test/fixtures/MWE.json");
        final String json = JSONStringCreator.createProgramJSONString(program);
        Assertions.assertTrue(json.contains("\"inputs\": {\"OPERAND1\": [3,[12,\"my variable\",\"`jEk@4|i[#Fk?(8x)AV.-my variable\"],[10,\"\"]],\"OPERAND2\": [1,[4,\"50\"]]}"));
    }

    @Test
    void createProgramJSONStringWithLooseVariable() throws ParsingException, IOException {
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
}
