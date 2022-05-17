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

import java.util.Comparator;
import java.util.function.ToDoubleFunction;

/**
 * A fitness function maps a given solution to a numeric value that tells the goodness of the
 * solution. Minimizing fitness functions characterize better solutions by assigning lower values
 * to them, whereas maximizing fitness functions assign higher values.
 *
 * @param <C> the type of solution rated by this fitness function
 */
public interface FitnessFunction<C> extends ToDoubleFunction<C> {
    String DEFAULT_NAME = "fitness_function";

    /**
     * <p>
     * Computes and returns the fitness value of the given solution {@code c}. Minimizing fitness
     * functions must return lower values for better solutions, whereas maximizing fitness
     * functions are expected to return higher values.
     * </p>
     * <p>
     * When two solutions {@code c1} and {@code c2} are equal it is generally recommended to
     * return the same fitness value for both of them. That is, {@code c1.equals(c2)} implies {@code
     * getFitness(c1) == getFitness(c2)}. While this is not an absolute requirement implementations
     * that do not conform to this should clearly indicate this fact.
     * </p>
     *
     * @param c the solution to rate
     * @return the fitness value of the given solutions
     * @throws NullPointerException if {@code null} is given
     */
    double getFitness(C c) throws NullPointerException;

    /**
     * Alias for {@link FitnessFunction#getFitness(C)}.
     */
    @Override
    default double applyAsDouble(C c) throws NullPointerException {
        return getFitness(c);
    }

    default String getName() {
        return DEFAULT_NAME;
    }

    /**
     * Tells whether this function is a minimizing fitness function.
     *
     * @return {@code true} if this is a minimizing fitness function, {@code false} if this is a
     * maximizing fitness function
     */
    boolean isMinimizing();

    /**
     * Returns the reference point of the fitness function for the hypervolume
     *
     * @return {@code 1} if this is a minimizing fitness function, {@code 0} if this is a
     * maximizing fitness function
     */
    double getReferencePoint();

    /**
     * <p>
     * Returns a comparator that compares two solutions by their fitness, taking into account
     * whether this is a maximizing or a minimizing fitness function. In other words, given two
     * solutions {@code c1} and {@code c2} with fitness values {@code f1} and {@code f2},
     * respectively, the comparator will return a positive integer if {@code f1} is better than
     * {@code f2}, zero ({@code 0}) if the two fitness values are equal, and a negative integer if
     * {@code f1} is worse than {@code f2}. If this is a minimizing fitness function, smaller
     * fitness values are considered better, and, on the contrary, if this is a maximizing fitness
     * function, larger fitness values are considered better.
     * </p>
     * <p>
     * Example usage:
     * <pre>{@code
     * FitnessFunction<C> ff = ...;
     * C c1 = ...; // first solution to compare
     * C c2 = ...; // second solution to compare
     *
     * int flag = ff.comparator().compare(c1, c2);
     * if (flag > 0) {
     *     // c1 is better than c2
     * } else if (flag < 0) {
     *     // c2 is better than c1
     * } else {
     *     // c1 and c2 are equally good
     * }
     * }</pre>
     * </p>
     *
     * @return a {@code Comparator<C>} that uses this fitness function as extractor for its sort key
     */
    default Comparator<C> comparator() {
        final Comparator<C> comparator = Comparator.comparingDouble(this);
        return isMinimizing() ? comparator.reversed() : comparator;
    }
}
