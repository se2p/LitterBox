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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import com.google.common.collect.Lists;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms.NSGAII;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.BinaryRankTournament;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OffspringGenerator<C extends Solution<C>> {

    private final BinaryRankTournament<C> binaryRankTournament;

    private static final Logger log = Logger.getLogger(OffspringGenerator.class.getName());
    private static final double CROSSOVER_PROBABILITY = PropertyLoader.getSystemDoubleProperty("nsga-ii.crossoverProbability");

    public OffspringGenerator(BinaryRankTournament<C> binaryRankTournament) {
        this.binaryRankTournament = binaryRankTournament;
    }

    public List<C> generateOffspring(List<C> population) {
        List<C> offspringPopulation = Lists.newLinkedList();

        while (offspringPopulation.size() < population.size()) {
            C parent1 = binaryRankTournament.apply(population);
            C parent2 = binaryRankTournament.apply(population);

            Pair<C> nextOffsprings;
            if (Randomness.nextDouble() < CROSSOVER_PROBABILITY) {
                nextOffsprings = parent1.crossover(parent2);
            } else {
                nextOffsprings = Pair.of(parent1, parent2);
            }

            C offspring1 = nextOffsprings.getFst().mutate();
            C offspring2 = nextOffsprings.getSnd().mutate();

            offspringPopulation.add(offspring1);
            offspringPopulation.add(offspring2);
        }

        // remove one random solution, if the population size was uneven to restore the initial size
        if (population.size() < offspringPopulation.size()) {
            int indexToRemove = Randomness.nextInt(offspringPopulation.size());
            offspringPopulation.remove(indexToRemove);
        }

        return offspringPopulation;
    }
}
