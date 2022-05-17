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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions;

/**
 * Represents a minimizing fitness function, which awards lower values to better solutions.
 *
 * @param <C> the type of configuration rated by this function
 */
@FunctionalInterface
public interface MinimizingFitnessFunction<C> extends FitnessFunction<C> {

    /**
     * Always returns {@code true} as this is a minimizing fitness function.
     *
     * @return always {@code true}
     */
    @Override
    default boolean isMinimizing() {
        return true;
    }

    /**
     * Always returns {@code 1} as this is a minimizing fitness function.
     *
     * @return always {@code 1}
     */
    @Override
    default double getReferencePoint() {
        return 1;
    }
}
