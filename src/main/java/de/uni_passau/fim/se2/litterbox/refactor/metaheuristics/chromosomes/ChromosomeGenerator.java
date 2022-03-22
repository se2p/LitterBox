/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
