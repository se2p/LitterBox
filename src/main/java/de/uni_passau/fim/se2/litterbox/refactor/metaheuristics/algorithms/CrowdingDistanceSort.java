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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.*;

public class CrowdingDistanceSort<C extends Solution<C>> {

    private final List<FitnessFunction<C>> fitnessFunctions;

    public CrowdingDistanceSort(List<FitnessFunction<C>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    public void calculateCrowdingDistanceAndSort(List<C> front) {
        updateCrowdingDistances(front);
        front.sort((c1, c2) -> Double.compare(c2.getDistance(), c1.getDistance())); // biggest distance at index 0
    }

    private void updateCrowdingDistances(List<C> front) {
        List<C> sorted = new LinkedList<>(front);
        Map<C, Double> newDistancesPerSolution = new IdentityHashMap<>();
        int l = front.size();

        Map<FitnessFunction<C>, Pair<Double>> minMaxPairPerFF = new LinkedHashMap<>();

        for (FitnessFunction<C> ff : fitnessFunctions) {
            double ffMax = Double.MIN_VALUE;
            double ffMin = Double.MAX_VALUE;

            for (C c : front) {
                // all solutions should have their fitness values calculated in the fast non dominated sorting
                double currentFitness = c.getFitness(ff);

                if (currentFitness > ffMax) {
                    ffMax = currentFitness;
                }
                if (currentFitness < ffMin) {
                    ffMin = currentFitness;
                }

                newDistancesPerSolution.put(c, 0d);
            }
            minMaxPairPerFF.put(ff, Pair.of(ffMin, ffMax));
        }


        for (FitnessFunction<C> ff : fitnessFunctions) {
            double rangeFitness = minMaxPairPerFF.get(ff).getSnd() - minMaxPairPerFF.get(ff).getFst();

            sorted.sort(Comparator.comparingDouble(c -> c.getFitness(ff)));
            newDistancesPerSolution.put(sorted.get(0), Double.MAX_VALUE);
            newDistancesPerSolution.put(sorted.get(l - 1), Double.MAX_VALUE);
            for (int k = 1; k < l - 1; k++) {
                double newDistance = newDistancesPerSolution.get(sorted.get(k))
                        + (sorted.get(k + 1).getFitness(ff) - sorted.get(k - 1).getFitness(ff))
                        / rangeFitness;
                newDistancesPerSolution.put(sorted.get(k), newDistance);
            }
        }

        for (C c : front) {
            c.setDistance(newDistancesPerSolution.get(c));
        }
    }
}
