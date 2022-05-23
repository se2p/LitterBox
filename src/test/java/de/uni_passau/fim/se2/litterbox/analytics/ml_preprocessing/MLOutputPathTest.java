package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MLOutputPathTest {
    @Test
    void testDisallowDirectories() {
        Path f = Path.of("src", "test", "fixtures", "emptyProject.json");
        assertThat(f.toFile().exists()).isTrue();

        assertThrows(IllegalArgumentException.class, () -> MLOutputPath.directory(f));
    }

    @Test
    void testAllowNonExistingDirectories() {
        Path d = Path.of("tmp", UUID.randomUUID().toString());
        assertThat(d.toFile().exists()).isFalse();
        MLOutputPath outputPath = MLOutputPath.directory(d);
        assertThat(outputPath.getPath().toFile()).isEqualTo(d.toFile());
    }

    @Test
    void testConsoleOutput() {
        MLOutputPath p = MLOutputPath.console();
        assertThat(p.isConsoleOutput()).isTrue();
        assertThat(p.toString()).isEqualTo("CONSOLE");
    }
}
