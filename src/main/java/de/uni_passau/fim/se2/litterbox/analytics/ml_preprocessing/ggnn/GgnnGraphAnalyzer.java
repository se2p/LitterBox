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

import de.uni_passau.fim.se2.litterbox.analytics.MLPreprocessingAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

public class GgnnGraphAnalyzer extends MLPreprocessingAnalyzer {
    private static final Logger log = Logger.getLogger(GgnnGraphAnalyzer.class.getName());

    private final boolean isDotStringGraph;
    private final String labelName;

    public GgnnGraphAnalyzer(final MLPreprocessorCommonOptions commonOptions, boolean outputDotStringGraph,
                             String labelName) {
        super(commonOptions);

        this.isDotStringGraph = outputDotStringGraph;
        this.labelName = labelName;
    }

    @Override
    protected Optional<String> process(File inputFile) {
        Program program = extractProgram(inputFile);
        if (program == null) {
            log.warning("Program was null. File name was '" + inputFile.getName() + "'");
            return Optional.empty();
        }

        GenerateGgnnGraphTask generateGgnnGraphTask = new GenerateGgnnGraphTask(program, input, includeStage,
                                                                                wholeProgram, labelName);
        if (isDotStringGraph) {
            String label = FilenameUtils.removeExtension(inputFile.getName());
            return Optional.of(generateGgnnGraphTask.generateDotGraphData(label));
        } else {
            return Optional.of(generateGgnnGraphTask.generateJsonGraphData());
        }
    }

    @Override
    protected Path outputFileName(File inputFile) {
        String format = (isDotStringGraph) ? ".dot" : ".jsonl";
        return Path.of("GraphData_" + FilenameUtils.removeExtension(inputFile.getName()) + format);
    }
}
