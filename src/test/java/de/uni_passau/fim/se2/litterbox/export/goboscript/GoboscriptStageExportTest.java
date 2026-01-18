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
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class GoboscriptStageExportTest implements JsonTest {

    private static final String SB3_FIXTURE = "src/test/fixtures/emptyProject.sb3";

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

        // Even with no assets, Stage.gs MUST be created
        Path stageFile = outputDir.resolve("stage.gs");
        
        // This assertion is expected to fail before the fix
        assertThat(Files.exists(stageFile)).isTrue();
        
        String stageContent = Files.readString(stageFile);
        assertThat(stageContent).contains("stage Stage");
    }
}
