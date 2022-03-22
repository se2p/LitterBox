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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class RefactorSequenceCrossover implements Crossover<RefactorSequence> {

    public RefactorSequenceCrossover() {
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
    public Pair<RefactorSequence> apply(RefactorSequence parent1, RefactorSequence parent2) {
        RefactorSequence child1 = parent1.copy();
        RefactorSequence child2 = parent2.copy();

        if (child1.getProductions().size() > 1 && !child2.getProductions().isEmpty()) {
            int crossoverPoint;
            do {
                crossoverPoint = 1 + Randomness.nextInt(child1.getProductions().size() - 1); // exclude 0 as valid crossoverPoint
            } while (crossoverPoint > child2.getProductions().size()); // only accept solutions where a swap is valid

            List<Integer> child1List = new ArrayList<>(child1.getProductions().subList(0, crossoverPoint));
            List<Integer> child2List = new ArrayList<>(child2.getProductions().subList(0, crossoverPoint));

            child1List.addAll(new ArrayList<>(child2.getProductions().subList(crossoverPoint, child2.getProductions().size())));
            child2List.addAll(new ArrayList<>(child1.getProductions().subList(crossoverPoint, child1.getProductions().size())));

            child1.getProductions().clear();
            child2.getProductions().clear();

            child1.getProductions().addAll(child1List);
            child2.getProductions().addAll(child2List);
        }

        return Pair.of(child1, child2);
    }
}
