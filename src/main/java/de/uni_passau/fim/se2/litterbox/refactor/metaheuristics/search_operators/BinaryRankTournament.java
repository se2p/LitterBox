/*
 * Copyright (C) 2019-2021 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.List;
import java.util.NoSuchElementException;

public class BinaryRankTournament<C extends Solution<C>> implements Selection<C> {

    public BinaryRankTournament() {
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
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Empty population given to binary rank tournament.");
        }
        if (population.size() == 1) {
            return population.get(0);
        }


        int index1 = Randomness.nextInt(population.size());
        C candidate1 = population.get(index1).copy();

        int index2;
        do {
            index2 = Randomness.nextInt(population.size());
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
