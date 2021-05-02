package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * The selection operator is responsible for determining which chromosomes should be subjected to
 * mutation and crossover (parent selection). Often, this is a probabilistic process based on the
 * fitness values of the potential parents to choose from.
 *
 * @param <C> the type of chromosomes supported by this selection operator
 */
@FunctionalInterface
public interface Selection<C extends Chromosome<C>> extends Function<List<C>, C> {

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
    C apply(List<C> population);
}
