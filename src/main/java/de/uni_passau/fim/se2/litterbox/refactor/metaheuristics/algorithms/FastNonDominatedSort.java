package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;

import java.util.List;
import java.util.Set;

public class FastNonDominatedSort<C extends Solution<C>> {

    private final Set<FitnessFunction<C>> fitnessFunctions;

    public FastNonDominatedSort(Set<FitnessFunction<C>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    /**
     * solution1 is said to dominate the other solution2 if both condition 1 and 2 below are true:
     * Condition 1: solution1 is no worse than solution2 for all objectives
     * Condition 2: solution1 is strictly better than solution2 in at least one objective
     * <p>
     *
     * @param solution1 Chromosome 1, which is checked, whether it dominates the other chromosome
     * @param solution2 Chromosome 2, which is checked, whether it is dominates by the other chromosome
     * @return {@code true} if solution1 dominates solution2, {@code false} otherwise
     */
    @VisibleForTesting
    boolean dominates(C solution1, C solution2) {
        int dominated = 0;

        for (FitnessFunction<C> fitnessFunction : fitnessFunctions) {
            int compareResult = fitnessFunction.comparator().compare(solution1, solution2);

            if (compareResult < 0) {
                return false;
            } else if (compareResult > 0) {
                // not return yet in case it is worse than a later fitness function
                dominated++;
            }
        }

        return dominated > 0;
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
                if (dominates(solutions.get(p), q)) {
                    dominatesList.get(p).add(q);
                } else if (dominates(q, solutions.get(p))) {
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

    private void fillFronts(List<C> solutions, List<List<C>> fronts, List<List<C>> dominatesList, int[] amountDominated) {
        int i = 0;
        while (!fronts.get(i).isEmpty()) {
            List<C> nextFront = Lists.newLinkedList();
            for (C p : fronts.get(i)) {
                for (C q : dominatesList.get(solutions.indexOf(p))) {
                    amountDominated[solutions.indexOf(q)]--;
                    if (amountDominated[solutions.indexOf(q)] == 0) {
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
}
