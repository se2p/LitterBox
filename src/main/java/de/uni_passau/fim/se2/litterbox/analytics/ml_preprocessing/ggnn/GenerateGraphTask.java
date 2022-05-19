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
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.util.StringUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateGraphTask {
    private final Path inputPath;
    private final Program program;
    private final boolean isStageIncluded;
    private final boolean isWholeProgram;
    private final String labelName;

    public GenerateGraphTask(Program program, Path inputPath, boolean isStageIncluded, boolean isWholeProgram,
                             String labelName) {
        this.inputPath = inputPath;
        this.program = program;
        this.isStageIncluded = isStageIncluded;
        this.isWholeProgram = isWholeProgram;
        this.labelName = labelName == null || labelName.isBlank() ? null : labelName;
    }

    String generateDotGraphData(final List<GgnnProgramGraph> graphs, final String label) {
        return GgnnProgramGraphDotGraphBuilder.asDotGraph(graphs, label);
    }

    String generateJsonGraphData(final List<GgnnProgramGraph> graphs) {
        ObjectMapper objectMapper = new ObjectMapper();

        return graphs.stream().flatMap(graph -> {
            try {
                return Stream.of(objectMapper.writeValueAsString(graph));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(Collectors.joining("\n"));
    }

    List<GgnnProgramGraph> getProgramGraphs() {
        List<GgnnProgramGraph> graphs;

        if (isWholeProgram) {
            String label = Objects.requireNonNullElseGet(labelName,
                    () -> FilenameUtils.removeExtension(inputPath.getFileName().toString()));

            graphs = List.of(buildNodeGraph(program, program, label));
        } else {
            graphs = buildGraphs(program, labelName);
        }

        return graphs;
    }

    private List<GgnnProgramGraph> buildGraphs(final Program program, String labelName) {
        return program.getActorDefinitionList().getDefinitions()
                .stream()
                .filter(actor -> !actor.isStage() || isStageIncluded)
                .map(actor -> {
                    String actorLabel = Objects.requireNonNullElseGet(labelName,
                            () -> StringUtil.replaceSpecialCharacters(actor.getIdent().getName()));
                    return buildNodeGraph(program, actor, actorLabel);
                })
                .collect(Collectors.toList());
    }

    private GgnnProgramGraph buildNodeGraph(final Program program, final ASTNode node, String label) {
        GgnnProgramGraph.ContextGraph contextGraph = new GgnnGraphBuilder<>(program, node).build();
        return new GgnnProgramGraph(inputPath.toString(), label, contextGraph);
    }
}
