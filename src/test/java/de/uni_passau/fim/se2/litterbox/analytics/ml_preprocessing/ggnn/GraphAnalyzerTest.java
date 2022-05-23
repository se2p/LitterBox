package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class GraphAnalyzerTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testProduceOutput(boolean toDotGraph, @TempDir Path outputDir) throws IOException {
        MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
                "src/test/fixtures/multipleSprites.json", MLOutputPath.directory(outputDir), false, true, false);
        GraphAnalyzer analyzer = new GraphAnalyzer(commonOptions, toDotGraph, null);
        analyzer.analyzeFile();

        Path expectedOutputFile = outputDir.resolve(expectedOutputFilename("multipleSprites", toDotGraph));
        assertThat(expectedOutputFile.toFile().exists()).isTrue();

        List<String> output = Files.readAllLines(expectedOutputFile);
        if (!toDotGraph) {
            assertThat(output).hasSize(3);
        } else {
            assertThat(output.get(0)).startsWith("digraph");
        }
    }

    private String expectedOutputFilename(String inputFile, boolean dotGraph) {
        return String.format("GraphData_%s.%s", inputFile, dotGraph ? "dot" : "jsonl");
    }
}
