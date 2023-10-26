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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.metric.FeatureResult;
import de.uni_passau.fim.se2.litterbox.analytics.metric.FeatureTool;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class FeatureAnalyzer extends Analyzer<List<FeatureResult>> {

    private static final Logger log = Logger.getLogger(FeatureAnalyzer.class.getName());
    private final FeatureTool featureTool;

    public FeatureAnalyzer(Path input, Path output, boolean delete) {
        super(input, output, delete);
        this.featureTool = new FeatureTool();
    }

    @Override
    protected void checkAndWrite(File file) throws IOException {
        final Program program = extractProgram(file);
        if (program == null) {
            return;
        }

        featureTool.createCSVFile(program, output);
    }

    @Override
    protected void writeResultToFile(Path projectFile, Program program, List<FeatureResult> checkResult)
            throws IOException {
        try {
            featureTool.createCSVFile(program, output);
        } catch (IOException e) {
            log.warning("Could not create CSV File: " + output);
            throw e;
        }
    }

    @Override
    public List<FeatureResult> check(Program program) {
        return featureTool.calculateFeatures(program);
    }
}
