package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RefactorSequence extends Solution<RefactorSequence> {

    private final List<Refactoring> refactorings;

    public List<Refactoring> getRefactorings() {
        return refactorings;
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation     a strategy that tells how to perform mutation, not {@code null}
     * @param crossover    a strategy that tells how to perform crossover, not {@code null}
     * @param productions a list of executed refactorings within the sequence, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover, List<Refactoring> productions) throws NullPointerException {
        super(mutation, crossover);
        this.refactorings = Objects.requireNonNull(productions);
    }

    /**
     * Creates a copy of this chromosome.
     *
     * @return a copy of this chromosome
     */
    @Override
    public RefactorSequence copy() {
        // TODO currently the refactorings are just a shallow copy
        return new RefactorSequence(getMutation(), getCrossover(), new ArrayList<>(refactorings));
    }

    /**
     * {@inheritDoc}
     *
     * @param other
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RefactorSequence)) {
            return false;
        }
        return ((RefactorSequence) other).getRefactorings().equals(getRefactorings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getRefactorings().hashCode();
    }

    @Override
    public RefactorSequence self() {
        return this;
    }
}
