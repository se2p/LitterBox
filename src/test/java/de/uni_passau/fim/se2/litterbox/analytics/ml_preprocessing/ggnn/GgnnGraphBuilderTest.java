package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

class GgnnGraphBuilderTest implements JsonTest {
    @Test
    void testNodeIndicesSequential() throws Exception {
        Program p = getAST("src/test/fixtures/multipleSprites.json");
        GgnnGraphBuilder builder = new GgnnGraphBuilder(p);
        GgnnProgramGraph.ContextGraph graph = builder.build();
        Map<Integer, String> nodes = graph.getNodeLabels();

        List<Integer> nodeIndices = nodes.keySet().stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < nodeIndices.size(); ++i) {
            assertThat(nodeIndices.get(i)).isEqualTo(i);
        }
    }

    @Test
    void testLiteralNodeLabels() throws Exception {
        Program p = getAST("src/test/fixtures/ml_preprocessing/ggnn/literal_nodes.json");
        GgnnGraphBuilder builder = new GgnnGraphBuilder(p);
        GgnnProgramGraph.ContextGraph graph = builder.build();
        Map<Integer, String> nodes = graph.getNodeLabels();

        Set<String> labels = new HashSet<>(nodes.values());
        // positive numbers remain as is; special symbols should not be removed from negative numbers;
        // spaces are removed from strings; colours as hexadecimal RGB
        assertThat(labels).containsAtLeast("10", "-10", "What'syourname?", "84135d");
    }
}
