package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;

import java.util.*;

public class CrowdingDistanceSort<C extends Solution<C>> {

    public void calculateCrowdingDistanceAndSort(List<C> front) {
        updateCrowdingDistances(front);
        front.sort((c1, c2) -> Double.compare(c2.getDistance(), c1.getDistance())); // biggest distance at index 0
    }

    private void updateCrowdingDistances(List<C> front) {
        List<C> sorted = new LinkedList<>(front);
        Map<C, Double> newDistancesPerSolution = new IdentityHashMap<>();
        int l = front.size();

        double f1Max = 0;
        double f1Min = 1;

        for (C c : front) {
            // all solutions should have their fitness values calculated in the fast non dominated sorting
            double f1 = c.getFitness1();

            if (f1 > f1Max) {
                f1Max = f1;
            }
            if (f1 < f1Min) {
                f1Min = f1;
            }

            newDistancesPerSolution.put(c, 0d);
        }

        double rangeFitness1 = f1Max - f1Min;

        sorted.sort(Comparator.comparingDouble(Solution::getFitness1));
        newDistancesPerSolution.put(sorted.get(0), Double.MAX_VALUE);
        newDistancesPerSolution.put(sorted.get(l - 1), Double.MAX_VALUE);
        for (int k = 1; k < l - 1; k++) {
            double newDistance = newDistancesPerSolution.get(sorted.get(k))
                    + (sorted.get(k + 1).getFitness1() - sorted.get(k - 1).getFitness1())
                    / rangeFitness1;
            newDistancesPerSolution.put(sorted.get(k), newDistance);
        }

        for (C c : front) {
            c.setDistance(newDistancesPerSolution.get(c));
        }
    }
}
