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
