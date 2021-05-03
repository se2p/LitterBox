package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.FixedSizePopulationGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.OffspringGenerator;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.List;
import java.util.stream.Collectors;

public class NSGAII<C extends Solution<C>> implements GeneticAlgorithm<C> {

    private final FixedSizePopulationGenerator<C> populationGenerator;
    private final OffspringGenerator<C> offspringGenerator;
    private final FastNonDominatedSort<C> fastNonDominatedSort;
    private final CrowdingDistanceSort<C> crowdingDistanceSort;

    private static final int MAX_GEN = PropertyLoader.getSystemIntProperty("nsga-ii.generations");
    private static final int MAX_SECONDS = PropertyLoader.getSystemIntProperty("nsga-ii.maxSecondsRuntime");

    public NSGAII(FixedSizePopulationGenerator<C> populationGenerator,
                  OffspringGenerator<C> offspringGenerator,
                  FastNonDominatedSort<C> fastNonDominatedSort,
                  CrowdingDistanceSort<C> crowdingDistanceSort) {
        this.populationGenerator = populationGenerator;
        this.offspringGenerator = offspringGenerator;
        this.fastNonDominatedSort = fastNonDominatedSort;
        this.crowdingDistanceSort = crowdingDistanceSort;
    }

    private List<C> generateInitialPopulation() {
        List<C> population = populationGenerator.get();
        population.addAll(offspringGenerator.generateOffspring(population));
        return population;
    }

    @Override
    public List<C> findSolution() {
        List<C> population = generateInitialPopulation();
        var iteration = 0;
        long end = System.currentTimeMillis() + MAX_SECONDS * 1000L; // MAX_SECONDS seconds * 1000 ms/sec
        while (iteration < MAX_GEN && System.currentTimeMillis() < end) {
            iteration++;
            population = evolve(population);
        }
        return fastNonDominatedSort.fastNonDominatedSort(population).get(0).stream().distinct().collect(Collectors.toList());
    }

    @VisibleForTesting
    List<C> evolve(List<C> population) {
        List<List<C>> nonDominatedSortedSolution = fastNonDominatedSort.fastNonDominatedSort(population);
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
}
