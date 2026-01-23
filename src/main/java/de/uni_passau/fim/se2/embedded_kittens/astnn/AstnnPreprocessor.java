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
package de.uni_passau.fim.se2.embedded_kittens.astnn;

import de.uni_passau.fim.se2.embedded_kittens.MLFilePreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.embedded_kittens.astnn.model.StatementTreeSequence;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

public class AstnnPreprocessor extends MLFilePreprocessor<StatementTreeSequence> {

    /**
     * Sets up an analyzer that extracts the necessary information for a machine learning model from a program.
     *
     * @param commonOptions Some common options used for all machine learning preprocessors.
     */
    public AstnnPreprocessor(final MLPreprocessorCommonOptions commonOptions) {
        super(new AstnnProgramPreprocessor(commonOptions), commonOptions.outputPath());
    }

    @Override
    protected Path outputFileName(final Path inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getFileName().toString()) + ".jsonl");
    }
}
