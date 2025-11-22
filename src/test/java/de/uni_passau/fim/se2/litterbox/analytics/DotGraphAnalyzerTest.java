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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DotGraphAnalyzerTest {

    private Program program;

    @BeforeEach
    void setUp() throws IOException, ParsingException {
        Scratch3Parser parser = new Scratch3Parser();
        program = parser.parseFile(Path.of("src/test/fixtures/MWE.json").toFile());
    }

    @ParameterizedTest
    @EnumSource(GraphType.class)
    void testAnalyzeGraph(GraphType graphType) {
        DotGraphAnalyzer analyzer = new DotGraphAnalyzer(graphType);
        String result = analyzer.analyze(program);
        assertNotNull(result);
        assertTrue(result.contains("digraph"));
    }

    @Test
    void testDirectoryOutput(@TempDir Path tempDir) throws IOException {
        DotAnalyzer analyzer = new DotAnalyzer(tempDir, false, GraphType.CFG);
        Path projectFile = Path.of("test_project.json");
        String dotString = "digraph G {}";
        analyzer.writeResultToFile(projectFile, program, dotString);

        Path expectedFile = tempDir.resolve("test_project_CFG.dot");
        assertTrue(java.nio.file.Files.exists(expectedFile));
    }
}
