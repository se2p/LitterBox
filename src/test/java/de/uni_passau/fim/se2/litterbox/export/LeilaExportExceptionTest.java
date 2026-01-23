package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LeilaExportExceptionTest implements JsonTest {

    private static final String JSON_FIXTURE = "src/test/fixtures/scratchblocks/motionblocks.json";

    @Test
    public void testLeilaExportMultipleProjectsToSingleFileFails(@TempDir Path tempDir) throws IOException, ParsingException {
        // Setup: Create input directory with 2 project files
        Path inputDir = tempDir.resolve("input");
        Files.createDirectory(inputDir);
        Path project1 = inputDir.resolve("project1.json");
        Path project2 = inputDir.resolve("project2.json");
        
        String fixtureContent = Files.readString(Path.of(JSON_FIXTURE));
        Files.writeString(project1, fixtureContent);
        Files.writeString(project2, fixtureContent);

        // Setup: Output is a FILE
        Path outputFile = tempDir.resolve("output.leila");

        // Execute: Export
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.LEILA, outputFile, false, false, false);
        
        // Assert: Should throw IOException because we are exporting multiple projects to a single file
        IOException e = assertThrows(IOException.class, () -> {
            analyzer.analyzeFile(inputDir);
        });
        
        assertThat(e).hasMessageThat().contains("Cannot export multiple projects");
    }
}
