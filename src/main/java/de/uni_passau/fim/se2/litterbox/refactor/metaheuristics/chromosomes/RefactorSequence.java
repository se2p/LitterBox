package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class RefactorSequence extends Solution<RefactorSequence> {

    private final List<Integer> productions;

    private final List<RefactoringFinder> refactoringFinders;

    private final Program originalProgram;

    private List<Refactoring> executedRefactorings;

    public List<Integer> getProductions() {
        return productions;
    }

    public List<Refactoring> getExecutedRefactorings() {
        if (executedRefactorings == null) {
            buildRefactoredProgram(); // this method initialises the executedRefactoring list
        }
        return executedRefactorings;
    }

    public Program getOriginalProgram() {
        return originalProgram;
    }

    private static final int MAX_PRODUCTION_NUMBER = PropertyLoader.getSystemIntProperty("nsga-ii.maxProductionNumber");

    /**
     * Lazily build the refactored program, only if it was not previously already build.
     */
    private Supplier<Program> refactoredProgram = () -> {
        var program = buildRefactoredProgram();
        refactoredProgram = () -> program;
        return program;
    };

    public Program getRefactoredProgram() {
        return refactoredProgram.get();
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param originalProgram    the original program without any refactorings applied
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param productions        a list of executed refactorings within the sequence, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Program originalProgram, Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<Integer> productions, List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.originalProgram = originalProgram;
        this.productions = Objects.requireNonNull(productions);
        this.refactoringFinders = Objects.requireNonNull(refactoringFinders);
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param originalProgram    the original program without any refactorings applied
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Program originalProgram, Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.originalProgram = originalProgram;
        this.productions = new LinkedList<>();
        this.refactoringFinders = refactoringFinders;
    }

    /**
     * Apply the refactoring sequence to a given program, without modifying the original program.
     *
     * @return A deep copy of the original program after the refactorings were applied.
     */
    public Program buildRefactoredProgram() {
        executedRefactorings = new LinkedList<>();
        var current = originalProgram;

        for (Integer nthProduction : productions) {

            var executedRefactoring = getExecutedRefactoring(current, nthProduction);
            if (executedRefactoring == null) {
                break;
            }
            executedRefactorings.add(executedRefactoring);
            current = executedRefactoring.apply(current);
        }
        return current;
    }

    private Refactoring getExecutedRefactoring(Program program, Integer nthProduction) {
        List<Refactoring> possibleProductions = new LinkedList<>();
        for (RefactoringFinder refactoringFinder : refactoringFinders) {
            possibleProductions.addAll(refactoringFinder.check(program));
        }
        if (possibleProductions.isEmpty()) {
            return null;
        }
        assert (possibleProductions.size() <= MAX_PRODUCTION_NUMBER) : "Number of productions is larger than codon range";

        int executedProduction = nthProduction % possibleProductions.size();
        return possibleProductions.get(executedProduction);
    }

    @Override
    public RefactorSequence copy() {
        return new RefactorSequence(originalProgram, getMutation(), getCrossover(),
                new ArrayList<>(productions), refactoringFinders);
    }

    /*
     * Attention! The following implementation of equals() compares the PHENOTYPE of two chromosomes, and not their
     * GENOTYPE! This is fine when the population is organized as a List of chromosomes. But it might lead to problems
     * when the population is a Set, since Sets use the equals() method for duplicate elimination. Two chromosomes
     * with the same phenotype might still have different genotypes, and sometimes it might be desirable or advantageous
     * for the search to evolve chromosomes with different genotypes but the same phenotype. The current implementation
     * of equals() would prevent this!
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RefactorSequence)) {
            return false;
        }
        if (executedRefactorings == null
                && ((RefactorSequence) other).getProductions().equals(getProductions())) {
            return true;
        }
        // calculate the executed refactorings for both objects for comparison
        return ((RefactorSequence) other).getRefactoredProgram().equals(getRefactoredProgram());
    }

    @Override
    public int hashCode() {
        // internally calculates refactored program and sets executedRefactoring list
        return getRefactoredProgram().hashCode();
    }

    @Override
    public RefactorSequence self() {
        return this;
    }
}
