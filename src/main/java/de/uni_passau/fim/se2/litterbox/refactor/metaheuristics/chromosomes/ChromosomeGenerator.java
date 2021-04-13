package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import java.util.function.Supplier;

/**
 * A generator for random chromosomes. Can be used in conjunction with {@link
 * FixedSizePopulationGenerator} to create the initial population for a genetic algorithm.
 *
 * @param <C> the type of chromosomes this generator is able to produce
 */
@FunctionalInterface
public interface ChromosomeGenerator<C extends Chromosome<C>> extends Supplier<C> {

    /**
     * Creates and returns a random chromosome. Implementations must ensure that the returned
     * chromosome represents a valid and admissible solution for the problem at hand.
     *
     * @return a random chromosome
     */
    @Override
    C get();
}
