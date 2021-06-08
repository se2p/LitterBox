package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractRefactoringFinder implements RefactoringFinder, ScratchVisitor {

    protected List<Refactoring> refactorings;

    /**
     * Checks the given program for a specific refactoring.
     *
     * @param program The project to check
     * @return a list of instantiated possible refactorings
     */
    @Override
    public List<Refactoring> check(Program program) {
        Preconditions.checkNotNull(program);
        refactorings = new LinkedList<>();
        program.accept(this);
        return Collections.unmodifiableList(refactorings);
    }

}
