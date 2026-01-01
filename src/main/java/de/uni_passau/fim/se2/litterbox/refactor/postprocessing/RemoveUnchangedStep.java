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
package de.uni_passau.fim.se2.litterbox.refactor.postprocessing;

import de.uni_passau.fim.se2.litterbox.ast.util.StructuralEquality;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;

import java.util.List;
import java.util.logging.Logger;

public class RemoveUnchangedStep implements PostProcessorStep {
    private static final Logger log = Logger.getLogger(RemoveUnchangedStep.class.getName());

    @Override
    public List<RefactorSequence> apply(List<RefactorSequence> solutions, PostProcessorContext context) {
        List<RefactorSequence> uniqueSolutions = solutions.stream()
                .filter(s -> !StructuralEquality.areStructurallyEqual(
                        s.getRefactoredProgram(), context.getOriginalProgram()
                ))
                .toList();
        log.info("After filtering unchanged programs: " + uniqueSolutions.size());
        return uniqueSolutions;
    }
}
