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
package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Chromosome;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;

import java.util.*;
import java.util.stream.Stream;

/**
 * Class for computing the quality of a Pareto front in two-dimensional spaces using the
 * hyper-volume indicator. It measures the size of the space enclosed by all solutions on the Pareto
 * front and a user-defined reference point. The hyper-volume accounts for both proximity and
 * diversity and is strictly Pareto compliant. This means that whenever a given Pareto front
 * dominates another front the hyper-volume of the former front is strictly greater than the volume
 * of the latter. On significant drawback to this measure is that its magnitude is dependent upon an
 * arbitrarily chosen reference point.
 *
 * @param <C> the type of chromosomes in the front
 */
public final class HyperVolume2D<C extends Chromosome<C>> {

    /**
     * The first objective.
     */
    private final FitnessFunction<C> objective1;

    /**
     * The second objective.
     */
    private final FitnessFunction<C> objective2;

    /**
     * Reference point for the first objective.
     */
    private final double r1;

    /**
     * Reference point for the second objective.
     */
    private final double r2;

    /**
     * Creates a new instance using the given objectives and reference point for hyper-volume
     * computation.
     *
     * @param objective1 the first objective
     * @param objective2 the second objective
     * @param reference1 reference value for the first objective
     * @param reference2 reference value for the second objective
     * @throws NullPointerException if an objective is null
     */
    public HyperVolume2D(
            final FitnessFunction<C> objective1, final FitnessFunction<C> objective2,
            final double reference1, final double reference2) throws NullPointerException {
        this.objective1 = Objects.requireNonNull(objective1);
        this.objective2 = Objects.requireNonNull(objective2);
        this.r1 = reference1;
        this.r2 = reference2;
    }

    /**
     * Computes the area of the rectangle bounded by two given points.
     *
     * @param x1 x-coordinate of the first point
     * @param y1 y-coordinate of the first point
     * @param x2 x-coordinate of the second point
     * @param y2 y-coordinate of the second point
     * @return the area of the rectangle
     */
    private static double rectangleArea(
            final double x1, final double y1,
            final double x2, final double y2) {
        return Math.abs((x1 - x2) * (y1 - y2));
    }

    /**
     * Computes the area of the rectangle bounded by the coordinates of the given points. More
     * precisely, the first point of the rectangle is given by taking the coordinate of the
     * reference point in one dimension and the fitness value of the pair's first chromosome in the
     * other dimension. The second point is given by the fitness values of the pair's second
     * chromosome.
     *
     * @param p the pair of chromosomes to consider
     * @return the area of the bounded rectangle (see above)
     */
    private double boundedRectangleArea(final Stream<C> p) {
        // TODO: kind of clumsy if you only have two elements...
        final List<C> chromosomes = p.toList();
        final var previous = chromosomes.get(0);
        final var current = chromosomes.get(1);

        final double v12 = previous.getFitness(objective2);
        final double v21 = current.getFitness(objective1);
        final double v22 = current.getFitness(objective2);
        return rectangleArea(r1, v12, v21, v22);

        // Alternatively, if sorted by objective1:
        // final double v11 = previous.getFitness(objective1);
        // return rectangleArea(v11, r2, v21, v22);
    }

    /**
     * Computes the hyper-volume of the given front w.r.t. to the reference point.
     *
     * @param front for which to compute the hyper-volume
     * @return the hyper-volume
     * @throws NullPointerException     if the front is null
     * @throws NoSuchElementException   if the front is empty
     */
    public double compute(final Collection<C> front)
            throws NullPointerException, NoSuchElementException {
        Objects.requireNonNull(front);

        if (front.isEmpty()) {
            throw new NoSuchElementException("empty front");
        }

        /*
         * Important: There must not be any duplicates in the front. Otherwise, the resulting
         * hyper-volume would be too big since we would be taking into account the same solution
         * multiple times. Note: we use a SortedSet, which eliminates all chromosomes that have the
         * same fitness in one objective. This works fine in our 2-dimensional case (since equal
         * fitness values in one objective also imply equal fitness values in the other objective;
         * otherwise, one chromosome would have dominated the other). But it breaks logic if one
         * attempts to generalize this to higher-dimensional use cases. Also, the front must be
         * sorted from best to worst according to the first objective. This can be done using a
         * reverse comparator for objective1 or the comparator for objective2 (since the best
         * solution in objective1 must by construction have the worst value in objective2).
         */
        final var sortedDistinctFront = new TreeSet<>(objective1.comparator().reversed());
        sortedDistinctFront.addAll(front);

        // A rectangle is spanned by the first point in the front and the reference point.
        final C first = sortedDistinctFront.first();
        final double v1 = objective1.getFitness(first);
        final double v2 = objective2.getFitness(first);
        final double areaFirst = rectangleArea(r1, r2, v1, v2);

        if (sortedDistinctFront.size() == 1) {
            return areaFirst;
        }

        // We iterate over the sorted front in pairs and span rectangles by the current point and
        // bounded in one dimension by the previous neighboring point and in the other by
        // the reference point.
        final double areaRest = SlidingWindow.of(sortedDistinctFront, 2)
                .mapToDouble(this::boundedRectangleArea)
                .sum();

        return areaFirst + areaRest;
    }
}
