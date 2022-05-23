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
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

class GenerateGraphTaskTest implements JsonTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testBlankLabel(boolean wholeProgram) throws Exception {
        Path fixture = Path.of("src", "test", "fixtures", "emptyProject.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, false, wholeProgram, "  \t\n  ");

        String actualLabel = graphs.get(0).getLabel();
        if (wholeProgram) {
            assertThat(actualLabel).isEqualTo("emptyProject");
        } else {
            assertThat(actualLabel).isEqualTo("Sprite1");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testFixedLabel(boolean wholeProgram) throws Exception {
        Path fixture = Path.of("src", "test", "fixtures", "multipleSprites.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, true, wholeProgram, "fixed");
        for (GgnnProgramGraph g : graphs) {
            assertThat(g.getLabel()).isEqualTo("fixed");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphEmptyProgram(boolean wholeProgram) throws Exception {
        List<GgnnProgramGraph> graphs = getGraphs(Path.of("src", "test", "fixtures", "emptyProject.json"), false, wholeProgram);
        assertThat(graphs).hasSize(1);

        int expectedNodeCount;
        if (wholeProgram) {
            expectedNodeCount = 20;
        } else {
            expectedNodeCount = 8;
        }
        assertThat(graphs.get(0).getContextGraph().getNodeLabels()).hasSize(expectedNodeCount);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphWholeProgram(boolean includeStage) throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(inputPath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, inputPath, includeStage, true, null);

        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(1);

        String graphJsonl = graphTask.generateJsonGraphData();
        assertThat(graphJsonl.lines().collect(Collectors.toList())).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphIncludeStage(boolean includeStage) throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(inputPath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, inputPath, includeStage, false, null);

        int expectedSprites;
        if (includeStage) {
            expectedSprites = 3;
        } else {
            expectedSprites = 2;
        }

        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(expectedSprites);

        String graphJsonl = graphTask.generateJsonGraphData();
        assertThat(graphJsonl.lines().collect(Collectors.toList())).hasSize(expectedSprites);
    }

    @Test
    void testVariableGuardedBy() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);
        assertThat(spriteGraph.getLabel()).isEqualTo("Sprite1");
        assertThat(spriteGraph.getFilename()).endsWith("guarded_by.json");
        assertThat(spriteGraph.getContextGraph().getNodeLabels()).hasSize(spriteGraph.getContextGraph().getNodeTypes().size());

        Pair<String> edge = Pair.of("Variable", "BiggerThan");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edge));
    }

    @Test
    void testVariablesGuardedByIfElse() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by_if_else_multiple.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        // three different variables are connected to the expression
        Pair<String> edge = Pair.of("Variable", "BiggerThan");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edge, edge, edge));
        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 3);
    }

    @Test
    void testVariablesComputedFrom() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "computed_from.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        // two different variables on the right side
        Pair<String> edge = Pair.of("Variable", "Variable");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, List.of(edge, edge));
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, 2);
    }

    @Test
    void testParameterPassing() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "parameter_passing.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        Pair<String> expectedEdge1 = Pair.of("some_text_input", "ParameterDefinition");
        Pair<String> expectedEdge2 = Pair.of("BiggerThan", "ParameterDefinition");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, List.of(expectedEdge1, expectedEdge2));

        // two different parameter definitions as targets
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, 2);
    }

    @Test
    void testVariableDependency() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "variable_dependency.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph stageGraph = graphs.get(0);

        Pair<String> expectedEdge1 = Pair.of("SetVariableTo", "ChangeVariableBy");
        Pair<String> expectedEdge2 = Pair.of("SetVariableTo", "SetVariableTo");
        Pair<String> expectedEdge3 = Pair.of("ChangeVariableBy", "ChangeVariableBy");
        Pair<String> expectedEdge4 = Pair.of("ChangeVariableBy", "SetVariableTo");

        assertHasEdges(stageGraph, GgnnProgramGraph.EdgeType.VARIABLE_USE,
                List.of(expectedEdge1, expectedEdge2, expectedEdge3, expectedEdge4));
    }

    private void assertHasEdges(final GgnnProgramGraph graph, final GgnnProgramGraph.EdgeType edgeType,
                                final List<Pair<String>> expectedEdges) {
        Set<Pair<Integer>> edges = graph.getContextGraph().getEdges(edgeType);
        Map<Integer, String> nodeLabels = graph.getContextGraph().getNodeLabels();
        List<Pair<String>> labelledEdges = labelledEdges(edges, nodeLabels);
        assertThat(labelledEdges).containsExactlyElementsIn(expectedEdges);
    }

    private void assertDifferentEdgeStartsCount(final GgnnProgramGraph graph, final GgnnProgramGraph.EdgeType edgeType, int expectedCount) {
        assertDifferentEdgeCounts(graph, edgeType, expectedCount, true);
    }

    private void assertDifferentEdgeTargetsCount(final GgnnProgramGraph graph, final GgnnProgramGraph.EdgeType edgeType, int expectedCount) {
        assertDifferentEdgeCounts(graph, edgeType, expectedCount, false);
    }

    private void assertDifferentEdgeCounts(final GgnnProgramGraph graph, final GgnnProgramGraph.EdgeType edgeType, int expectedCount, boolean start) {
        Set<Pair<Integer>> paramEdges = graph.getContextGraph().getEdges(edgeType);
        Set<Integer> edgeTargets = paramEdges.stream()
                .map(p -> start ? p.getFst() : p.getSnd())
                .collect(Collectors.toSet());
        assertThat(edgeTargets).hasSize(expectedCount);
    }

    private List<GgnnProgramGraph> getGraphs(Path fixturePath, boolean includeStage, boolean wholeProgram) throws Exception {
        return getGraphs(fixturePath, includeStage, wholeProgram, null);
    }

    private List<GgnnProgramGraph> getGraphs(Path fixturePath, boolean includeStage, boolean wholeProgram, String label) throws Exception {
        Program program = getAST(fixturePath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, fixturePath, includeStage, wholeProgram, label);
        return graphTask.getProgramGraphs();
    }

    private List<Pair<String>> labelledEdges(final Set<Pair<Integer>> edges, final Map<Integer, String> labels) {
        return edges.stream().map(e -> labelledEdge(labels, e)).collect(Collectors.toList());
    }

    private Pair<String> labelledEdge(final Map<Integer, String> labels, final Pair<Integer> edge) {
        return Pair.of(labels.get(edge.getFst()), labels.get(edge.getSnd()));
    }
}
