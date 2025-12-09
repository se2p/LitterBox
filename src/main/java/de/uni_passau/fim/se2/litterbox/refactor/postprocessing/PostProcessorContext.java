/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;

import java.nio.file.Path;
import java.util.List;

public class PostProcessorContext {
    private final Program originalProgram;
    private final Path outputDir;
    private final Path originalFile;
    private final List<FitnessFunction<RefactorSequence>> fitnessFunctions;

    public PostProcessorContext(
            Program originalProgram,
            Path outputDir,
            Path originalFile,
            List<FitnessFunction<RefactorSequence>> fitnessFunctions
    ) {
        this.originalProgram = originalProgram;
        this.outputDir = outputDir;
        this.originalFile = originalFile;
        this.fitnessFunctions = fitnessFunctions;
    }

    public Program getOriginalProgram() {
        return originalProgram;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public Path getOriginalFile() {
        return originalFile;
    }

    public List<FitnessFunction<RefactorSequence>> getFitnessFunctions() {
        return fitnessFunctions;
    }
}
