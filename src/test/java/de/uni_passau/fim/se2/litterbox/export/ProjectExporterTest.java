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

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;

public class ProjectExporterTest {

    private static class TestExporter extends ProjectExporter {
        @Override
        public void export(Path sb3File, Program program, Path outputDir) throws IOException {
            // no-op
        }

        // Expose protected method for testing
        @Override
        public String sanitizeFilename(String name) {
            return super.sanitizeFilename(name);
        }
    }

    @Test
    public void testSanitizeFilenameReplacesInvalidChars() {
        TestExporter exporter = new TestExporter();
        
        assertThat(exporter.sanitizeFilename("validName")).isEqualTo("validName");
        assertThat(exporter.sanitizeFilename("invalid/name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid\\name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid:name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid*name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid?name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid\"name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid<name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid>name")).isEqualTo("invalid_name");
        assertThat(exporter.sanitizeFilename("invalid|name")).isEqualTo("invalid_name");
    }

    @Test
    public void testSanitizeFilenameTruncatesLongNames() {
        TestExporter exporter = new TestExporter();
        
        // Create a string with 300 'a' characters
        String longName = String.join("", Collections.nCopies(300, "a"));
        
        String sanitized = exporter.sanitizeFilename(longName);
        
        // Should be truncated to the maximum length
        assertThat(sanitized.length()).isEqualTo(ProjectExporter.MAX_FILENAME_LENGTH);
        assertThat(longName).startsWith(sanitized);
    }
}
