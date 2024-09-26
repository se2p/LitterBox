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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Scratch3ParserTest {

    private final Scratch3Parser parser = new Scratch3Parser();

    @ParameterizedTest
    @ValueSource(strings = {"emptyProject.sb3", "greenflag.json"})
    void parseSb3ExtractProgramNameAsFilename(final String filename) throws ParsingException, IOException {
        final Path projectFile = Path.of("./src/test/fixtures/").resolve(filename);
        final Program program = parser.parseFile(projectFile.toFile());

        final String expectedName = filename.replace(".json", "").replace(".sb3", "");
        assertEquals(expectedName, program.getIdent().getName());
    }

    @Test
    void parseSb3ProgramNoFileExtension(@TempDir final Path tempDir) throws ParsingException, IOException {
        final Path projectFile = Path.of("./src/test/fixtures/emptyProject.sb3");
        final Path tmpProjectFile = tempDir.resolve("filename_no_extension");
        Files.copy(projectFile, tmpProjectFile);

        final Program program = parser.parseFile(tmpProjectFile.toFile());

        assertEquals("filename_no_extension", program.getIdent().getName());
    }
}
