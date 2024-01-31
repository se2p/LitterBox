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
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;
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
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class GenerateGgnnGraphTaskTest implements JsonTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testBlankLabel(boolean wholeProgram) throws Exception {
        Path fixture = Path.of("src", "test", "fixtures", "emptyProject.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, false, wholeProgram, "  \t\n  ");

        String actualLabel = graphs.get(0).label();
        if (wholeProgram) {
            assertThat(actualLabel).isEqualTo("emptyProject");
        } else {
            assertThat(actualLabel).isEqualTo("sprite");
        }
    }

    @Test
    void testIgnoreDefaultSpriteNames() throws Exception {
        Path fixture = Path.of("src", "test", "fixtures", "emptyProject.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, false, false, false, "  \t\n  ");

        assertThat(graphs).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testFixedLabel(boolean wholeProgram) throws Exception {
        Path fixture = Path.of("src", "test", "fixtures", "multipleSprites.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, true, wholeProgram, "fixed");
        for (GgnnProgramGraph g : graphs) {
            assertThat(g.label()).isEqualTo("fixed");
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
        assertThat(graphs.get(0).contextGraph().nodeLabels()).hasSize(expectedNodeCount);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphWholeProgram(boolean includeStage) throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(inputPath.toString());
        GenerateGgnnGraphTask graphTask = new GenerateGgnnGraphTask(
                program, includeStage, true, true, null, ActorNameNormalizer.getDefault()
        );

        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(1);

        List<String> graphJsonl = graphTask.generateJsonGraphData().collect(Collectors.toList());
        assertThat(graphJsonl).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphIncludeStage(boolean includeStage) throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(inputPath.toString());
        GenerateGgnnGraphTask graphTask = new GenerateGgnnGraphTask(
                program, includeStage, false, false, null, ActorNameNormalizer.getDefault()
        );

        int expectedSprites;
        if (includeStage) {
            expectedSprites = 3;
        } else {
            expectedSprites = 2;
        }

        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(expectedSprites);

        List<String> graphJsonl = graphTask.generateJsonGraphData().collect(Collectors.toList());
        assertThat(graphJsonl).hasSize(expectedSprites);
    }

    @Test
    void testVariableGuardedBy() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);
        assertThat(spriteGraph.label()).isEqualTo("sprite");
        assertThat(spriteGraph.filename()).endsWith("guarded_by");
        assertThat(spriteGraph.contextGraph().nodeLabels()).hasSize(spriteGraph.contextGraph().nodeTypes().size());

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
    void testVariablesGuardedByRepeat() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by_repeat.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 2);
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 2);

        Pair<String> edgeRepeatUntil = Pair.of("Variable", "BiggerThan");
        Pair<String> edgeRepeat = Pair.of("Variable", "AsNumber");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edgeRepeatUntil, edgeRepeat));
    }

    @Test
    void testAttributesOfGuardedBy() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by_attribute_of.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        // only a single edge should be added, the other two attribute of usages within the block query other attributes
        List<Pair<String>> expectedEdges = List.of(Pair.of("AttributeOf", "AsNumber"));
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, expectedEdges);
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
    void testVariableComputedFromNumericalAttributes() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "computed_from_numerical_attributes.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, 3);

        List<Pair<String>> expectedEdges = Stream.of("PositionX", "Size", "DistanceTo", "Volume",
                        "PositionY", "MouseX", "Loudness", "Costume", "Current",
                        "Direction", "MouseY", "Timer", "Backdrop", "DaysSince2000")
                .map(target -> Pair.of("Variable", target))
                .collect(Collectors.toList());
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, expectedEdges);
    }

    @Test
    void testVariableComputedFromStringAttributes() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "computed_from_string_attributes.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, 1);

        List<Pair<String>> expectedEdges = Stream.of("Answer", "AttributeOf", "Username")
                .map(target -> Pair.of("Variable", target))
                .collect(Collectors.toList());
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, expectedEdges);
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
    void testParameterPassingNoParameters() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "parameterless_procedure.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        Pair<String> expectedEdge = Pair.of("CallStmt", "ProcedureDefinition");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, List.of(expectedEdge));
    }

    @Test
    void testMessagePassing() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "message_passing.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);

        Pair<String> expectedEdge1 = Pair.of("Broadcast", "ReceptionOfMessage");
        Pair<String> expectedEdge2 = Pair.of("Broadcast", "ReceptionOfMessage");
        Pair<String> expectedEdge3 = Pair.of("BroadcastAndWait", "ReceptionOfMessage");
        List<Pair<String>> expectedEdges = List.of(expectedEdge1, expectedEdge2, expectedEdge3);
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.MESSAGE_PASSING, expectedEdges);

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.MESSAGE_PASSING, 2);
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.MESSAGE_PASSING, 3);
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

        assertHasEdges(stageGraph, GgnnProgramGraph.EdgeType.DATA_DEPENDENCY,
                List.of(expectedEdge1, expectedEdge2, expectedEdge3, expectedEdge4));
    }

    @Test
    void testAttributeDataDependency() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "data_dependency_attribute.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph stageGraph = graphs.get(0);

        Pair<String> expectedEdge = Pair.of("TurnRight", "TurnRight");
        assertHasEdges(stageGraph, GgnnProgramGraph.EdgeType.DATA_DEPENDENCY, List.of(expectedEdge));
    }

    @Test
    void testLabelIndicesWholeProgram() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph programGraph = graphs.get(0);

        Set<Integer> expectedNodeIndices = programGraph.contextGraph().nodeLabels().entrySet().stream()
                .filter(entry -> "Program".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());

        assertThat(programGraph.labelNodes()).containsExactlyElementsIn(expectedNodeIndices);
        assertThat(programGraph.labelNodes()).hasSize(1);
    }

    @Test
    void testLabelIndicesSprite() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);

        assertAll(graphs.stream().map(graph -> () -> assertHasSingleLabelNodeIndex(graph)));
    }

    private void assertHasSingleLabelNodeIndex(final GgnnProgramGraph graph) {
        Set<Integer> expectedNodeIndices = graph.contextGraph().nodeLabels().entrySet().stream()
                .filter(entry -> "ActorDefinition".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());

        assertThat(graph.labelNodes()).containsExactlyElementsIn(expectedNodeIndices);
        assertThat(graph.labelNodes()).hasSize(1);
    }

    private void assertHasEdges(
            final GgnnProgramGraph graph,
            final GgnnProgramGraph.EdgeType edgeType,
            final List<Pair<String>> expectedEdges
    ) {
        Set<Pair<Integer>> edges = graph.contextGraph().getEdges(edgeType);
        Map<Integer, String> nodeLabels = graph.contextGraph().nodeLabels();
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
        Set<Pair<Integer>> paramEdges = graph.contextGraph().getEdges(edgeType);
        Set<Integer> edgeTargets = paramEdges.stream()
                .map(p -> start ? p.getFst() : p.getSnd())
                .collect(Collectors.toSet());
        assertThat(edgeTargets).hasSize(expectedCount);
    }

    private List<GgnnProgramGraph> getGraphs(Path fixturePath, boolean includeStage, boolean wholeProgram)
            throws Exception {
        return getGraphs(fixturePath, includeStage, wholeProgram, null);
    }

    private List<GgnnProgramGraph> getGraphs(Path fixturePath, boolean includeStage, boolean wholeProgram, String label)
            throws Exception {
        return getGraphs(fixturePath, includeStage, true, wholeProgram, label);
    }

    private List<GgnnProgramGraph> getGraphs(
            Path fixturePath, boolean includeStage, boolean includeDefaultSprites, boolean wholeProgram, String label
    ) throws Exception {
        Program program = getAST(fixturePath.toString());
        GenerateGgnnGraphTask graphTask = new GenerateGgnnGraphTask(
                program, includeStage, includeDefaultSprites, wholeProgram, label,
                ActorNameNormalizer.getDefault()
        );
        return graphTask.getProgramGraphs();
    }

    private List<Pair<String>> labelledEdges(final Set<Pair<Integer>> edges, final Map<Integer, String> labels) {
        return edges.stream().map(e -> labelledEdge(labels, e)).collect(Collectors.toList());
    }

    private Pair<String> labelledEdge(final Map<Integer, String> labels, final Pair<Integer> edge) {
        return Pair.of(labels.get(edge.getFst()), labels.get(edge.getSnd()));
    }
}
