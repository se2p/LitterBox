package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class BinaryRankTournament<C extends Solution<C>> implements Selection<C> {

    private final Random random;

    public BinaryRankTournament(Random random) {
        this.random = random;
    }

    /**
     * Selects a chromosome to be used as parent for mutation or crossover from the given non-null
     * and non-empty population of chromosomes, and returns the result.
     *
     * @param population the population of chromosomes from which to select
     * @return the selected chromosome
     * @throws NoSuchElementException if the population is empty
     * @throws NullPointerException   if the population is {@code null}
     */
    @Override
    public C apply(List<C> population) {
        int index1 = random.nextInt(population.size());
        C candidate1 = population.get(index1).copy();

        int index2;
        // TODO runs endless for generations size == 1
        do {
            index2 = random.nextInt(population.size());
        } while (index2 == index1);
        C candidate2 = population.get(index2).copy();

        if (candidate1.getRank() < candidate2.getRank()) {
            return candidate1;
        } else if (candidate1.getRank() > candidate2.getRank()) {
            return candidate2;
        } else {
            return candidate1.getDistance() < candidate2.getDistance() ? candidate2 : candidate1;
        }
    }
}
