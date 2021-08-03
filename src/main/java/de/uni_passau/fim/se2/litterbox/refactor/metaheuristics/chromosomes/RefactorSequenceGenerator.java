/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.LinkedList;
import java.util.List;

public class RefactorSequenceGenerator implements ChromosomeGenerator<RefactorSequence> {

    private static final int INITIAL_PRODUCTIONS_PER_SOLUTION = PropertyLoader.getSystemIntProperty("nsga-ii.initialProductionsPerSolution");
    private static final int MAX_PRODUCTION_NUMBER = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");

    private final Mutation<RefactorSequence> mutation;
    private final Crossover<RefactorSequence> crossover;
    private final List<RefactoringFinder> refactoringFinders;
    private final Program originalProgram;

    public RefactorSequenceGenerator(Program originalProgram, Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                                     List<RefactoringFinder> refactoringFinders) {
        this.originalProgram = originalProgram;
        this.mutation = mutation;
        this.crossover = crossover;
        this.refactoringFinders = refactoringFinders;
    }

    /**
     * Creates and returns a random chromosome that represents a valid and admissible solution for the problem at hand.
     *
     * @return a random chromosome
     */
    @Override
    public RefactorSequence get() {
        List<Integer> productions = new LinkedList<>();
        int numberOfProductions = 1 + Randomness.nextInt(INITIAL_PRODUCTIONS_PER_SOLUTION);
        for (int i = 0; i < numberOfProductions; i++) {
            productions.add(Randomness.nextInt(MAX_PRODUCTION_NUMBER));
        }
        return new RefactorSequence(originalProgram, mutation, crossover, productions, refactoringFinders);
    }
}
