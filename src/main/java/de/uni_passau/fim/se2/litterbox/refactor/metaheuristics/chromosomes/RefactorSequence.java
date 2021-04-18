package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.*;

public class RefactorSequence extends Solution<RefactorSequence> {

    // TODO test

    private final List<Integer> productions;

    private final List<RefactoringFinder> refactoringFinders;

    public List<Integer> getProductions() {
        return productions;
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param productions        a list of executed refactorings within the sequence, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @param fitnessMap         A map of fitness functions and their value stored inside the solution
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<Integer> productions, List<RefactoringFinder> refactoringFinders,
                            Map<FitnessFunction<RefactorSequence>, Double> fitnessMap) throws NullPointerException {
        super(mutation, crossover, fitnessMap);
        this.productions = Objects.requireNonNull(productions);
        this.refactoringFinders = Objects.requireNonNull(refactoringFinders);
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param productions        a list of executed refactorings within the sequence, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<Integer> productions, List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.productions = Objects.requireNonNull(productions);
        this.refactoringFinders = Objects.requireNonNull(refactoringFinders);
    }


    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover, List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.productions = new LinkedList<>();
        this.refactoringFinders = refactoringFinders;
    }

    /**
     * Apply the refactoring sequence to a given program, without modifying the original program.
     *
     * @param program The original un-refactored program
     * @return A deep copy of the original program after the refactorings were applied.
     */
    public Program applyToProgram(Program program) {
        Program current = program.deepCopy();

        for (Integer nthProduction : productions) {

            List<Refactoring> possibleProductions = new LinkedList<>();
            for (RefactoringFinder refactoringFinder : refactoringFinders) {
                possibleProductions.addAll(refactoringFinder.check(program));
            }
            if (possibleProductions.isEmpty()) {
                break;
            }

            int executedProduction = nthProduction % possibleProductions.size();
            Refactoring executedRefactoring = possibleProductions.get(executedProduction);
            current = executedRefactoring.apply(current);
        }
        return current;
    }

    /**
     * Creates a copy of this chromosome.
     *
     * @return a copy of this chromosome
     */
    @Override
    public RefactorSequence copy() {
        return new RefactorSequence(getMutation(), getCrossover(), new ArrayList<>(productions), refactoringFinders);
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
        return ((RefactorSequence) other).getProductions().equals(getProductions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getProductions().hashCode();
    }

    @Override
    public RefactorSequence self() {
        return this;
    }
}
