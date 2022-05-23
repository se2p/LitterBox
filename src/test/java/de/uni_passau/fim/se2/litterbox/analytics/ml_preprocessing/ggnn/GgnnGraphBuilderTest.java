package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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
}
