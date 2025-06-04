/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * The crossover operator recombines the genetic material of two given chromosomes (acting as
 * parents) and produces two new chromosomes (offspring) as a result. Often, crossover simply
 * chooses a random locus and exchanges the genes before and after that locus between the two
 * parents.
 *
 * @param <C> the type of chromosomes supported by this crossover operator
 * @apiNote Usually, it is desired that the offspring of two chromosomes of type {@code C} is again
 * of the same type {@code C}. This requirement can be enforced at compile time by specifying a
 * recursive type parameter, here: {@code C extends Chromosome<C>}.
 */
@FunctionalInterface
public interface Crossover<C extends Chromosome<C>> extends BiFunction<C, C, Pair<C>> {

    /**
     * A crossover operator that returns the two given parent chromosomes as offspring without
     * actually modifying them.
     *
     * @param <C> the type of chromosomes
     * @return a crossover operator that returns the parents as offspring
     * @apiNote Can be useful for creating dummy chromosomes when writing unit tests.
     */
    static <C extends Chromosome<C>> Crossover<C> identity() {
        return (c, d) -> Pair.of(c, d).map(C::copy);
    }

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
    Pair<C> apply(final C parent1, final C parent2);

    /**
     * Applies crossover to the given pair of parent chromosomes and returns the resulting pair of
     * offspring chromosomes.
     *
     * @param parents the parent chromosomes
     * @return the offspring formed by applying crossover to the two parents
     * @throws NullPointerException if an argument is {@code null}
     * @apiNote This method is equivalent to {@link Crossover#apply(C, C)} but instead of taking the
     * parents as individual arguments it receives them as pair.
     */
    default Pair<C> apply(final Pair<? extends C> parents) {
        return Objects.requireNonNull(parents).reduce(this);
    }
}
