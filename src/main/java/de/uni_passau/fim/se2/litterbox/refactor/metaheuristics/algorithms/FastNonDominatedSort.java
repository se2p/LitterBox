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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;

import java.util.Collections;
import java.util.List;

public class FastNonDominatedSort<C extends Solution<C>> {

    private final List<FitnessFunction<C>> fitnessFunctions;

    private Dominance<C> dominance;

    public FastNonDominatedSort(List<FitnessFunction<C>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
        this.dominance = new Dominance<>(fitnessFunctions);
    }

    public FastNonDominatedSort(List<FitnessFunction<C>> fitnessFunctions, Dominance<C> dominance) {
        this.fitnessFunctions = fitnessFunctions;
        this.dominance = dominance;
    }

    /**
     * Implements the fast dominated sort provided in the lecture slides.
     * Sorts the given population of chromosomes into pareto fronts.
     *
     * @param solutions The population of chromosomes.
     * @return A list of pareto fronts of chromosomes, each front being a sorted list again.
     */
    public List<List<C>> fastNonDominatedSort(List<C> solutions) {
        calculateFitnessValuesForSolutions(solutions);

        List<List<C>> fronts = Lists.newLinkedList();
        fronts.add(Lists.newLinkedList());

        // Solution i dominates the solutions in the list at index i
        List<List<C>> dominatesList = Lists.newLinkedList();
        // Solution i is dominated by n[i] other solutions
        int[] amountDominated = new int[solutions.size()];

        for (int i = 0; i < solutions.size(); i++) {
            dominatesList.add(Lists.newLinkedList());
            amountDominated[i] = 0;
        }

        for (int p = 0; p < solutions.size(); p++) {
            dominatesList.set(p, Lists.newLinkedList());
            amountDominated[p] = 0;
            for (C q : solutions) {
                if (dominance.test(solutions.get(p), q)) {
                    dominatesList.get(p).add(q);
                } else if (dominance.test(q, solutions.get(p))) {
                    amountDominated[p]++;
                }
            }
            if (amountDominated[p] == 0) {
                fronts.get(0).add(solutions.get(p));
                solutions.get(p).setRank(0);
            }
        }

        fillFronts(solutions, fronts, dominatesList, amountDominated);
        return fronts;
    }

    private static int indexOfById(List<?> list, Object searchedObject) {
        int i = 0;
        for (Object o : list) {
            if (o == searchedObject) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void fillFronts(List<C> solutions, List<List<C>> fronts, List<List<C>> dominatesList, int[] amountDominated) {
        int i = 0;
        while (!fronts.get(i).isEmpty()) {
            List<C> nextFront = Lists.newLinkedList();
            for (C p : fronts.get(i)) {
                int ip = indexOfById(solutions, p);
                for (C q : dominatesList.get(ip)) {
                    int iq = indexOfById(solutions, q);
                    amountDominated[iq]--;
                    if (amountDominated[iq] == 0) {
                        nextFront.add(q);
                        q.setRank(i + 1);
                    }
                }
            }
            i++;
            fronts.add(nextFront);
        }
        fronts.remove(fronts.size() - 1);
    }

    /**
     * (Re-)Calculates the fitness values for each solution before determining the pareto fronts or the crowding
     * to later on reuse the fitness values instead of duplicating the calculations.
     *
     * @param solutions The list of solutions for the current generation.
     */
    @VisibleForTesting
    void calculateFitnessValuesForSolutions(List<C> solutions) {
        solutions.forEach(c -> fitnessFunctions.forEach(c::getFitness));
    }

    public List<FitnessFunction<C>> getFitnessFunctions() {
        return Collections.unmodifiableList(this.fitnessFunctions);
    }
}
