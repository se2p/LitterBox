/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.JsonTest;
import de.uni_passau.fim.se2.embedded_kittens.MLOutputPath;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.DeclarationStmtList;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

class GenerateGgnnGraphTaskTest implements JsonTest {

    private static final String VAR_TYPE = "Variable";
    private static final String LIST_TYPE = "ScratchList";

    private final Path multipleSpritesFixture = fixture("multipleSprites.json");

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testBlankLabel(boolean wholeProgram) throws Exception {
        Path fixture = fixture("emptyProject.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, false, wholeProgram, "  \t\n  ");

        String actualLabel = graphs.getFirst().label();
        if (wholeProgram) {
            assertThat(actualLabel).isEqualTo("emptyProject");
        }
        else {
            assertThat(actualLabel).isEqualTo("sprite");
        }
    }

    @Test
    void testIgnoreDefaultSpriteNames() throws Exception {
        Path fixture = fixture("emptyProject.json");
        List<GgnnProgramGraph> graphs = getGraphs(fixture, false, false, false, "  \t\n  ");

        assertThat(graphs).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testFixedLabel(boolean wholeProgram) throws Exception {
        List<GgnnProgramGraph> graphs = getGraphs(multipleSpritesFixture, true, wholeProgram, "fixed");
        for (GgnnProgramGraph g : graphs) {
            assertThat(g.label()).isEqualTo("fixed");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGraphEmptyProgram(boolean wholeProgram) throws Exception {
        Path path = fixture("emptyProject.json");
        List<GgnnProgramGraph> graphs = getGraphs(path, false, wholeProgram);
        assertThat(graphs).hasSize(1);

        int expectedNodeCount;
        if (wholeProgram) {
            expectedNodeCount = 13;
        }
        else {
            expectedNodeCount = 5;
        }
        assertThat(graphs.getFirst().contextGraph().nodeLabels()).hasSize(expectedNodeCount);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGraphWholeProgram(boolean includeStage) throws Exception {
        Program program = getAST(multipleSpritesFixture);
        MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
            MLOutputPath.console(), includeStage, true, false, ActorNameNormalizer.getDefault()
        );

        {
            GgnnProgramPreprocessor graphTask = new GgnnProgramPreprocessor(
                commonOptions, GgnnOutputFormat.JSON_GRAPH, null
            );
            List<GgnnProgramGraph> graphs = graphTask.processWholeProgram(program)
                .map(g -> ((GgnnAnalyzerOutput.Graph) g).graph())
                .toList();
            assertThat(graphs).hasSize(1);
        }

        {
            GgnnProgramPreprocessor graphTask = new GgnnProgramPreprocessor(
                commonOptions, GgnnOutputFormat.DOT_GRAPH, null
            );
            List<String> graphJsonl = graphTask.processWholeProgram(program).map(graphTask::resultToString).toList();
            assertThat(graphJsonl).hasSize(1);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGraphIncludeStage(boolean includeStage) throws Exception {
        Program program = getAST(multipleSpritesFixture);
        MLPreprocessorCommonOptions commonOptions = new MLPreprocessorCommonOptions(
            MLOutputPath.console(), includeStage, true, false, ActorNameNormalizer.getDefault()
        );
        GgnnProgramPreprocessor graphTask = new GgnnProgramPreprocessor(
            commonOptions, GgnnOutputFormat.JSON_GRAPH, null
        );

        int expectedSprites;
        if (includeStage) {
            expectedSprites = 3;
        }
        else {
            expectedSprites = 2;
        }

        List<GgnnProgramGraph> graphs = graphTask.processSprites(program)
            .map(g -> ((GgnnAnalyzerOutput.Graph) g).graph())
            .toList();
        assertThat(graphs).hasSize(expectedSprites);

        List<String> graphJsonl = graphTask.processSprites(program).map(graphTask::resultToString).toList();
        assertThat(graphJsonl).hasSize(expectedSprites);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testGraphDoesNotContainActorHelperNodes(final boolean wholeProgram) throws Exception {
        List<GgnnProgramGraph> graphs = getGraphs(multipleSpritesFixture, true, wholeProgram);

        assertAll(graphs.stream().map(graph -> () -> assertNoHelperNodes(graph)));
    }

    private void assertNoHelperNodes(final GgnnProgramGraph graph) {
        assertThat(graph.contextGraph().nodeTypes().values()).containsNoneOf(
            DeclarationStmt.class.getSimpleName(),
            DeclarationStmtList.class.getSimpleName(),
            SetStmtList.class.getSimpleName()
        );

        Stream<Executable> metadataAssertions = graph.contextGraph().nodeTypes().values().stream()
            .map(nodeType -> () -> {
                String message = String.format(
                    "expected node types to not contain metadata nodes, but contained '%s'",
                    nodeType
                );
                assertFalse(nodeType.matches("Metadata$"), message);
            });
        assertAll(metadataAssertions);
    }

    @Test
    void testNextToken() throws Exception {
        Path inputPath = ggnnFixture("guarded_by.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        List<Pair<String>> edges = List.of(
            Pair.of("Never", "StmtList"),
            Pair.of("Say", "Think")
        );
        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.NEXT_TOKEN, edges);
    }

    @Test
    void testLastLexicalUseList() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_list_dropdown_in_condition.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        List<Pair<String>> edges = List.of(
            Pair.of(LIST_TYPE, LIST_TYPE),
            Pair.of(LIST_TYPE, LIST_TYPE)
        );
        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.LAST_LEXICAL_USE, edges);
    }

    @Test
    void testLastLexicalUseDifferentVariables() throws Exception {
        Path inputPath = ggnnFixture("last_lexical_use_different_variables.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        List<Pair<String>> edges = List.of(Pair.of("Variable", "Variable"));
        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.LAST_LEXICAL_USE, edges);
    }

    @Test
    void testLastLexicalUseListAndVariableSameName() throws Exception {
        Path inputPath = ggnnFixture("last_lexical_use_list_var_same_name.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.LAST_LEXICAL_USE, Collections.emptyList());
    }

    @Test
    void testLastLexicalUseVariableMultipleTimesSameBlock() throws Exception {
        Path inputPath = ggnnFixture("last_lexical_use_variable_same_block_multiple_times.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        List<Pair<String>> edges = List.of(Pair.of("Variable", "Variable"));
        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.LAST_LEXICAL_USE, edges);
    }

    @Test
    void testLastLexicalUseNoConnectionBetweenScripts() throws Exception {
        Path inputPath = ggnnFixture("last_lexical_use_no_connection_between_scripts.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.LAST_LEXICAL_USE, Collections.emptyList());
    }

    @Test
    void testLastLexicalUseConnectVolumeAttribute() throws Exception {
        Path inputPath = ggnnFixture("last_lexical_use_connect_attributes.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph graph = graphs.getFirst();

        Pair<String> edge = Pair.of("Volume", "Volume");
        assertContainsEdges(graph, GgnnProgramGraph.EdgeType.LAST_LEXICAL_USE, List.of(edge));
    }

    @Test
    void testVariableGuardedBy() throws Exception {
        Path inputPath = ggnnFixture("guarded_by.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();
        assertThat(spriteGraph.label()).isEqualTo("sprite");
        assertThat(spriteGraph.filename()).endsWith("guarded_by");
        assertThat(spriteGraph.contextGraph().nodeLabels()).hasSize(spriteGraph.contextGraph().nodeTypes().size());

        Pair<String> edge = Pair.of(VAR_TYPE, "BiggerThan");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edge));
    }

    @Test
    void testVariablesGuardedByIfElse() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_if_else_multiple.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        // three different variables are connected to the expression
        Pair<String> edge = Pair.of(VAR_TYPE, "BiggerThan");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edge, edge, edge));
        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 3);
    }

    @Test
    void testVariablesGuardedByRepeat() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_repeat.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 2);
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 2);

