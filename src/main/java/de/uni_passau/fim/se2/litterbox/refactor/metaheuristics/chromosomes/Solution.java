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

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Solution<C extends Solution<C>> extends Chromosome<C> {

    // the rank of this solution inside a front
    private int rank = Integer.MAX_VALUE;
    // the crowding distance towards other solutions
    private double distance = Double.MAX_VALUE;
    // a map from fitness functions to a double value to cache the evaluations of a solution
    private final Map<FitnessFunction<C>, Double> fitnessMap;

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation  a strategy that tells how to perform mutation, not {@code null}
     * @param crossover a strategy that tells how to perform crossover, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    protected Solution(Mutation<C> mutation, Crossover<C> crossover) throws NullPointerException {
        super(mutation, crossover);
        this.fitnessMap = new LinkedHashMap<>();
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation  a strategy that tells how to perform mutation, not {@code null}
     * @param crossover a strategy that tells how to perform crossover, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    protected Solution(Mutation<C> mutation, Crossover<C> crossover, Map<FitnessFunction<C>, Double> fitnessMap) throws NullPointerException {
        super(mutation, crossover);
        this.fitnessMap = Objects.requireNonNull(fitnessMap);
    }

    /**
     * Creates a deep copy of this chromosome that uses the same mutation and crossover operators as the
     * given {@code other} chromosome.
     *
     * @param other the chromosome to copy
     * @throws NullPointerException if the given chromosome is {@code null}
     * @apiNote Can be called by copy constructors of implementing subclasses.
     */
    protected Solution(C other) throws NullPointerException {
        super(other);
        this.fitnessMap = new LinkedHashMap<>(other.getFitnessMap());
        this.rank = other.getRank();
        this.distance = other.getDistance();
    }

    /**
     * Computes and returns the fitness of this chromosome using the supplied fitness function.
     * If this fitness function was already given once, use a cached value from the fitness map.
     *
     * @param fitnessFunction the fitness function with which to compute the fitness of this
     *                        chromosome, not {@code null}
     * @return the fitness of this chromosome as computed by the given fitness function
     * @throws NullPointerException if the given fitness function is {@code null}
     * @apiNote This method is primarily intended as syntactic sugar to allow for a more idiomatic,
     * OOP-like use.
     */
    @Override
    public double getFitness(final FitnessFunction<C> fitnessFunction) {
        Objects.requireNonNull(fitnessFunction);
        fitnessMap.computeIfAbsent(fitnessFunction, cFitnessFunction -> cFitnessFunction.getFitness(self()));
        return fitnessMap.get(fitnessFunction);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Map<FitnessFunction<C>, Double> getFitnessMap() {
        return fitnessMap;
    }
}
