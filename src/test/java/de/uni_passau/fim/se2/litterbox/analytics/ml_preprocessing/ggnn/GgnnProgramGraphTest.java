package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GgnnProgramGraphTest implements JsonTest {
    @Test
    void testConstructionMissingEdgeTypes() {
        final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = new HashMap<>();
        for (GgnnProgramGraph.EdgeType t : GgnnProgramGraph.EdgeType.values()) {
            edges.put(t, new HashSet<>());
        }
        edges.remove(GgnnProgramGraph.EdgeType.CHILD);

        assertThrows(IllegalArgumentException.class, () -> new GgnnProgramGraph.ContextGraph(edges, Map.of(), Map.of()));
    }

    @Test
    void testContainsAllEdgeTypes() throws Exception {
        Program p = getAST("src/test/fixtures/emptyProject.json");
        GgnnGraphBuilder builder = new GgnnGraphBuilder(p);
        GgnnProgramGraph.ContextGraph graph = builder.build();

        Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = graph.getEdges();
        int expectedSize = GgnnProgramGraph.EdgeType.values().length;
        assertThat(edges).hasSize(expectedSize);
    }
}
