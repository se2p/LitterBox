/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.refactor.postprocessing;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.jsoncreation.JSONFileCreator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

public class WriteResultStep implements PostProcessorStep {
    private static final Logger log = Logger.getLogger(WriteResultStep.class.getName());

    @Override
    public List<RefactorSequence> apply(List<RefactorSequence> solutions, PostProcessorContext context) {
        try {
            if (!context.getOutputDir().toFile().exists()) {
                Files.createDirectories(context.getOutputDir());
            }

            for (int i = 0; i < solutions.size(); i++) {
                Program program = solutions.get(i).getRefactoredProgram();
                String suffix = "_refactored_" + i;
                if (FilenameUtils.getExtension(context.getOriginalFile().toString()).equalsIgnoreCase("json")) {
                    JSONFileCreator.writeJsonFromProgram(program, context.getOutputDir(), suffix);
                } else {
                    JSONFileCreator.writeSb3FromProgram(program, context.getOutputDir(), context.getOriginalFile().toFile(), suffix);
                }
                log.info("Written solution to " + context.getOutputDir().resolve(FilenameUtils.getBaseName(context.getOriginalFile().toString()) + suffix + "." + FilenameUtils.getExtension(context.getOriginalFile().toString())));
            }
        } catch (IOException e) {
            log.severe("Failed to write solutions: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return solutions;
    }
}
