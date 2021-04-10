package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractRefactoringFinder implements RefactoringFinder, ScratchVisitor {

    protected Set<Refactoring> refactorings = new LinkedHashSet<>();
    protected Program program;
    protected boolean ignoreLooseBlocks = false;

    /**
     * Checks the given program for a specific refactoring.
     *
     * @param program The project to check
     * @return a set of instantiated possible refactorings
     */
    @Override
    public Set<Refactoring> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        refactorings = new LinkedHashSet<>();
        program.accept(this);
        return Collections.unmodifiableSet(refactorings);
    }

    @Override
    public void setIgnoreLooseBlocks(boolean value) {
        this.ignoreLooseBlocks = value;
    }
}
