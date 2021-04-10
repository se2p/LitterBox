package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.RefactorSequence;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.LinkedList;
import java.util.List;

public class RefactorSequenceMutation implements Mutation<RefactorSequence> {

    private final Program program;
    private final List<RefactoringFinder> refactoringFinders;

    public RefactorSequenceMutation(Program program, List<RefactoringFinder> refactoringFinders) {
        this.program = program;
        this.refactoringFinders = refactoringFinders;
    }

    @Override
    public RefactorSequence apply(RefactorSequence refactorSequence) {

        Program currentState = getCurrentState(refactorSequence);

        List<Refactoring> possibleRefactorings = new LinkedList<>();
        for (RefactoringFinder finder : refactoringFinders) {
            possibleRefactorings.addAll(finder.check(currentState));
        }
        if (possibleRefactorings.isEmpty()) {
            return refactorSequence.copy();
        }

        List<Refactoring> mutatedRefactorings = refactorSequence.getRefactorings();
        int numberOfRefactorings = 1; // TODO replace with random value
        for (int i = 0; i < numberOfRefactorings; i++) {
            int index = 0; // TODO replace with random value
            mutatedRefactorings.add(possibleRefactorings.get(index));
        }

        // TODO validate new solution

        return new RefactorSequence(refactorSequence.getMutation(), refactorSequence.getCrossover(), mutatedRefactorings);
    }

    private Program getCurrentState(RefactorSequence refactorSequence) {
        Program currentState = program.copy();
        for (Refactoring refactoring : refactorSequence.getRefactorings()) {
            if (refactoring.preCondition()) {
                currentState = refactoring.apply(currentState);
                if (!refactoring.postCondition()) {
                    throw new IllegalStateException("Post Condition failed in mutation");
                }
            }
        }
        return currentState;
    }
}
