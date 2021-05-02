package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;

import java.util.List;

/**
 * Represents a stochastic, inherently probabilistic search algorithm that operates according to the
 * laws of Darwinian evolution. Genetic algorithms aspire finding one or more (approximated)
 * solutions to a given problem. Valid admissible solutions are encoded as chromosomes, which form
 * the so called population of the genetic algorithm. A chromosome is allowed to occur multiple
 * times in the population. The population is the "basic unit" upon which genetic algorithms and
 * their search operators work. Here, it is represented as a list of chromosomes.
 *
 * @param <C> the type of chromosome that encodes the problem at hand
 */
public interface GeneticAlgorithm<C extends Chromosome<C>> extends SearchAlgorithm<List<C>> {

    /**
     * Returns a list (i.e., population) of possible admissible solutions to the given problem.
     *
     * @return the solutions
     */
    List<C> findSolution();
}
