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
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

public class GraphAnalyzer extends MLPreprocessingAnalyzer {
    private static final Logger log = Logger.getLogger(GraphAnalyzer.class.getName());

    private final boolean isDotStringGraph;
    private final String labelName;

    public GraphAnalyzer(String input, MLOutputPath output, boolean delete, boolean includeStage, boolean wholeProgram,
                         boolean outputDotStringGraph, String labelName) {
        super(input, output, delete, includeStage, wholeProgram);

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

        GenerateGraphTask generateGraphTask = new GenerateGraphTask(program, input, includeStage, wholeProgram,
                isDotStringGraph, labelName);
        return Optional.of(generateGraphTask.generateGraphData());
    }

    @Override
    protected Path outputFileName(File inputFile) {
        String format = (isDotStringGraph) ? ".dot" : ".txt";
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        return Path.of("GraphData_" + timeStamp + format);
    }
}
