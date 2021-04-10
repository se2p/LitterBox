package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

public class RefactorSequenceCrossover implements Crossover<RefactorSequence> {
    /**
     * Applies this crossover operator to the two given non-null parent chromosomes {@code parent1}
     * and {@code parent2}, and returns the resulting pair of offspring chromosomes.
     * <p>
     * Note: an offspring can equal one of its parents (in terms of {@link Chromosome#equals
     * equals()}. While not an absolute requirement, it is generally advisable parents and offspring
     * be different in terms of reference equality ({@code offspring != parent}) as it tends to
     * simplify the implementation of some search algorithms.
     *
     * @param parent1 a parent
     * @param parent2 another parent
     * @return the offspring formed by applying crossover to the two parents
     * @throws NullPointerException if an argument is {@code null}
     */
    @Override
    public Pair<RefactorSequence> apply(RefactorSequence parent1, RefactorSequence parent2) {
        RefactorSequence child1 = parent1.copy();
        RefactorSequence child2 = parent2.copy();

        // TODO implement one-point crossover on the list if sequence here instead
        // TODO validate crossover solutions afterwards

        return Pair.of(child1, child2);
    }
}
