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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class GenerateGgnnGraphTask {
    private final Program program;
    private final boolean includeStage;
    private final boolean includeDefaultSprites;
    private final boolean wholeProgramAsSingleGraph;
    private final ActorNameNormalizer actorNameNormalizer;
    private final String labelName;

    public GenerateGgnnGraphTask(Program program, boolean includeStage, boolean includeDefaultSprites,
                                 boolean wholeProgramAsSingleGraph, String labelName,
                                 ActorNameNormalizer actorNameNormalizer) {
        this.program = program;
        this.includeStage = includeStage;
        this.includeDefaultSprites = includeDefaultSprites;
        this.wholeProgramAsSingleGraph = wholeProgramAsSingleGraph;
        this.actorNameNormalizer = actorNameNormalizer;
        this.labelName = labelName == null || labelName.isBlank() ? null : labelName;
    }

    String generateDotGraphData(final String label) {
        final List<GgnnProgramGraph> graphs = getProgramGraphs();
        return GgnnProgramGraphDotGraphBuilder.asDotGraph(graphs, label);
    }

    Stream<String> generateJsonGraphData() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<GgnnProgramGraph> graphs = getProgramGraphs();

        return graphs.stream().flatMap(graph -> {
            try {
                return Stream.of(objectMapper.writeValueAsString(graph));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Stream.empty();
            }
        });
    }

    List<GgnnProgramGraph> getProgramGraphs() {
        List<GgnnProgramGraph> graphs;

        if (wholeProgramAsSingleGraph) {
            String label = Objects.requireNonNullElseGet(labelName, () -> program.getIdent().getName());
            graphs = List.of(buildProgramGraph(program, label));
        } else {
            graphs = buildGraphs(program);
        }

        return graphs;
    }

    private List<GgnnProgramGraph> buildGraphs(final Program program) {
        return program.getActorDefinitionList().getDefinitions()
                .stream()
                .filter(actor -> includeStage || !actor.isStage())
                .filter(actor -> includeDefaultSprites || !NodeNameUtil.hasDefaultName(actor))
                .map(actor -> {
                    final String actorLabel = getActorLabel(actor);
                    return buildProgramGraph(program, actor, actorLabel);
                })
                .toList();
    }

    private String getActorLabel(final ActorDefinition actor) {
        return Optional.ofNullable(labelName)
                .or(() -> actorNameNormalizer.normalizeName(actor))
                .orElse("");
    }

    private GgnnProgramGraph buildProgramGraph(final Program program, String label) {
        GgnnProgramGraph.ContextGraph contextGraph = new GgnnGraphBuilder(program).build();
        return new GgnnProgramGraph(program.getIdent().getName(), label, contextGraph);
    }

    private GgnnProgramGraph buildProgramGraph(final Program program, final ActorDefinition actor, String label) {
        GgnnProgramGraph.ContextGraph contextGraph = new GgnnGraphBuilder(program, actor).build();
        return new GgnnProgramGraph(program.getIdent().getName(), label, contextGraph);
    }
}
