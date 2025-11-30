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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.postprocessing.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RefactoringPostProcessor {

    private static final Logger log = Logger.getLogger(RefactoringPostProcessor.class.getName());

    private final List<FitnessFunction<RefactorSequence>> fitnessFunctions;
    private final List<PostProcessorStep> pipeline;

    public RefactoringPostProcessor(List<FitnessFunction<RefactorSequence>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
        this.pipeline = new ArrayList<>();
        // Default pipeline configuration
        this.pipeline.add(new CleanProgramStep());
        this.pipeline.add(new RemoveUnchangedStep());
        this.pipeline.add(new DeduplicateByFitnessStep());
        this.pipeline.add(new DominationStep());
        this.pipeline.add(new DeduplicateByStructureStep());
        this.pipeline.add(new WriteResultStep());
    }

    public RefactoringPostProcessor(List<FitnessFunction<RefactorSequence>> fitnessFunctions, List<PostProcessorStep> pipeline) {
        this.fitnessFunctions = fitnessFunctions;
        this.pipeline = pipeline;
    }

    public void process(List<RefactorSequence> solutions, Program originalProgram, Path outputDir, Path originalFile) throws IOException {
        if (solutions.isEmpty()) {
            log.info("No solutions found");
            return;
        }

        log.info("Found " + solutions.size() + " solutions");

        PostProcessorContext context = new PostProcessorContext(originalProgram, outputDir, originalFile, fitnessFunctions);
        List<RefactorSequence> currentSolutions = solutions;

        for (PostProcessorStep step : pipeline) {
            currentSolutions = step.apply(currentSolutions, context);
            if (currentSolutions.isEmpty()) {
                break;
            }
        }
    }
}
