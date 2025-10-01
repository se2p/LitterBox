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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

/**
 * Represents a strategy to search for an (approximated) solution to a given problem.
 *
 * @param <C> the type of solution encoding for the problem at hand
 */
public interface SearchAlgorithm<C> {

    /**
     * Runs the search algorithm and returns a possible admissible solution of the encoded problem.
     *
     * <p>Note: every run must perform a new search and must be independent of the previous one. In
     * particular, it must be possible to call this method multiple times in a row. Implementors
     * must ensure multiple runs do not interfere each other.
     *
     * @return a solution
     */
    C findSolution();
}
