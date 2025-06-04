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
package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.SelfTyped;

import java.util.Objects;

/**
 * Chromosomes define a gene representation (genotype) for valid solutions to a given optimization
 * problem. A chromosome consists of several genes, which are usually in fixed positions (loci) and
 * have a value (allele). Chromosomes can be mutated and crossed over to produce offspring.
 *
 * @param <C> the type of chromosomes produced as offspring by mutation and crossover
 * @apiNote Usually, it is desired that chromosomes of type {@code C} produce offspring of the same
 * type {@code C}. This requirement can be enforced at compile time by specifying a recursive type
 * parameter, here: {@code C extends Chromosome<C>}.
 */
public abstract class Chromosome<C extends Chromosome<C>> implements SelfTyped<C> {

    /**
     * The mutation operator telling how to mutate a chromosome of the current type.
     */
    private final Mutation<C> mutation;

    /**
     * The crossover operator defining how to pair two chromosomes of the current type.
     */
    private final Crossover<C> crossover;

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation  a strategy that tells how to perform mutation, not {@code null}
     * @param crossover a strategy that tells how to perform crossover, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    protected Chromosome(final Mutation<C> mutation, final Crossover<C> crossover)
            throws NullPointerException {
        this.mutation = Objects.requireNonNull(mutation);
        this.crossover = Objects.requireNonNull(crossover);
    }

    /**
     * Creates a copy of this chromosome that uses the same mutation and crossover operators as the
     * given {@code other} chromosome.
     *
     * @param other the chromosome to copy
     * @throws NullPointerException if the given chromosome is {@code null}
     * @apiNote Can be called by copy constructors of implementing subclasses.
     */
    protected Chromosome(final C other) throws NullPointerException {
        Objects.requireNonNull(other);
        this.mutation = other.getMutation();
        this.crossover = other.getCrossover();
    }

    /**
     * Returns the mutation operator used by this chromosome.
     *
     * @return the mutation operator
     */
    public Mutation<C> getMutation() {
        return mutation;
    }

    /**
     * Returns the crossover operator used by this chromosome.
     *
     * @return the crossover operator
     */
    public Crossover<C> getCrossover() {
        return crossover;
    }

    /**
     * Applies the mutation operator to this chromosome and returns the resulting offspring.
     *
     * @return the mutated chromosome
     */
    public final C mutate() {
        return mutation.apply(self());
    }

    /**
     * Applies the crossover operator to this chromosome and the given other given chromosome and
     * returns the resulting offspring.
     *
     * @param other the chromosome with which to pair, not {@code null}
     * @return the offspring
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public final Pair<C> crossover(final C other) {
        Objects.requireNonNull(other);
        return crossover.apply(self(), other);
    }

    /**
     * Computes and returns the fitness of this chromosome using the supplied fitness function.
     *
     * @param fitnessFunction the fitness function with which to compute the fitness of this
     *                        chromosome, not {@code null}
     * @return the fitness of this chromosome as computed by the given fitness function
     * @throws NullPointerException if the given fitness function is {@code null}
     * @apiNote This method is primarily intended as syntactic sugar to allow for a more idiomatic,
     * OOP-like use.
     */
    public double getFitness(final FitnessFunction<C> fitnessFunction) {
        Objects.requireNonNull(fitnessFunction);
        return fitnessFunction.getFitness(self());
    }

    /**
     * Creates a copy of this chromosome. Implementors should clearly indicate whether a shallow
     * or deep copy is made.
     *
     * @return a copy of this chromosome
     */
    public abstract C copy();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals(final Object other); // enforce custom implementation

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode(); // enforce custom implementation
}
