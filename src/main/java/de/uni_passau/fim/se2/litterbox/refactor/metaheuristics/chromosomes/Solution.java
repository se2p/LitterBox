package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;

public abstract class Solution<C extends Solution<C>> extends Chromosome<C> {

    private int rank = Integer.MAX_VALUE;
    private double distance = Double.MAX_VALUE;

    // TODO make generic for dynamic amount of fitness functions (also in crowding distance sort)
    private double fitness1 = Double.MAX_VALUE;

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
    }

    /**
     * Creates a copy of this chromosome that uses the same mutation and crossover operators as the
     * given {@code other} chromosome.
     *
     * @param other the chromosome to copy
     * @throws NullPointerException if the given chromosome is {@code null}
     * @apiNote Can be called by copy constructors of implementing subclasses.
     */
    protected Solution(C other) throws NullPointerException {
        super(other);
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

    public double getFitness1() {
        return fitness1;
    }

    public void setFitness1(double fitness1) {
        this.fitness1 = fitness1;
    }
}
