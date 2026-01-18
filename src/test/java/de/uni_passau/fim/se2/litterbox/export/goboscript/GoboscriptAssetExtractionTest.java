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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptAssetExtractionTest implements JsonTest {

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

        // We need a dummy program, just use emptyProject.sb3's AST as we only care about extraction here
        // The exporter requires a program, but the extraction part only reads the zip
        Program program = getAST("src/test/fixtures/emptyProject.sb3");
        
        Path outputDir = tempDir.resolve("project");

        GoboscriptProjectExporter exporter = new GoboscriptProjectExporter();
        exporter.export(sb3File, program, outputDir);

        Path assetsDir = outputDir.resolve("assets");
        
        // This assertion checks if the file is flattened
        // If the bug exists, this might fail to exist OR exist in assets/nested/folder/custom.svg
        // The fix should ensure it exists at assets/custom.svg
        assertThat(Files.exists(assetsDir.resolve("custom.svg"))).isTrue();
        
        // Ensure no directories were created in assets
        assertThat(Files.isDirectory(assetsDir.resolve("nested"))).isFalse();
    }
}
