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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class RefactorSequenceMutation implements Mutation<RefactorSequence> {

    private static final Logger log = Logger.getLogger(RefactorSequenceMutation.class.getName());
    private static final int NUMBER_OF_POSSIBLE_PRODUCTIONS = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");

    private final List<RefactoringFinder> refactoringFinders;

    public RefactorSequenceMutation(List<RefactoringFinder> refactoringFinders) {
        this.refactoringFinders = refactoringFinders;
    }

    /**
     * <p>
     * Returns a mutated deep copy of the given refactoring sequence.
     * </p>
     * <p>
     * Each integer in the production list mutates with a probability of one divided by the lists size.
     * If a index inside the list mutates it executes one of the following mutations with equal probability:
     * <ol>
     *  <li>add a new production to the list at the index</li>
     *  <li>replace the current production at the index</li>
     *  <li>remove the current production at the index</li>
     * </ol>
     * </p>
     *
     * @param refactorSequence The original RefactorSequence, that mutates.
     * @return A mutated deep copy of the given RefactorSequence object.
     */
    @Override
    public RefactorSequence apply(RefactorSequence refactorSequence) {

        List<Integer> productions = refactorSequence.getProductions();
        double pMutate = 1d / productions.size();
        List<Integer> mutatedProductions = new LinkedList<>(productions);

        var index = 0;
        while (index < mutatedProductions.size()) {
            if (Randomness.nextDouble() < pMutate) {
                index = mutateAtIndex(mutatedProductions, index);
            }
            index++;
        }

        return new RefactorSequence(refactorSequence.getOriginalProgram(), refactorSequence.getMutation(), refactorSequence.getCrossover(), mutatedProductions, refactoringFinders);
    }

    /**
     * Execute one of the allowed mutations, with equally distributed probability.
     * Since this modifies the size of the list, the adjusted index, after a mutation is returned.
     *
     * @param mutatedProductions The list of integer that is being mutated.
     * @param index              The index where to mutate inside the list.
     * @return The modified index after the mutation.
     */
    private int mutateAtIndex(List<Integer> mutatedProductions, int index) {
        var mutation = Randomness.nextInt(3);
        switch (mutation) {
            case 0:
                mutatedProductions.add(index, Randomness.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS));
                index++;
                break;
            case 1:
                mutatedProductions.set(index, Randomness.nextInt(NUMBER_OF_POSSIBLE_PRODUCTIONS));
                break;
            case 2:
            default:
                mutatedProductions.remove(index);
                index--;
                break;
        }
        return index;
    }
}
