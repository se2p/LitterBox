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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.FixedSizePopulationGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.OffspringGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSGAII<C extends Solution<C>> implements GeneticAlgorithm<C> {

    private static final Logger log = Logger.getLogger(NSGAII.class.getName());

    private final FixedSizePopulationGenerator<C> populationGenerator;
    private final OffspringGenerator<C> offspringGenerator;
    private final FastNonDominatedSort<C> fastNonDominatedSort;
    private final CrowdingDistanceSort<C> crowdingDistanceSort;
    private List<List<C>> finalFronts;

    private int iteration = 0;

    private final int maxGen;
    private final int maxSeconds;

    public NSGAII(FixedSizePopulationGenerator<C> populationGenerator,
                  OffspringGenerator<C> offspringGenerator,
                  FastNonDominatedSort<C> fastNonDominatedSort,
                  CrowdingDistanceSort<C> crowdingDistanceSort) {
        this(populationGenerator, offspringGenerator, fastNonDominatedSort, crowdingDistanceSort,
                PropertyLoader.getSystemIntProperty("nsga-ii.generations"),
                PropertyLoader.getSystemIntProperty("nsga-ii.maxSecondsRuntime"));
    }

    public NSGAII(FixedSizePopulationGenerator<C> populationGenerator,
                  OffspringGenerator<C> offspringGenerator,
                  FastNonDominatedSort<C> fastNonDominatedSort,
                  CrowdingDistanceSort<C> crowdingDistanceSort,
                  int maxGen,
                  int maxSeconds) {
        this.populationGenerator = populationGenerator;
        this.offspringGenerator = offspringGenerator;
        this.fastNonDominatedSort = fastNonDominatedSort;
        this.crowdingDistanceSort = crowdingDistanceSort;
        this.maxGen = maxGen;
        this.maxSeconds = maxSeconds;
    }

    private List<C> generateInitialPopulation() {
        List<C> population = populationGenerator.get();
        population.addAll(offspringGenerator.generateOffspring(population));
        return population;
    }

    @Override
    public List<C> findSolution() {
        List<C> population = generateInitialPopulation();
        iteration = 0;
        long end = System.currentTimeMillis() + maxSeconds * 1000L; // maxSeconds seconds * 1000 ms/sec
        while (iteration < maxGen && System.currentTimeMillis() < end) {
            log.log(Level.FINE, "### NSGA-II ITERATION {0} ###", iteration);

            population = evolve(population);

            logIterationStatistics(population);
            iteration++;
        }

        if (iteration == maxGen) {
            log.log(Level.FINE, "NSGA-II stopped after reaching the maximum iteration {0}", maxGen);
        } else {
            log.log(Level.FINE, "NSGA-II stopped after running for the maximum time of {0} seconds", maxSeconds);
        }

        finalFronts = fastNonDominatedSort.fastNonDominatedSort(population);
        population = finalFronts.getFirst();
        log.log(Level.FINE, "Final first front has size {0}, of which are distict: {1}",
                new Object[]{population.size(), population.stream().distinct().count()});

        this.finalPopulation = population;
        return population.stream().distinct().toList(); // only return unique solutions
    }

    private void logIterationStatistics(List<C> population) {
        log.log(Level.FINE, "NSGA-II iteration {0} created a population with size {1}",
                new Object[]{iteration, population.size()});
        for (FitnessFunction<C> fitnessFunction : fastNonDominatedSort.getFitnessFunctions()) {
            if (fitnessFunction.isMinimizing()) {
                log.log(Level.FINE, "Best fitness " + fitnessFunction.getName() + ": "
                        + population.stream().mapToDouble(i -> i.getFitness(fitnessFunction)).min().getAsDouble());
            } else {
                log.log(Level.FINE, "Best fitness " + fitnessFunction.getName() + ": "
                        + population.stream().mapToDouble(i -> i.getFitness(fitnessFunction)).max().getAsDouble());
            }
            log.log(Level.FINE, "Average fitness " + fitnessFunction.getName() + ": "
                    + population.stream().mapToDouble(i -> i.getFitness(fitnessFunction)).average().getAsDouble());
        }
    }

    private List<C> finalPopulation;

    public List<C> getFinalPopulation() {
        return finalPopulation;
    }

    public List<List<C>> getFinalFronts() {
        return finalFronts;
    }

    @VisibleForTesting
    List<C> evolve(List<C> population) {
        List<List<C>> nonDominatedSortedSolution = fastNonDominatedSort.fastNonDominatedSort(population);
        log.log(Level.FINE, "Fronts: " + nonDominatedSortedSolution.size());
        if (!nonDominatedSortedSolution.isEmpty()) {
            log.log(Level.FINE, "First Front Size: " + nonDominatedSortedSolution.getFirst().size());
        }
        population = Lists.newLinkedList();

        for (List<C> f : nonDominatedSortedSolution) {
            List<C> front = Lists.newArrayList(f);
            crowdingDistanceSort.calculateCrowdingDistanceAndSort(front);

            for (C c : front) {
                population.add(c);
                if (population.size() == populationGenerator.getPopulationSize()) {
                    break;
                }
            }
            if (population.size() == populationGenerator.getPopulationSize()) {
                break;
            }
        }
        population.addAll(offspringGenerator.generateOffspring(population));
        return population;
    }

    public int getIteration() {
        return iteration;
    }

    public List<FitnessFunction<C>> getFitnessFunctions() {
        return fastNonDominatedSort.getFitnessFunctions();
    }
}
