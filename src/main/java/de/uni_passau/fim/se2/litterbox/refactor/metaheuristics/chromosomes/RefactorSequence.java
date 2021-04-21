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

    private List<Refactoring> executedRefactorings;

    public List<Integer> getProductions() {
        return productions;
    }

    public List<Refactoring> getExecutedRefactorings() {
        return executedRefactorings;
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param mutation             a strategy that tells how to perform mutation, not {@code null}
     * @param crossover            a strategy that tells how to perform crossover, not {@code null}
     * @param productions          a list of executed refactorings within the sequence, not {@code null}
     * @param refactoringFinders   used refactoringFinders in the run, not {@code null}
     * @param fitnessMap           A map of fitness functions and their value stored inside the solution, not {@code null}
     * @param executedRefactorings A list of the concrete refactorings produced by the given list of productions, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<Integer> productions, List<RefactoringFinder> refactoringFinders,
                            Map<FitnessFunction<RefactorSequence>, Double> fitnessMap,
                            List<Refactoring> executedRefactorings) throws NullPointerException {
        super(mutation, crossover, fitnessMap);
        this.productions = Objects.requireNonNull(productions);
        this.refactoringFinders = Objects.requireNonNull(refactoringFinders);
        this.executedRefactorings = Objects.requireNonNull(executedRefactorings);
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
        this.executedRefactorings = new LinkedList<>();
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
    public RefactorSequence(Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.productions = new LinkedList<>();
        this.refactoringFinders = refactoringFinders;
        this.executedRefactorings = new LinkedList<>();
    }

    /**
     * Apply the refactoring sequence to a given program, without modifying the original program.
     *
     * @param program The original un-refactored program
     * @return A deep copy of the original program after the refactorings were applied.
     */
    public Program applyToProgram(Program program) {
        executedRefactorings = new LinkedList<>();
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
            executedRefactorings.add(executedRefactoring);
            current = executedRefactoring.apply(current);
        }
        return current;
    }


    @Override
    public RefactorSequence copy() {
        return new RefactorSequence(getMutation(), getCrossover(), new ArrayList<>(productions),
                refactoringFinders);
    }


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


    @Override
    public int hashCode() {
        return getProductions().hashCode();
    }

    @Override
    public RefactorSequence self() {
        return this;
    }
}
