/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
