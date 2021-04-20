package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.BinaryRankTournament;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.List;
import java.util.Random;

public class OffspringGenerator<C extends Solution<C>> {

    private final Random random;
    private final BinaryRankTournament<C> binaryRankTournament;

    private static final double CROSSOVER_PROBABILITY = Double.parseDouble(System.getProperty("nsga-ii.crossoverProbability"));

    public OffspringGenerator(Random random, BinaryRankTournament<C> binaryRankTournament) {
        this.random = random;
        this.binaryRankTournament = binaryRankTournament;
    }

    public List<C> generateOffspring(List<C> population) {
        List<C> offspringPopulation = Lists.newLinkedList();

        while (offspringPopulation.size() < population.size()) {
            C parent1 = binaryRankTournament.apply(population);
            C parent2 = binaryRankTournament.apply(population);

            Pair<C> nextOffsprings;
            if (random.nextDouble() < CROSSOVER_PROBABILITY) {
                nextOffsprings = parent1.crossover(parent2);
            } else {
                nextOffsprings = Pair.of(parent1, parent2);
            }

            C offspring1 = nextOffsprings.getFst().mutate();
            C offspring2 = nextOffsprings.getSnd().mutate();

            offspringPopulation.add(offspring1);
            offspringPopulation.add(offspring2);
        }

        // remove one random solution, if the population size was uneven to restore the initial size
        if (population.size() < offspringPopulation.size()) {
            int indexToRemove = random.nextInt(offspringPopulation.size());
            offspringPopulation.remove(indexToRemove);
        }

        return offspringPopulation;
    }
}
