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
package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link ExportAnalyzer} export functionality.
 */
public class ExportAnalyzerTest implements JsonTest {

    private static final String JSON_FIXTURE = "src/test/fixtures/scratchblocks/motionblocks.json";
    private static final String SB3_FIXTURE = "src/test/fixtures/emptyProject.sb3";

    // ---------------------------------------------------------------
    // Goboscript single file export (no --full-project)
    // ---------------------------------------------------------------

    @Test
    public void testGoboscriptExportToFile(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputFile = tempDir.resolve("output.gs");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.GOBOSCRIPT, outputFile, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        assertThat(Files.exists(outputFile)).isTrue();
        String content = Files.readString(outputFile);
        assertThat(content).contains("sprite");
        assertThat(content).contains("onflag");
    }

    @Test
    public void testGoboscriptExportToDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        // When output is a directory, should create <project>.gs in that directory
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.GOBOSCRIPT, tempDir, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        Path expectedFile = tempDir.resolve("motionblocks.gs");
        assertThat(Files.exists(expectedFile)).isTrue();
        String content = Files.readString(expectedFile);
        assertThat(content).contains("sprite");
    }

    @Test
    public void testGoboscriptExportFromSb3(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputFile = tempDir.resolve("output.gs");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.GOBOSCRIPT, outputFile, false, false, false);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        assertThat(Files.exists(outputFile)).isTrue();
        String content = Files.readString(outputFile);
        // Should contain both Stage and Sprite1 in single file
        assertThat(content).contains("stage Stage");
        assertThat(content).contains("sprite Sprite1");
    }

    // ---------------------------------------------------------------
    // Goboscript full project export (--full-project)
    // ---------------------------------------------------------------

    @Test
    public void testGoboscriptFullProjectExport(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputDir = tempDir.resolve("project");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.GOBOSCRIPT, outputDir, false, false, true);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        // Should create project directory with separate files
        assertThat(Files.exists(outputDir)).isTrue();
        assertThat(Files.isDirectory(outputDir)).isTrue();

        // Should have separate .gs files for each actor
        assertThat(Files.exists(outputDir.resolve("Stage.gs"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("Sprite1.gs"))).isTrue();

        // Should have extracted assets in assets/ subdirectory
        Path assetsDir = outputDir.resolve("assets");
        assertThat(Files.exists(assetsDir)).isTrue();
        assertThat(Files.isDirectory(assetsDir)).isTrue();
        assertThat(Files.exists(assetsDir.resolve("cd21514d0531fdffb22204e0ec5ed84a.svg"))).isTrue();
    }

    @Test
    public void testGoboscriptFullProjectExportToExistingDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        // When output is an existing directory, should create subdirectory with project name
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.GOBOSCRIPT, tempDir, false, false, true);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        // Should create subdirectory named after project
        Path projectDir = tempDir.resolve("emptyProject");
        assertThat(Files.exists(projectDir)).isTrue();
        assertThat(Files.exists(projectDir.resolve("Stage.gs"))).isTrue();
    }

    @Test
    public void testGoboscriptFullProjectRejectsJson(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputDir = tempDir.resolve("project");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.GOBOSCRIPT, outputDir, false, false, true);

        // This should not throw, but should log an error and not create output
        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        // Output directory should not be created (or be empty) since JSON is rejected
        if (Files.exists(outputDir)) {
            List<Path> files = Files.list(outputDir).collect(Collectors.toList());
            assertThat(files).isEmpty();
        }
    }

    // ---------------------------------------------------------------
    // ScratchBlocks export (existing functionality)
    // ---------------------------------------------------------------

    @Test
    public void testScratchBlocksExportToFile(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputFile = tempDir.resolve("output.txt");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.SCRATCHBLOCKS, outputFile, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        assertThat(Files.exists(outputFile)).isTrue();
        String content = Files.readString(outputFile);
        assertThat(content).contains("when green flag clicked");
    }

    @Test
    public void testScratchBlocksExportToDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.SCRATCHBLOCKS, tempDir, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        Path expectedFile = tempDir.resolve("motionblocks.txt");
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    // ---------------------------------------------------------------
    // PyStage export
    // ---------------------------------------------------------------

    @Test
    public void testPyStageExportToFile(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputFile = tempDir.resolve("output.py");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, outputFile, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        assertThat(Files.exists(outputFile)).isTrue();
        String content = Files.readString(outputFile);
        assertThat(content).contains("from pystage.en import Stage");
        assertThat(content).contains("stage = Stage()");
        assertThat(content).contains("stage.play()");
    }

    @Test
    public void testPyStageExportToDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, tempDir, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        Path expectedFile = tempDir.resolve("motionblocks.py");
        assertThat(Files.exists(expectedFile)).isTrue();
        String content = Files.readString(expectedFile);
        assertThat(content).contains("from pystage.en import Stage");
    }

    @Test
    public void testPyStageExportFromSb3(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputFile = tempDir.resolve("output.py");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, outputFile, false, false, false);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        assertThat(Files.exists(outputFile)).isTrue();
        String content = Files.readString(outputFile);
        // Should contain stage and sprite setup
        assertThat(content).contains("stage = Stage()");
        assertThat(content).contains("= stage.add_a_sprite()");
    }

    @Test
    public void testPyStageExportContainsPythonSyntax(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputFile = tempDir.resolve("output.py");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, outputFile, false, false, false);

        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        String content = Files.readString(outputFile);
        // Should use Python syntax, not Scratch/Goboscript syntax
        assertThat(content).contains("def ");
        assertThat(content).contains("(self):");
        assertThat(content).doesNotContain("{");
        assertThat(content).doesNotContain("}");
        assertThat(content).doesNotContain(";");
    }

    // ---------------------------------------------------------------
    // PyStage full project export (--full-project)
    // ---------------------------------------------------------------

    @Test
    public void testPyStageFullProjectExport(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputDir = tempDir.resolve("project");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, outputDir, false, false, true);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        // Should create project directory
        assertThat(Files.exists(outputDir)).isTrue();
        assertThat(Files.isDirectory(outputDir)).isTrue();

        // Should have single .py file with all code
        Path pyFile = outputDir.resolve("emptyProject.py");
        assertThat(Files.exists(pyFile)).isTrue();
        String content = Files.readString(pyFile);
        assertThat(content).contains("from pystage.en import Stage");
        assertThat(content).contains("stage.play()");

        // Should have extracted assets in images/ and sounds/ subdirectories
        Path imagesDir = outputDir.resolve("images");
        assertThat(Files.exists(imagesDir)).isTrue();
        assertThat(Files.isDirectory(imagesDir)).isTrue();
        // Assets are named with their logical names + extension
        assertThat(Files.exists(imagesDir.resolve("costume1.svg"))).isTrue();
    }

    @Test
    public void testPyStageFullProjectExportToExistingDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        // When output is an existing directory, should create subdirectory with project name
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, tempDir, false, false, true);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        // Should create subdirectory named after project
        Path projectDir = tempDir.resolve("emptyProject");
        assertThat(Files.exists(projectDir)).isTrue();
        assertThat(Files.exists(projectDir.resolve("emptyProject.py"))).isTrue();
    }

    @Test
    public void testPyStageFullProjectRejectsJson(@TempDir Path tempDir) throws IOException, ParsingException {
        Path outputDir = tempDir.resolve("project");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, outputDir, false, false, true);

        // This should not throw, but should log an error and not create output
        analyzer.analyzeFile(Path.of(JSON_FIXTURE));

        // Output directory should not be created (or be empty) since JSON is rejected
        if (Files.exists(outputDir)) {
            List<Path> files = Files.list(outputDir).collect(Collectors.toList());
            assertThat(files).isEmpty();
        }
    }

    @Test
    public void testPyStageFullProjectSinglePythonFile(@TempDir Path tempDir) throws IOException, ParsingException {
        // Unlike Goboscript full project which splits into multiple .gs files,
        // PyStage full project should produce a single .py file
        Path outputDir = tempDir.resolve("project");
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.PYSTAGE, outputDir, false, false, true);

        analyzer.analyzeFile(Path.of(SB3_FIXTURE));

        // Count .py files - should be exactly 1
        long pyFileCount = Files.list(outputDir)
                .filter(p -> p.toString().endsWith(".py"))
                .count();
        assertThat(pyFileCount).isEqualTo(1);
    }
}
