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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        assertThat(Files.exists(outputDir.resolve("stage.gs"))).isTrue();
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

        // Check stage.gs content
        String stageContent = Files.readString(outputDir.resolve("stage.gs"));
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

        // Check stage.gs content has pop sound
        String stageContent = Files.readString(outputDir.resolve("stage.gs"));
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
        assertThat(Files.exists(outputDir.resolve("stage.gs"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("Sprite1.gs"))).isTrue();
    }

    // ==================== Nested Asset Extraction ====================

    @Test
    public void testNestedAssetExtraction(@TempDir Path tempDir) throws IOException, ParsingException {
        // Create a fake sb3 zip with a nested asset
        Path sb3File = tempDir.resolve("nested.sb3");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(sb3File))) {
            // Add project.json (minimal)
            ZipEntry jsonEntry = new ZipEntry("project.json");
            zos.putNextEntry(jsonEntry);
            zos.write("{}".getBytes());
            zos.closeEntry();

            // Add a nested asset
            ZipEntry nestedEntry = new ZipEntry("nested/folder/custom.svg");
            zos.putNextEntry(nestedEntry);
            zos.write("<svg></svg>".getBytes());
            zos.closeEntry();
        }

        // Use emptyProject.sb3's AST as we only care about extraction here
        Program program = getAST("src/test/fixtures/emptyProject.sb3");

        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        Path assetsDir = outputDir.resolve("assets");

        // Nested asset should be flattened to assets/custom.svg
        assertThat(Files.exists(assetsDir.resolve("custom.svg"))).isTrue();

        // Ensure no nested directories were created in assets
        assertThat(Files.isDirectory(assetsDir.resolve("nested"))).isFalse();
    }

    // ==================== Missing Assets Handling ====================

    // Subclass to override asset extraction and simulate missing assets
    private static class TestExporter extends GoboscriptProjectExporter {
        @Override
        protected Set<String> extractAssets(Path sb3File, Path outputDir) throws IOException {
            // Simulate that NO assets were extracted
            return Collections.emptySet();
        }
    }

    @Test
    public void testStageIsExportedEvenWithMissingAssets(@TempDir Path tempDir) throws IOException, ParsingException {
        Path sb3File = Path.of(SB3_FIXTURE);
        Program program = getAST(SB3_FIXTURE);
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new TestExporter();
        exporter.export(sb3File, program, outputDir);

        // Even with no assets, stage.gs MUST be created
        Path stageFile = outputDir.resolve("stage.gs");

        assertThat(Files.exists(stageFile)).isTrue();

        String stageContent = Files.readString(stageFile);
        assertThat(stageContent).contains("stage Stage");
    }
}
