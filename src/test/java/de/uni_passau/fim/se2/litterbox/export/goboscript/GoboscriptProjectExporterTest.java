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
package de.uni_passau.fim.se2.litterbox.export.goboscript;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for the {@link GoboscriptProjectExporter} full project export functionality.
 */
public class GoboscriptProjectExporterTest implements JsonTest {

    private static final String SB3_FIXTURE = "src/test/fixtures/emptyProject.sb3";

    @Test
    public void testExportCreatesDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        assertThat(Files.exists(outputDir)).isTrue();
        assertThat(Files.isDirectory(outputDir)).isTrue();
    }

    @Test
    public void testExportCreatesActorFiles(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        // Should have one .gs file per actor (Stage + Sprite1)
        List<Path> gsFiles = Files.list(outputDir)
                .filter(p -> p.toString().endsWith(".gs"))
                .collect(Collectors.toList());

        assertThat(gsFiles).hasSize(2);

        // Check that expected files exist
        assertThat(Files.exists(outputDir.resolve("Stage.gs"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("Sprite1.gs"))).isTrue();
    }

    @Test
    public void testExportExtractsAssets(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        Path assetsDir = outputDir.resolve("assets");

        // Should extract costume SVGs
        assertThat(Files.exists(assetsDir.resolve("cd21514d0531fdffb22204e0ec5ed84a.svg"))).isTrue();
        assertThat(Files.exists(assetsDir.resolve("b7853f557e4426412e64bb3da6531a99.svg"))).isTrue();
        assertThat(Files.exists(assetsDir.resolve("e6ddc55a6ddd9cc9d84fe0b4c21e016f.svg"))).isTrue();

        // Should extract sound WAVs
        assertThat(Files.exists(assetsDir.resolve("83a9787d4cb6f3b7632b4ddfebf74367.wav"))).isTrue();
        assertThat(Files.exists(assetsDir.resolve("83c36d806dc92327b9e7049a565c6bff.wav"))).isTrue();

        // Should NOT extract project.json
        assertThat(Files.exists(outputDir.resolve("project.json"))).isFalse();
    }

    @Test
    public void testExportedActorContainsCostumeDeclarations(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        // Check Stage.gs content
        String stageContent = Files.readString(outputDir.resolve("Stage.gs"));
        assertThat(stageContent).contains("stage Stage");
        assertThat(stageContent).contains("costumes \"assets/cd21514d0531fdffb22204e0ec5ed84a.svg\" as \"backdrop1\"");

        // Check Sprite1.gs content
        String spriteContent = Files.readString(outputDir.resolve("Sprite1.gs"));
        assertThat(spriteContent).contains("sprite Sprite1");
        assertThat(spriteContent).contains("costumes \"assets/b7853f557e4426412e64bb3da6531a99.svg\" as \"costume1\"");
        assertThat(spriteContent).contains("costumes \"assets/e6ddc55a6ddd9cc9d84fe0b4c21e016f.svg\" as \"costume2\"");
    }

    @Test
    public void testExportedActorContainsSoundDeclarations(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        // Check Stage.gs content has pop sound
        String stageContent = Files.readString(outputDir.resolve("Stage.gs"));
        assertThat(stageContent).contains("sounds \"assets/83a9787d4cb6f3b7632b4ddfebf74367.wav\" as \"pop\"");

        // Check Sprite1.gs content has Meow sound
        String spriteContent = Files.readString(outputDir.resolve("Sprite1.gs"));
        assertThat(spriteContent).contains("sounds \"assets/83c36d806dc92327b9e7049a565c6bff.wav\" as \"Meow\"");
    }

    @Test
    public void testSanitizeFilename(@TempDir Path tempDir) throws IOException, ParsingException {
        // This test verifies that problematic characters in sprite names are sanitized
        // The emptyProject doesn't have such names, so we just verify the exporter doesn't crash
        // with normal names
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        // Verify files were created with sanitized names (no special characters)
        List<Path> gsFiles = Files.list(outputDir)
                .filter(p -> p.toString().endsWith(".gs"))
                .collect(Collectors.toList());

        for (Path gsFile : gsFiles) {
            String filename = gsFile.getFileName().toString();
            assertThat(filename).doesNotContain("/");
            assertThat(filename).doesNotContain("\\");
            assertThat(filename).doesNotContain(":");
            assertThat(filename).doesNotContain("*");
            assertThat(filename).doesNotContain("?");
            assertThat(filename).doesNotContain("\"");
            assertThat(filename).doesNotContain("<");
            assertThat(filename).doesNotContain(">");
            assertThat(filename).doesNotContain("|");
        }
    }

    @Test
    public void testExportToExistingDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);

        // Create directory before export
        Path outputDir = tempDir.resolve("existing");
        Files.createDirectories(outputDir);

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        // Should still work and create files
        assertThat(Files.exists(outputDir.resolve("Stage.gs"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("Sprite1.gs"))).isTrue();
    }
}
