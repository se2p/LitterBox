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
import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DeduplicateByStructureStep implements PostProcessorStep {
    private static final Logger log = Logger.getLogger(DeduplicateByStructureStep.class.getName());

    @Override
    public List<RefactorSequence> apply(List<RefactorSequence> solutions, PostProcessorContext context) {
        List<RefactorSequence> uniqueSolutions = new ArrayList<>();
        int duplicateOthers = 0;

        for (RefactorSequence sequence : solutions) {
            Program program = sequence.getRefactoredProgram();
            boolean isDuplicate = false;
            for (RefactorSequence existing : uniqueSolutions) {
                if (StructuralEquality.areStructurallyEqual(existing.getRefactoredProgram(), program)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate) {
                uniqueSolutions.add(sequence);
            } else {
                duplicateOthers++;
            }
        }
        log.info("Removed " + duplicateOthers + " solutions identical to other solutions.");
        log.info("Unique solutions: " + uniqueSolutions.size());
        return uniqueSolutions;
    }
}
