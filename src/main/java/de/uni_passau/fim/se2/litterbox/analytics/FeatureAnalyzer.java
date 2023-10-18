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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class FeatureAnalyzer extends Analyzer<List<FeatureResult>> {

    private static final Logger log = Logger.getLogger(FeatureAnalyzer.class.getName());
    private FeatureTool featureTool;

    public FeatureAnalyzer(Path input, Path output, boolean delete) {
        super(input, output, delete);
        this.featureTool = new FeatureTool();
    }

    /**
     * The method for analyzing one Scratch project file (ZIP). It will produce only console output.
     *
     * @param fileEntry the file to analyze
     */
    @Override
    void checkAndWrite(File fileEntry, Path csv) {
        Program program = extractProgram(fileEntry);
        if (program == null) {
            // Todo error message
            return;
        }

        try {
            featureTool.createCSVFile(program, csv);
        } catch (IOException e) {
            log.warning("Could not create CSV File: " + csv);
        }
    }

    @Override
    public List<FeatureResult> check(File fileEntry) throws IOException {
        Program program = extractProgram(fileEntry);
        return featureTool.calculateFeatures(program);
    }
}
