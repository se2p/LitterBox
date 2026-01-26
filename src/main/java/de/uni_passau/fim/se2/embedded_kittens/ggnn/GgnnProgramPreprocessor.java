/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.MLProgramPreprocessor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GgnnProgramPreprocessor extends MLProgramPreprocessor<GgnnAnalyzerOutput> {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.shared();

    private final String labelName;
    private final GgnnOutputFormat outputFormat;

    public GgnnProgramPreprocessor(
            final MLPreprocessorCommonOptions commonOptions, final GgnnOutputFormat outputFormat, final String labelName
    ) {
        super(commonOptions);

        this.outputFormat = outputFormat;
        this.labelName = labelName;
    }

    @Override
    public Stream<GgnnAnalyzerOutput> processSprites(Program program) {
        return process(program, false);
    }

    @Override
    public Stream<GgnnAnalyzerOutput> processWholeProgram(Program program) {
        return process(program, true);
    }

    private Stream<GgnnAnalyzerOutput> process(final Program program, final boolean wholeProgram) {
        GenerateGgnnGraphTask generateGgnnGraphTask = new GenerateGgnnGraphTask(
                program, commonOptions.includeStage(), commonOptions.includeDefaultSprites(), wholeProgram,
                labelName, commonOptions.actorNameNormalizer()
        );
        if (outputFormat.isDotGraph()) {
            String label = program.getIdent().getName();
            return Stream.of(new GgnnAnalyzerOutput.DotGraph(generateGgnnGraphTask.generateDotGraphData(label)));
        } else {
            return generateGgnnGraphTask.generateGraphData().map(GgnnAnalyzerOutput.Graph::new);
        }
    }

    @Override
    public String resultToString(final GgnnAnalyzerOutput result) {
        return switch (result) {
            case GgnnAnalyzerOutput.DotGraph(String dotGraph) -> dotGraph;
            case GgnnAnalyzerOutput.Graph(GgnnProgramGraph graph) ->
                    graphToJson(graph).collect(Collectors.joining("\n"));
        };
    }

    private Stream<String> graphToJson(GgnnProgramGraph graph) {
        return Stream.of(OBJECT_MAPPER.writeValueAsString(graph));
    }
}
