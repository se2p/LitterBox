package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class GgnnProgramGraphDotGraphBuilderTest implements JsonTest {

    @Test
    void testAllElementsPresent() throws Exception {
        Path filePath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(filePath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, filePath, true, false, null);
        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(3);

        String dotGraph = graphTask.generateDotGraphData(graphs, "multipleSprites");
        // one subgraph per sprite
        assertThat(substringCount(dotGraph, "subgraph")).isEqualTo(3);

        long totalEdges = graphs.stream()
                .flatMapToLong(g -> g.getContextGraph().getEdges().values().stream().mapToLong(Set::size))
                .sum();
        assertThat(substringCount(dotGraph, "->")).isEqualTo(totalEdges);
    }

    private int substringCount(String searchIn, String substring) {
        return (searchIn.length() - searchIn.replace(substring, "").length()) / substring.length();
    }
}