        Pair<String> edgeRepeatUntil = Pair.of(VAR_TYPE, "BiggerThan");
        Pair<String> edgeRepeat = Pair.of(VAR_TYPE, "AsNumber");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edgeRepeatUntil, edgeRepeat));
    }

    @Test
    void testVariablesUsedInDropdownsGuardedBy() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_variable_use_dropdowns.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 5);
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 1);

        Pair<String> edge = Pair.of(VAR_TYPE, "Add");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edge, edge, edge, edge, edge));
    }

    @Test
    void testListUsedInDropdownGuardedBy() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_list.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 10);
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 1);

        Pair<String> edge = Pair.of(LIST_TYPE, "AsNumber");
        assertHasEdges(
            spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, Stream.generate(() -> edge).limit(10).toList()
        );
    }

    @Test
    void testListUsedInDropdownAsGuard() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_list_dropdown_in_condition.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 2);
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, 1);

        Pair<String> edge = Pair.of(LIST_TYPE, "ListContains");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, List.of(edge, edge));
    }

    @Test
    void testAttributesOfGuardedBy() throws Exception {
        Path inputPath = ggnnFixture("guarded_by_attribute_of.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        // only a single edge should be added, the other two attribute of usages within the block query other attributes
        List<Pair<String>> expectedEdges = List.of(Pair.of("AttributeOf", "AsNumber"));
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.GUARDED_BY, expectedEdges);
    }

    @Test
    void testVariablesComputedFrom() throws Exception {
        Path inputPath = ggnnFixture("computed_from.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        // two different variables on the right side
        Pair<String> edge = Pair.of(VAR_TYPE, VAR_TYPE);
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, List.of(edge, edge));
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, 2);
    }

    @Test
    void testVariableComputedFromNumericalAttributes() throws Exception {
        Path inputPath = ggnnFixture("computed_from_numerical_attributes.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, 3);

        List<Pair<String>> expectedEdges = Stream.of(
            "PositionX", "Size", "DistanceTo", "Volume",
            "PositionY", "MouseX", "Loudness", "Costume", "Current",
            "Direction", "MouseY", "Timer", "Backdrop", "DaysSince2000"
        )
            .map(target -> Pair.of(VAR_TYPE, target))
            .toList();
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, expectedEdges);
    }

    @Test
    void testVariableComputedFromStringAttributes() throws Exception {
        Path inputPath = ggnnFixture("computed_from_string_attributes.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertDifferentEdgeStartsCount(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, 1);

        List<Pair<String>> expectedEdges = Stream.of("Answer", "AttributeOf", "Username")
            .map(target -> Pair.of(VAR_TYPE, target))
            .toList();
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.COMPUTED_FROM, expectedEdges);
    }

    @Test
    void testParameterPassing() throws Exception {
        Path inputPath = ggnnFixture("parameter_passing.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        Pair<String> expectedEdge1 = Pair.of("some_text_input", "ParameterDefinition");
        Pair<String> expectedEdge2 = Pair.of("BiggerThan", "ParameterDefinition");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, List.of(expectedEdge1, expectedEdge2));

        // two different parameter definitions as targets
        assertDifferentEdgeTargetsCount(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, 2);
    }

    @Test
    void testParameterPassingNoParameters() throws Exception {
        Path inputPath = ggnnFixture("parameterless_procedure.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        Pair<String> expectedEdge = Pair.of("CallStmt", "ProcedureDefinition");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, List.of(expectedEdge));
    }

    @Test
    void testMessagePassing() throws Exception {
        Path inputPath = ggnnFixture("message_passing.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

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
        Path inputPath = ggnnFixture("variable_dependency.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph stageGraph = graphs.getFirst();

        Pair<String> expectedEdge1 = Pair.of("SetVariableTo", "ChangeVariableBy");
        Pair<String> expectedEdge3 = Pair.of("ChangeVariableBy", "ChangeVariableBy");
        Pair<String> expectedEdge4 = Pair.of("ChangeVariableBy", "SetVariableTo");

        assertHasEdges(
            stageGraph, GgnnProgramGraph.EdgeType.DATA_DEPENDENCY,
            List.of(expectedEdge1, expectedEdge3, expectedEdge4)
        );
    }

    @Test
    void testAttributeDataDependency() throws Exception {
        Path inputPath = ggnnFixture("data_dependency_attribute.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph stageGraph = graphs.getFirst();

        Pair<String> expectedEdge = Pair.of("TurnRight", "TurnRight");
        assertHasEdges(stageGraph, GgnnProgramGraph.EdgeType.DATA_DEPENDENCY, List.of(expectedEdge));
    }

    @Test
    void testListDataDependency() throws Exception {
        Path inputPath = ggnnFixture("data_dependency_list.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph stageGraph = graphs.getFirst();

        Pair<String> expectedEdge = Pair.of("AddTo", "InsertAt");
        assertHasEdges(stageGraph, GgnnProgramGraph.EdgeType.DATA_DEPENDENCY, List.of(expectedEdge));
    }

    @Test
    void testLabelIndicesWholeProgram() throws Exception {
        List<GgnnProgramGraph> graphs = getGraphs(multipleSpritesFixture, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph programGraph = graphs.getFirst();

        Set<Integer> expectedNodeIndices = programGraph.contextGraph().nodeLabels().entrySet().stream()
            .filter(entry -> "Program".equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toUnmodifiableSet());

        assertThat(programGraph.labelNodes()).containsExactlyElementsIn(expectedNodeIndices);
        assertThat(programGraph.labelNodes()).hasSize(1);
    }

    @Test
    void testLabelIndicesSprite() throws Exception {
        List<GgnnProgramGraph> graphs = getGraphs(multipleSpritesFixture, false, false);

        assertAll(graphs.stream().map(graph -> () -> assertHasSingleLabelNodeIndex(graph)));
    }

    @Test
    void testCustomBlockProcedureName() throws Exception {
        Path inputPath = fixture("customBlocks.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        Collection<String> nodeLabels = spriteGraph.contextGraph().nodeLabels().values();
        assertThat(nodeLabels).containsAtLeast("BlockNoInputs", "BlockWithInputs");
        // block ID of a custom block
        assertThat(nodeLabels).doesNotContain("v_ch_q_tp_zb_ga_pt_mu_5_ok");
    }

    @Test
    void testCustomBlockFoundNumTextParameterTypes() throws Exception {
        Path inputPath = fixture("customBlockNumTextParameters.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();
        Set<Pair<Integer>> edges = spriteGraph.contextGraph().getEdges(GgnnProgramGraph.EdgeType.PARAMETER_PASSING);

        assertThat(edges).hasSize(8);
    }

    @Test
    void testCustomBlockPercentNParameter() throws Exception {
        Path inputPath = fixture("customBlockPercentNParameter.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        int callStmtCount = (int) spriteGraph.contextGraph().nodeLabels().values().stream()
            .filter(label -> label.startsWith("add note"))
            .count();
        Set<Pair<Integer>> edges = spriteGraph.contextGraph().getEdges(GgnnProgramGraph.EdgeType.PARAMETER_PASSING);

        assertThat(callStmtCount).isEqualTo(26);
        assertThat(edges).hasSize(callStmtCount * 2); // two parameters per call
    }

    @Test
    void testConnectParametersCalledCustomBlocks() throws Exception {
        Path inputPath = fixture("customBlocksWithParams.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        Pair<String> expectedEdge = Pair.of("custom_param", "ParameterDefinition");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.PARAMETER_PASSING, List.of(expectedEdge));
    }

    @Test
    void testReturnToEdge() throws Exception {
        Path inputPath = fixture("customBlocksWithParams.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        Pair<String> expectedEdge = Pair.of("PlaySoundUntilDone", "ProcedureDefinition");
        assertHasEdges(spriteGraph, GgnnProgramGraph.EdgeType.RETURN_TO, List.of(expectedEdge));
    }

    @Test
    void testNoReturnToEdgeEmptyProcedure() throws Exception {
        Path inputPath = fixture("customBlockNoStatements.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, true, true);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.getFirst();

        assertThat(spriteGraph.contextGraph().edges().get(GgnnProgramGraph.EdgeType.RETURN_TO)).isEmpty();
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

    private void assertContainsEdges(
        final GgnnProgramGraph graph,
        final GgnnProgramGraph.EdgeType edgeType,
        final List<Pair<String>> expectedEdges
    ) {
        Set<Pair<Integer>> edges = graph.contextGraph().getEdges(edgeType);
        Map<Integer, String> nodeLabels = graph.contextGraph().nodeLabels();
        List<Pair<String>> labelledEdges = labelledEdges(edges, nodeLabels);
        assertThat(labelledEdges).containsAtLeastElementsIn(expectedEdges);
    }

    private void assertDifferentEdgeStartsCount(
        final GgnnProgramGraph graph,
        final GgnnProgramGraph.EdgeType edgeType,
        int expectedCount
    ) {
        assertDifferentEdgeCounts(graph, edgeType, expectedCount, true);
    }

    private void assertDifferentEdgeTargetsCount(
        final GgnnProgramGraph graph,
        final GgnnProgramGraph.EdgeType edgeType,
        int expectedCount
    ) {
        assertDifferentEdgeCounts(graph, edgeType, expectedCount, false);
    }

    private void assertDifferentEdgeCounts(
        final GgnnProgramGraph graph,
        final GgnnProgramGraph.EdgeType edgeType,
        int expectedCount,
        boolean start
    ) {
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
        Program program = getAST(fixturePath);
        GenerateGgnnGraphTask graphTask = new GenerateGgnnGraphTask(
            program, includeStage, includeDefaultSprites, wholeProgram, label,
            ActorNameNormalizer.getDefault()
        );
        return graphTask.getProgramGraphs();
    }

    private List<Pair<String>> labelledEdges(final Set<Pair<Integer>> edges, final Map<Integer, String> labels) {
        return edges.stream().map(e -> labelledEdge(labels, e)).toList();
    }

    private Pair<String> labelledEdge(final Map<Integer, String> labels, final Pair<Integer> edge) {
        return Pair.of(labels.get(edge.getFst()), labels.get(edge.getSnd()));
    }

    private Path fixture(final String... filename) {
        Path base = Path.of("src", "test", "fixtures");
        for (final String part : filename) {
            base = base.resolve(part);
        }
        return base;
    }

    private Path ggnnFixture(final String... filename) {
        final List<String> pathParts = new ArrayList<>();
        pathParts.add("ml_preprocessing");
        pathParts.add("ggnn");
        pathParts.addAll(List.of(filename));
        return fixture(pathParts.toArray(new String[0]));
    }
}
