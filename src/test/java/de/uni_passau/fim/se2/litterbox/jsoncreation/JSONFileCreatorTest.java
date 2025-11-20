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
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JSONFileCreatorTest implements JsonTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteJsonFromProgramWithDirectory() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        JSONFileCreator.writeJsonFromProgram(program, tempDir, "_test");
        
        Path expectedFile = tempDir.resolve("emptyProject_test.json");
        assertTrue(Files.exists(expectedFile), "File should be created in the directory with default name");
    }

    @Test
    void testWriteJsonFromProgramWithFilePath() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Path specificFile = tempDir.resolve("SpecificName.json");
        
        JSONFileCreator.writeJsonFromProgram(program, specificFile, "_ignored");
        
        assertTrue(Files.exists(specificFile), "File should be created at the specific path");
        // Ensure the default file was NOT created
        Path defaultFile = tempDir.resolve("emptyProject_ignored.json");
        assertFalse(Files.exists(defaultFile), "Default file should not be created when specific path is given");
    }

    @Test
    void testWriteJsonFromProgramWithNonExistentDirectory() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Path nonExistentDir = tempDir.resolve("nonExistentDir");
        
        assertThrows(FileNotFoundException.class, () -> {
            JSONFileCreator.writeJsonFromProgram(program, nonExistentDir, "_test");
        });
    }

    @Test
    void testWriteSb3FromProgramWithDirectory() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        File sourceSb3 = new File("src/test/fixtures/emptyProject.sb3");
        
        JSONFileCreator.writeSb3FromProgram(program, tempDir, sourceSb3, "_test");
        
        Path expectedFile = tempDir.resolve("EmptyProject_test.sb3");
        assertTrue(Files.exists(expectedFile), "SB3 file should be created in the directory with default name");
    }

    @Test
    void testWriteSb3FromProgramWithFilePath() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        File sourceSb3 = new File("src/test/fixtures/emptyProject.sb3");
        Path specificFile = tempDir.resolve("SpecificName.sb3");
        
        JSONFileCreator.writeSb3FromProgram(program, specificFile, sourceSb3, "_ignored");
        
        assertTrue(Files.exists(specificFile), "SB3 file should be created at the specific path");
        // Ensure the default file was NOT created
        Path defaultFile = tempDir.resolve("EmptyProject_ignored.sb3");
        assertFalse(Files.exists(defaultFile), "Default SB3 file should not be created when specific path is given");
    }

    @Test
    void testWriteSb3FromProgramWithNonExistentDirectory() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        File sourceSb3 = new File("src/test/fixtures/emptyProject.sb3");
        Path nonExistentDir = tempDir.resolve("nonExistentDir");
        
        assertThrows(FileNotFoundException.class, () -> {
            JSONFileCreator.writeSb3FromProgram(program, nonExistentDir, sourceSb3, "_test");
        });
    }

    @Test
    void testWriteJsonFromProgramWithExistingFile() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        Path existingFile = tempDir.resolve("Existing.json");
        Files.createFile(existingFile);
        
        assertThrows(IOException.class, () -> {
            JSONFileCreator.writeJsonFromProgram(program, existingFile, "_ignored");
        });
    }

    @Test
    void testWriteSb3FromProgramWithExistingFile() throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        File sourceSb3 = new File("src/test/fixtures/emptyProject.sb3");
        Path existingFile = tempDir.resolve("Existing.sb3");
        Files.createFile(existingFile);
        
        assertThrows(IOException.class, () -> {
            JSONFileCreator.writeSb3FromProgram(program, existingFile, sourceSb3, "_ignored");
        });
    }
}
