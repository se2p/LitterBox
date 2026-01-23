package de.uni_passau.fim.se2.litterbox.export;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

public class ScratchBlocksExportTest implements JsonTest {

    private static final String JSON_FIXTURE = "src/test/fixtures/scratchblocks/motionblocks.json";

    @Test
    public void testScratchBlocksExportMultipleProjectsToDirectory(@TempDir Path tempDir) throws IOException, ParsingException {
        // Setup: Create input directory with 2 project files
        Path inputDir = tempDir.resolve("input");
        Files.createDirectory(inputDir);
        Path project1 = inputDir.resolve("project1.json");
        Path project2 = inputDir.resolve("project2.json");
        
        String fixtureContent = Files.readString(Path.of(JSON_FIXTURE));
        Files.writeString(project1, fixtureContent);
        Files.writeString(project2, fixtureContent);

        // Setup: Create output directory
        Path outputDir = tempDir.resolve("output");
        Files.createDirectory(outputDir);

        // Execute: Export
        ExportAnalyzer analyzer = new ExportAnalyzer(
                ExportFormat.SCRATCHBLOCKS, outputDir, false, false, false);
        
        // Analyze the directory (which iterates over project1 and project2)
        analyzer.analyzeFile(inputDir);

        // Assert: Should have separate files in output directory
        assertThat(Files.isDirectory(outputDir)).isTrue();
        
        // We expect ONE file per project.
        // If checking behavior is merged, we might see only one file or weird content.
        
        boolean project1Exists = Files.exists(outputDir.resolve("project1.txt"));
        boolean project2Exists = Files.exists(outputDir.resolve("project2.txt"));
        
        // If it merged, it might have written to a single file inside or maybe to the dir pointer itself (which is impossible as it exists).
        // Actually, if it merges, maybe it didn't create ANY files inside outputDir if it thought outputDir was the file? 
        // But outputDir exists as directory.
        
        assertThat(project1Exists).isTrue();
        assertThat(project2Exists).isTrue();
    }
}
